package it.auties.whatsapp.manager;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.auties.whatsapp.binary.BinaryArray;
import it.auties.whatsapp.crypto.CipherHelper;
import it.auties.whatsapp.protobuf.contact.ContactId;
import it.auties.whatsapp.protobuf.group.SenderKeyName;
import it.auties.whatsapp.protobuf.group.SenderKeyRecord;
import it.auties.whatsapp.protobuf.group.SenderKeyStructure;
import it.auties.whatsapp.protobuf.key.IdentityKeyPair;
import it.auties.whatsapp.protobuf.key.SignedKeyPair;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * This class is a data class used to hold the clientId, serverToken, clientToken, publicKey, privateKey, encryptionKey and macKey.
 * It can be serialized using Jackson and deserialized using the fromPreferences named constructor.
 */
@Data
@Accessors(fluent = true, chain = true)
public class WhatsappKeys {
    /**
     * The path used to serialize and deserialize this object
     */
    public static final String PREFERENCES_PATH = WhatsappKeys.class.getName();

    /**
     * An instance of Jackson
     */
    private static final ObjectMapper JACKSON = new ObjectMapper();

    /**
     * The client id
     */
    @JsonProperty
    private int id;

    /**
     * The secret key pair used for buffer messages
     */
    @JsonProperty
    private @NonNull IdentityKeyPair companionKeyPair;

    /**
     * The ephemeral key pair
     */
    @JsonProperty
    private @NonNull IdentityKeyPair ephemeralKeyPair;

    /**
     * The signed identity key
     */
    @JsonProperty
    private @NonNull IdentityKeyPair identityKeyPair;

    /**
     * The signed pre key
     */
    @JsonProperty
    private @NonNull SignedKeyPair signedKeyPair;

    /**
     * The adv secret key
     */
    @JsonProperty
    private byte @NonNull [] companionKey;

    /**
     * The user using these keys
     */
    @JsonProperty
    private ContactId companion;

    /**
     * Whether these keys have generated pre keys assigned to them
     */
    @JsonProperty
    private boolean preKeys;

    /**
     * Session dependent keys to write and read cyphered messages
     */
    private BinaryArray writeKey, readKey;

    /**
     * Sender keys for signal implementation
     */
    private List<SenderKeyStructure> senderKeyStructures;

    /**
     * Returns the keys saved in memory or constructs a new clean instance
     *
     * @return a non-null instance of WhatsappKeys
     */
    public static WhatsappKeys fromMemory(){
        return Optional.ofNullable(Preferences.userRoot().get(PREFERENCES_PATH, null))
                .map(WhatsappKeys::deserializeOrThrow)
                .orElse(new WhatsappKeys());
    }

    @SneakyThrows
    private static WhatsappKeys deserializeOrThrow(String json) {
        try {
            return JACKSON.readValue(json, WhatsappKeys.class);
        }catch (Throwable throwable){
            Preferences.userRoot().clear();
            return null;
        }
    }

    public WhatsappKeys() {
        this.id = CipherHelper.randomRegistrationId();
        this.companionKeyPair = CipherHelper.randomKeyPair();
        this.ephemeralKeyPair = CipherHelper.randomKeyPair();
        this.identityKeyPair = CipherHelper.randomKeyPair();
        this.signedKeyPair = CipherHelper.randomKeyPair(id, identityKeyPair());
        this.companionKey = CipherHelper.randomSenderKey();
        this.senderKeyStructures = new ArrayList<>();
    }

    /**
     * Serializes this object to a json and saves it in memory
     */
    public void save(){
        try {
            Preferences.userRoot().put(PREFERENCES_PATH, JACKSON.writeValueAsString(this));
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("Cannot save keys to memory", exception);
        }
    }

    /**
     * Clears the signal keys associated with this object
     */
    public void clear() {
        writeKey(null).readKey(null);
    }

    /**
     * Clears all the keys from this machine's memory.
     * This method doesn't clear this object's values.
     */
    public void delete() {
        try {
            Preferences.userRoot().clear();
        }catch (BackingStoreException exception){
            throw new RuntimeException("Cannot delete keys from memory", exception);
        }
    }

    public void initializeKeys(@NonNull BinaryArray writeKey, @NonNull BinaryArray readKey){
        readKey(readKey).writeKey(writeKey).save();
    }

    /**
     * Checks if the serverToken and clientToken are not null
     *
     * @return true if both the serverToken and clientToken are not null
     */
    public boolean hasCompanion() {
        return companion != null;
    }

    /**
     * Queries the first {@link SenderKeyRecord} that matches {@code name}
     *
     * @param name the non-null name to search
     * @return a non-null SenderKeyRecord
     * @throws NoSuchElementException if no element can be found
     */
    public SenderKeyRecord findSenderKeyByName(@NonNull SenderKeyName name){
        return senderKeyStructures.stream()
                .filter(structure -> Objects.equals(structure.name(), name))
                .findFirst()
                .map(SenderKeyStructure::record)
                .orElseThrow(() -> new NoSuchElementException("Cannot find sender key for name %s".formatted(name)));
    }
}