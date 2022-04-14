package it.auties.whatsapp.model.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import it.auties.protobuf.api.model.ProtobufProperty;
import it.auties.whatsapp.api.Whatsapp;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import java.util.Arrays;

import static it.auties.protobuf.api.model.ProtobufProperty.Type.*;

/**
 * A model class that holds the information related to an companion reply.
 * This class is only a model, this means that changing its values will have no real effect on WhatsappWeb's servers.
 * Instead, methods inside {@link Whatsapp} should be used.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Jacksonized
@Builder(builderMethodName = "newAdReplyInfo", buildMethodName = "create")
@Accessors(fluent = true)
public final class AdReplyInfo implements Info {
  /**
   * The name of the advertiser that served the original companion
   */
  @ProtobufProperty(index = 1, type = STRING)
  private String advertiserName;

  /**
   * The type of original companion
   */
  @ProtobufProperty(index = 2, type = MESSAGE, concreteType = AdReplyInfoMediaType.class)
  private AdReplyInfoMediaType mediaType;

  /**
   * The thumbnail of the original companion encoded as jpeg in an array of bytes
   */
  @ProtobufProperty(index = 16, type = BYTES)
  private byte[] thumbnail;

  /**
   * The caption of the original companion
   */
  @ProtobufProperty(index = 17, type = STRING)
  private String caption;

  /**
   * The constants of this enumerated type describe the various types of companion that a {@link AdReplyInfo} can link to
   */
  @AllArgsConstructor
  @Accessors(fluent = true)
  public enum AdReplyInfoMediaType {
    /**
     * Unknown type
     */
    NONE(0),

    /**
     * Image type
     */
    IMAGE(1),

    /**
     * Video type
     */
    VIDEO(2);

    @Getter
    private final int index;

    @JsonCreator
    public static AdReplyInfoMediaType forIndex(int index) {
      return Arrays.stream(values())
          .filter(entry -> entry.index() == index)
          .findFirst()
          .orElse(null);
    }
  }
}
