package it.auties.whatsapp.socket;

import static it.auties.whatsapp.api.ErrorHandler.Location.CRYPTOGRAPHY;
import static it.auties.whatsapp.api.ErrorHandler.Location.LOGGED_OUT;
import static it.auties.whatsapp.api.ErrorHandler.Location.LOGIN;
import static it.auties.whatsapp.api.ErrorHandler.Location.MEDIA_CONNECTION;
import static it.auties.whatsapp.api.ErrorHandler.Location.MESSAGE;
import static it.auties.whatsapp.api.ErrorHandler.Location.STREAM;
import static it.auties.whatsapp.model.request.Node.ofAttributes;
import static it.auties.whatsapp.model.request.Node.ofChildren;
import static java.util.Map.of;
import static java.util.concurrent.CompletableFuture.delayedExecutor;
import static java.util.concurrent.CompletableFuture.runAsync;

import it.auties.bytes.Bytes;
import it.auties.curve25519.Curve25519;
import it.auties.whatsapp.api.DisconnectReason;
import it.auties.whatsapp.api.ErrorHandler.Location;
import it.auties.whatsapp.api.SocketEvent;
import it.auties.whatsapp.binary.PatchType;
import it.auties.whatsapp.crypto.Hmac;
import it.auties.whatsapp.exception.ErroneousNodeRequestException;
import it.auties.whatsapp.exception.HmacValidationException;
import it.auties.whatsapp.exception.UnknownStreamException;
import it.auties.whatsapp.listener.OnNodeReceived;
import it.auties.whatsapp.model.chat.Chat;
import it.auties.whatsapp.model.chat.ChatEphemeralTimer;
import it.auties.whatsapp.model.contact.Contact;
import it.auties.whatsapp.model.contact.ContactJid;
import it.auties.whatsapp.model.contact.ContactJid.Server;
import it.auties.whatsapp.model.contact.ContactJid.Type;
import it.auties.whatsapp.model.contact.ContactStatus;
import it.auties.whatsapp.model.info.MessageInfo;
import it.auties.whatsapp.model.info.MessageInfo.StubType;
import it.auties.whatsapp.model.media.MediaConnection;
import it.auties.whatsapp.model.message.model.MessageKey;
import it.auties.whatsapp.model.message.model.MessageStatus;
import it.auties.whatsapp.model.privacy.PrivacySettingType;
import it.auties.whatsapp.model.privacy.PrivacySettingValue;
import it.auties.whatsapp.model.request.Attributes;
import it.auties.whatsapp.model.request.MessageSendRequest;
import it.auties.whatsapp.model.request.Node;
import it.auties.whatsapp.model.response.ContactStatusResponse;
import it.auties.whatsapp.model.signal.auth.DeviceIdentity;
import it.auties.whatsapp.model.signal.auth.SignedDeviceIdentity;
import it.auties.whatsapp.model.signal.auth.SignedDeviceIdentityHMAC;
import it.auties.whatsapp.model.signal.keypair.SignalPreKeyPair;
import it.auties.whatsapp.serialization.ControllerProviderLoader;
import it.auties.whatsapp.util.BytesHelper;
import it.auties.whatsapp.util.Clock;
import it.auties.whatsapp.util.JacksonProvider;
import it.auties.whatsapp.util.Specification;
import it.auties.whatsapp.util.Validate;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
class StreamHandler extends Handler
    implements JacksonProvider {

  private static final byte[] MESSAGE_HEADER = {6, 0};
  private static final byte[] SIGNATURE_HEADER = {6, 1};
  private static final int REQUIRED_PRE_KEYS_SIZE = 5;
  private static final int PRE_KEYS_UPLOAD_CHUNK = 30;
  private static final int PING_INTERVAL = 30;
  private static final int MEDIA_CONNECTION_DEFAULT_INTERVAL = 60;
  private static final int MAX_ATTEMPTS = 3;

  private final SocketHandler socketHandler;
  private final Map<String, Integer> retries;

  public StreamHandler(SocketHandler socketHandler) {
    this.socketHandler = socketHandler;
    this.retries = new HashMap<>();
  }


  protected void digest(@NonNull Node node) {
    switch (node.description()) {
      case "ack" -> digestAck(node);
      case "call" -> digestCall(node);
      case "failure" -> digestFailure(node);
      case "ib" -> digestIb(node);
      case "iq" -> digestIq(node);
      case "receipt" -> digestReceipt(node);
      case "stream:error" -> digestError(node);
      case "success" -> digestSuccess();
      case "message" -> socketHandler.decodeMessage(node);
      case "notification" -> digestNotification(node);
      case "presence", "chatstate" -> digestChatState(node);
    }
  }

  private void digestFailure(Node node) {
    var location = node.attributes()
        .getOptionalString("location")
        .orElse("unknown");
    var reason = node.attributes()
        .getInt("reason");
    if (reason == 401) {
      socketHandler.errorHandler()
          .handleFailure(LOGGED_OUT, new RuntimeException(
              "The socket was closed from Whatsapp because of a failure at %s with status code %s".formatted(
                  location, reason)));
      return;
    }
    socketHandler.errorHandler()
        .handleFailure(Location.STREAM,
            new ErroneousNodeRequestException("Stream error(%s)".formatted(node), node));
  }

  private void digestChatState(Node node) {
    CompletableFuture.runAsync(() -> {
      var updateType = node.attributes()
          .getOptionalString("type")
          .or(() -> node.findNode()
              .map(Node::description))
          .orElse("available");
      var chatJid = node.attributes()
          .getJid("from")
          .orElseThrow(() -> new NoSuchElementException("Missing from in chat state update"));
      var participantJid = node.attributes()
          .getJid("participant")
          .orElse(chatJid);
      socketHandler.store()
          .findContactByJid(participantJid)
          .ifPresent(contact -> updateContactPresence(chatJid, updateType, contact));
    });
  }

  private void updateContactPresence(ContactJid chatJid, String updateType, Contact contact) {
    var status = ContactStatus.of(updateType)
        .orElse(ContactStatus.AVAILABLE);
    if (status == contact.lastKnownPresence()) {
      return;
    }
    contact.lastKnownPresence(status);
    contact.lastSeen(ZonedDateTime.now());
    socketHandler.store()
        .findChatByJid(chatJid)
        .ifPresent(chat -> {
          chat.presences()
              .put(contact.jid(), status);
          socketHandler.onUpdateChatPresence(status, contact, chat);
        });
  }

  private void digestReceipt(Node node) {
    var type = node.attributes()
        .getOptionalString("type");
    var status = type.flatMap(MessageStatus::of)
        .orElse(MessageStatus.DELIVERED);
    var retry = type.filter(entry -> entry.equals("retry"))
        .isPresent();
    var chat = node.attributes()
        .getJid("from")
        .filter(jid -> jid.type() != Type.STATUS)
        .flatMap(socketHandler.store()::findChatByJid)
        .orElse(null);
    var participant = node.attributes()
        .getJid("participant")
        .flatMap(socketHandler.store()::findContactByJid)
        .orElse(null);
    getReceiptsMessageIds(node)
        .stream()
        .map(messageId -> chat == null ? socketHandler.store().findStatusById(messageId)
            : socketHandler.store().findMessageById(chat, messageId))
        .flatMap(Optional::stream)
        .forEach(message -> {
          message.status(status);
          updateReceipt(status, chat, participant, message);
          socketHandler.onMessageStatus(status, participant, message, chat);
          sendMessageRetry(retry, message);
        });
    var attributes = Attributes.of()
        .put("class", "receipt")
        .put("type", type, Objects::nonNull)
        .toMap();
    socketHandler.sendMessageAck(node, attributes);
  }

  private void sendMessageRetry(boolean retry, MessageInfo message) {
    if (!retry) {
      return;
    }
    if (!message.fromMe()) {
      return;
    }
    var attempts = retries.getOrDefault(message.id(), 0);
    Validate.isTrue(attempts <= MAX_ATTEMPTS,
        "Cannot send message retry: exceeded maximum tries");
    try {
      var all = message.senderJid().device() == 0;
      socketHandler.querySessionsForcefully(message.senderJid());
      message.chat().participantsPreKeys().clear();
      var request = MessageSendRequest.builder()
          .info(message)
          .overrideSender(all ? null : message.senderJid())
          .force(!all)
          .build();
      socketHandler.sendMessage(request);
    } finally {
      retries.put(message.id(), attempts + 1);
    }
  }

  private void updateReceipt(MessageStatus status, Chat chat, Contact participant,
      MessageInfo message) {
    var container = status == MessageStatus.READ ? message.receipt().readJids()
        : message.receipt().deliveredJids();
    container.add(participant != null ? participant.jid() : message.senderJid());
    if (chat != null && participant != null && chat.participants().size() != container.size()) {
      return;
    }
    switch (status) {
      case READ -> message.receipt()
          .readTimestamp(Clock.now());
      case PLAYED -> message.receipt()
          .playedTimestamp(Clock.now());
    }
  }

  private List<String> getReceiptsMessageIds(Node node) {
    var messageIds = Stream.ofNullable(node.findNode("list"))
        .flatMap(Optional::stream)
        .map(list -> list.findNodes("item"))
        .flatMap(Collection::stream)
        .map(item -> item.attributes().getOptionalString("id"))
        .flatMap(Optional::stream)
        .collect(Collectors.toList());
    messageIds.add(node.attributes().getRequiredString("id"));
    return messageIds;
  }

  private void digestCall(Node node) {
    var call = node.children()
        .peekFirst();
    if (call == null) {
      return;
    }
    socketHandler.sendMessageAck(node, of("class", "call", "type", call.description()));
  }

  private void digestAck(Node node) {
    var clazz = node.attributes()
        .getString("class");
    if (!Objects.equals(clazz, "message")) {
      return;
    }
    var messageId = node.id();
    var from = node.attributes()
        .getJid("from")
        .orElseThrow(() -> new NoSuchElementException("Cannot digest ack: missing from"));
    socketHandler.store().findMessageById(from, messageId)
        .filter(message -> message.status().index() < MessageStatus.SERVER_ACK.index())
        .ifPresent(message -> message.status(MessageStatus.SERVER_ACK));
    var receipt = ofAttributes("ack", of("class", "receipt", "id", messageId, "from", from));
    socketHandler.sendWithNoResponse(receipt);
  }

  private void digestNotification(Node node) {
    socketHandler.sendMessageAck(node, node.attributes()
        .toMap());
    var type = node.attributes()
        .getString("type", null);
    switch (type) {
      case "w:gp2" -> handleGroupNotification(node);
      case "server_sync" -> handleServerSyncNotification(node);
      case "account_sync" -> handleAccountSyncNotification(node);
      case "encrypt" -> handleEncryptNotification(node);
      case "picture" -> handlePictureNotification(node);
    }
  }

  private void handlePictureNotification(Node node) {
    var fromJid = node.attributes()
        .getJid("from")
        .orElseThrow(() -> new NoSuchElementException("Missing from in notification"));
    var fromChat = socketHandler.store()
        .findChatByJid(fromJid)
        .orElseGet(() -> socketHandler.store()
            .addChat(fromJid));
    var timestamp = node.attributes()
        .getLong("t");
    if (fromChat.isGroup()) {
      addMessageForGroupStubType(fromChat, StubType.GROUP_CHANGE_ICON, timestamp);
      socketHandler.onGroupPictureChange(fromChat);
      return;
    }
    var fromContact = socketHandler.store()
        .findContactByJid(fromJid)
        .orElseGet(() -> {
          var contact = socketHandler.store()
              .addContact(fromJid);
          socketHandler.onNewContact(contact);
          return contact;
        });
    socketHandler.onContactPictureChange(fromContact);
  }

  private void handleGroupNotification(Node node) {
    node.findNode()
        .map(Node::description)
        .flatMap(StubType::of)
        .ifPresent(stubType -> handleGroupStubNotification(node, stubType));
  }

  private void handleGroupStubNotification(Node node, StubType stubType) {
    var timestamp = node.attributes()
        .getLong("t");
    var fromJid = node.attributes()
        .getJid("from")
        .orElseThrow(() -> new NoSuchElementException("Missing chat in notification"));
    var fromChat = socketHandler.store()
        .findChatByJid(fromJid)
        .orElseGet(() -> socketHandler.store()
            .addChat(fromJid));
    addMessageForGroupStubType(fromChat, stubType, timestamp);
  }

  private void addMessageForGroupStubType(Chat chat, StubType stubType, long timestamp) {
    var key = MessageKey.builder()
        .chatJid(chat.jid())
        .build();
    var message = MessageInfo.builder()
        .timestampInSeconds(timestamp)
        .key(key)
        .ignore(true)
        .stubType(stubType)
        .stubParameters(List.of())
        .build();
    chat.addMessage(socketHandler.store().attribute(message));
  }

  private void handleEncryptNotification(Node node) {
    var chat = node.attributes()
        .getJid("from")
        .orElseThrow(() -> new NoSuchElementException("Missing chat in notification"));
    if (!chat.isServerJid(ContactJid.Server.WHATSAPP)) {
      return;
    }
    var keysSize = node.findNode("count")
        .orElseThrow(() -> new NoSuchElementException("Missing count in notification"))
        .attributes()
        .getLong("value");
    if (keysSize >= REQUIRED_PRE_KEYS_SIZE) {
      return;
    }
    sendPreKeys();
  }

  private void handleAccountSyncNotification(Node node) {
    var child = node.findNode();
    if (child.isEmpty()) {
      return;
    }
    switch (child.get()
        .description()) {
      case "privacy" -> changeUserPrivacySetting(child.get());
      case "disappearing_mode" -> updateUserDisappearingMode(child.get());
      case "status" -> updateUserStatus(true);
      case "picture" -> updateUserPicture(true);
      case "blocklist" -> updateBlocklist(child.orElse(null));
    }
  }

  private void updateBlocklist(Node child) {
    child.findNodes("item")
        .forEach(this::updateBlocklistEntry);
  }

  private void updateBlocklistEntry(Node entry) {
    entry.attributes()
        .getJid("jid")
        .flatMap(socketHandler.store()::findContactByJid)
        .ifPresent(contact -> {
          contact.blocked(Objects.equals(entry.attributes()
              .getString("action"), "block"));
          socketHandler.onContactBlocked(contact);
        });
  }

  private void changeUserPrivacySetting(Node child) {
    child.findNodes("category")
        .forEach(entry -> addPrivacySetting(entry.attributes()));
  }

  private void updateUserDisappearingMode(Node child) {
    socketHandler.store()
        .newChatsEphemeralTimer(ChatEphemeralTimer.of(child.attributes()
            .getLong("duration")));
  }


  private void addPrivacySetting(Attributes entry) {
    var privacyType = PrivacySettingType.of(entry.getString("name"));
    var privacyValue = PrivacySettingValue.of(entry.getString("value"));
    if (privacyType.isEmpty() || privacyValue.isEmpty()) {
      return;
    }
    socketHandler.store()
        .privacySettings()
        .put(privacyType.get(), privacyValue.get());
  }

  private void handleServerSyncNotification(Node node) {
    if (!socketHandler.store()
        .initialAppSync()) {
      return;
    }
    node.findNode("collection")
        .map(entry -> entry.attributes().getRequiredString("name"))
        .map(PatchType::of)
        .ifPresent(socketHandler::pullPatch);
  }

  private void digestIb(Node node) {
    var dirty = node.findNode("dirty");
    if (dirty.isEmpty()) {
      Validate.isTrue(!node.hasNode("downgrade_webclient"),
          "Multi device beta is not enabled. Please enable it from Whatsapp");
      return;
    }
    var type = dirty.get()
        .attributes()
        .getString("type");
    if (!Objects.equals(type, "account_sync")) {
      return;
    }
    var timestamp = dirty.get()
        .attributes()
        .getString("timestamp");
    socketHandler.sendQuery("set", "urn:xmpp:whatsapp:dirty",
        ofAttributes("clean", of("type", type, "timestamp", timestamp)));
  }

  private void digestError(Node node) {
    if (node.hasNode("bad-mac")) {
      socketHandler.errorHandler()
          .handleFailure(CRYPTOGRAPHY, new UnknownStreamException("Detected a bad mac"));
      return;
    }
    var conflict = node.findNode("conflict");
    if (conflict.isPresent()) {
      socketHandler.disconnect(DisconnectReason.LOGGED_OUT);
      return;
    }
    var statusCode = node.attributes()
        .getInt("code");
    switch (statusCode) {
      case 515, 503 -> socketHandler.disconnect(DisconnectReason.RECONNECTING);
      case 401 -> handleStreamError(node);
      default -> node.children()
          .forEach(error -> socketHandler.store()
              .resolvePendingRequest(error, true));
    }
  }

  private void handleStreamError(Node node) {
    var child = node.children()
        .getFirst();
    var type = child.attributes()
        .getString("type");
    var reason = child.attributes()
        .getString("reason", type);
    socketHandler.errorHandler()
        .handleFailure(Objects.equals(reason, "device_removed") ? LOGGED_OUT : STREAM,
            new RuntimeException(reason));
  }

  private void digestSuccess() {
    confirmConnection();
    if (!socketHandler.keys()
        .hasPreKeys()) {
      sendPreKeys();
    }
    getOrCreateScheduledService().scheduleAtFixedRate(this::sendPing, PING_INTERVAL, PING_INTERVAL,
        TimeUnit.SECONDS);
    createMediaConnection(0, null);
    sendStatusUpdate();
    socketHandler.onLoggedIn();
    if (!socketHandler.store()
        .initialSnapshot()) {
      return;
    }
    ControllerProviderLoader.findOnlyDeserializer(socketHandler.options()
            .defaultSerialization())
        .attributeStore(socketHandler.store())
        .thenRun(socketHandler::onChats)
        .exceptionallyAsync(exception -> socketHandler.errorHandler()
            .handleFailure(MESSAGE, exception));
    socketHandler.onContacts();
    if (!socketHandler.options()
        .automaticallySubscribeToPresences()) {
      return;
    }
    var delayedExecutor = CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS);
    subscribeToAllPresences(delayedExecutor);
  }

  private void subscribeToAllPresences(Executor delayedExecutor) {
    var future = CompletableFuture.runAsync(
        () -> socketHandler.store().contacts().forEach(socketHandler::subscribeToPresence),
        delayedExecutor);
    OnNodeReceived listener = node -> {
      if (!node.hasDescription("message") || !node.attributes().hasKey("offline")) {
        return;
      }
      if (future.isDone()) {
        return;
      }
      future.cancel(true);
      subscribeToAllPresences(delayedExecutor);
    };
    socketHandler.store().listeners().add(listener);
    future.thenRunAsync(() -> socketHandler.store().listeners().remove(listener));
  }

  private void sendStatusUpdate() {
    updateSelfPresence();
    socketHandler.queryBlockList()
        .thenAcceptAsync(entry -> entry.forEach(this::markBlocked));
    socketHandler.sendQuery("get", "privacy", Node.of("privacy"))
        .thenAcceptAsync(this::parsePrivacySettings);
    socketHandler.sendQuery("get", "abt",
        ofAttributes("props", of("protocol", "1"))); // Ignore this response
    socketHandler.sendQuery("get", "w", Node.of("props"))
        .thenAcceptAsync(this::parseProps);
    updateUserStatus(false);
    updateUserPicture(false);
  }

  private void updateSelfPresence() {
    socketHandler.sendWithNoResponse(ofAttributes("presence", of("type", "available")));
    socketHandler.store()
        .findContactByJid(socketHandler.store()
            .userCompanionJid()
            .toUserJid())
        .ifPresent(entry -> entry.lastKnownPresence(ContactStatus.AVAILABLE)
            .lastSeen(ZonedDateTime.now()));
  }

  private void updateUserStatus(boolean update) {
    socketHandler.queryStatus(socketHandler.store()
            .userCompanionJid()
            .toUserJid())
        .thenAcceptAsync(result -> parseNewStatus(result.orElse(null), update));
  }

  private void parseNewStatus(ContactStatusResponse result, boolean update) {
    if (result == null) {
      return;
    }
    var oldStatus = socketHandler.store()
        .userStatus();
    socketHandler.store()
        .userStatus(result.status());
    if (!update) {
      return;
    }
    socketHandler.onUserStatusChange(result.status(), oldStatus);
  }

  private void updateUserPicture(boolean update) {
    socketHandler.queryPicture(socketHandler.store()
            .userCompanionJid()
            .toUserJid())
        .thenAcceptAsync(result -> handleUserPictureChange(result.orElse(null), update));
  }

  private void handleUserPictureChange(URI newPicture, boolean update) {
    var oldStatus = socketHandler.store()
        .userProfilePicture()
        .orElse(null);
    socketHandler.store()
        .userProfilePicture(newPicture);
    if (!update) {
      return;
    }
    socketHandler.onUserPictureChange(newPicture, oldStatus);
  }

  private void markBlocked(ContactJid entry) {
    socketHandler.store()
        .findContactByJid(entry)
        .orElseGet(() -> {
          var contact = socketHandler.store()
              .addContact(entry);
          socketHandler.onNewContact(contact);
          return contact;
        })
        .blocked(true);
  }

  private void parsePrivacySettings(Node result) {
    result.children()
        .forEach(entry -> addPrivacySetting(entry.attributes()));
  }

  private void parseProps(Node result) {
    var properties = result.findNode("props")
        .stream()
        .map(entry -> entry.findNodes("prop"))
        .flatMap(Collection::stream)
        .map(node -> Map.entry(node.attributes()
            .getString("name"), node.attributes()
            .getString("value")))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    socketHandler.onMetadata(properties);
  }

  private void sendPing() {
    if (socketHandler.state() != SocketState.CONNECTED) {
      return;
    }
    socketHandler.store()
        .serialize(true);
    socketHandler.sendQueryWithNoResponse("get", "w:p", Node.of("ping"));
    socketHandler.onSocketEvent(SocketEvent.PING);
  }

  private void createMediaConnection(int tries, Throwable error) {
    if (socketHandler.state() != SocketState.CONNECTED) {
      return;
    }
    if (tries >= MAX_ATTEMPTS) {
      socketHandler.store().mediaConnection((MediaConnection) null);
      socketHandler.errorHandler().handleFailure(MEDIA_CONNECTION, error);
      scheduleMediaConnection(MEDIA_CONNECTION_DEFAULT_INTERVAL);
      return;
    }
    socketHandler.sendQuery("set", "w:m", Node.of("media_conn"))
        .thenApplyAsync(MediaConnection::of)
        .thenAcceptAsync(result -> {
          socketHandler.store().mediaConnection(result);
          scheduleMediaConnection(result.ttl());
        })
        .exceptionallyAsync(throwable -> {
          createMediaConnection(tries + 1, throwable);
          return null;
        });
  }

  private void scheduleMediaConnection(int seconds) {
    var executor = delayedExecutor(seconds, TimeUnit.SECONDS);
    runAsync(() -> createMediaConnection(0, null), executor);
  }

  private void digestIq(Node node) {
    var container = node.children()
        .peekFirst();
    if (container == null) {
      return;
    }
    if (container.description()
        .equals("pair-device")) {
      generateQrCode(node, container);
      return;
    }
    if (!container.description()
        .equals("pair-success")) {
      return;
    }
    confirmQrCode(node, container);
  }

  private void confirmConnection() {
    socketHandler.sendQuery("set", "passive", Node.of("active"));
  }

  private void sendPreKeys() {
    var startId = socketHandler.keys()
        .lastPreKeyId() + 1;
    var preKeys = IntStream.range(startId, startId + PRE_KEYS_UPLOAD_CHUNK)
        .mapToObj(SignalPreKeyPair::random)
        .peek(socketHandler.keys()::addPreKey)
        .map(SignalPreKeyPair::toNode)
        .toList();
    socketHandler.sendQuery("set", "encrypt",
        Node.of("registration", BytesHelper.intToBytes(socketHandler.keys()
            .id(), 4)),
        Node.of("type", Specification.Signal.KEY_BUNDLE_TYPE), Node.of("identity",
            socketHandler.keys()
                .identityKeyPair()
                .publicKey()),
        ofChildren("list", preKeys), socketHandler.keys()
            .signedKeyPair()
            .toNode());
  }

  private void generateQrCode(Node node, Node container) {
    printQrCode(container);
    sendConfirmNode(node, null);
  }

  private void printQrCode(Node container) {
    var ref = container.findNode("ref")
        .flatMap(Node::contentAsString)
        .orElseThrow(() -> new NoSuchElementException("Missing ref"));
    var qr = "%s,%s,%s,%s".formatted(ref, Bytes.of(socketHandler.keys()
            .noiseKeyPair()
            .publicKey())
        .toBase64(), Bytes.of(socketHandler.keys()
            .identityKeyPair()
            .publicKey())
        .toBase64(), Bytes.of(socketHandler.keys()
            .companionKey())
        .toBase64());
    socketHandler.options()
        .qrHandler()
        .accept(qr);
  }

  @SneakyThrows
  private void confirmQrCode(Node node, Node container) {
    saveCompanion(container);
    var deviceIdentity = container.findNode("device-identity")
        .orElseThrow(() -> new NoSuchElementException("Missing device identity"));
    var advIdentity = PROTOBUF.readMessage(deviceIdentity.contentAsBytes()
        .orElseThrow(), SignedDeviceIdentityHMAC.class);
    var advSign = Hmac.calculateSha256(advIdentity.details(), socketHandler.keys()
        .companionKey());
    if (!Arrays.equals(advIdentity.hmac(), advSign)) {
      socketHandler.errorHandler()
          .handleFailure(LOGIN, new HmacValidationException("adv_sign"));
      return;
    }
    var account = PROTOBUF.readMessage(advIdentity.details(), SignedDeviceIdentity.class);
    var message = Bytes.of(MESSAGE_HEADER)
        .append(account.details())
        .append(socketHandler.keys()
            .identityKeyPair()
            .publicKey())
        .toByteArray();
    if (!Curve25519.verifySignature(account.accountSignatureKey(), message,
        account.accountSignature())) {
      socketHandler.errorHandler()
          .handleFailure(LOGIN, new HmacValidationException("message_header"));
      return;
    }
    var deviceSignatureMessage = Bytes.of(SIGNATURE_HEADER)
        .append(account.details())
        .append(socketHandler.keys()
            .identityKeyPair()
            .publicKey())
        .append(account.accountSignatureKey())
        .toByteArray();
    account.deviceSignature(Curve25519.sign(socketHandler.keys()
        .identityKeyPair()
        .privateKey(), deviceSignatureMessage, true));
    var keyIndex = PROTOBUF.readMessage(account.details(), DeviceIdentity.class)
        .keyIndex();
    var devicePairNode = ofChildren("pair-device-sign",
        Node.of("device-identity", of("key-index", keyIndex),
            PROTOBUF.writeValueAsBytes(account.withoutKey())));
    socketHandler.keys()
        .companionIdentity(account);
    sendConfirmNode(node, devicePairNode);
  }

  private void sendConfirmNode(Node node, Node content) {
    var attributes = Attributes.of()
        .put("id", node.id())
        .put("type", "result")
        .put("to", Server.WHATSAPP.toJid())
        .toMap();
    var request = ofChildren("iq", attributes, content);
    socketHandler.sendWithNoResponse(request);
  }

  private void saveCompanion(Node container) {
    var node = container.findNode("device")
        .orElseThrow(() -> new NoSuchElementException("Missing device"));
    var companion = node.attributes()
        .getJid("jid")
        .orElseThrow(() -> new NoSuchElementException("Missing companion"));
    socketHandler.store()
        .userCompanionJid(companion);
    socketHandler.store()
        .addContact(Contact.ofJid(socketHandler.store()
            .userCompanionJid()
            .toUserJid()));
  }

  @Override
  public void dispose() {
    super.dispose();
    retries.clear();
  }
}
