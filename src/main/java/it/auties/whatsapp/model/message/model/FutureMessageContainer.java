package it.auties.whatsapp.model.message.model;

import static it.auties.protobuf.base.ProtobufType.MESSAGE;

import it.auties.protobuf.base.ProtobufMessage;
import it.auties.protobuf.base.ProtobufName;
import it.auties.protobuf.base.ProtobufProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@AllArgsConstructor
@Builder
@Accessors(fluent = true)
@Jacksonized
@ProtobufName("FutureProofMessage")
class FutureMessageContainer implements ProtobufMessage {
  @ProtobufProperty(index = 1, type = MESSAGE, implementation = MessageContainer.class)
  @Getter(AccessLevel.PROTECTED)
  private MessageContainer content;

  protected static FutureMessageContainer of(Message message) {
    return new FutureMessageContainer(MessageContainer.of(message));
  }

  protected static FutureMessageContainer of(MessageContainer container) {
    return new FutureMessageContainer(container);
  }

  protected Message unbox() {
    return content.content();
  }
}