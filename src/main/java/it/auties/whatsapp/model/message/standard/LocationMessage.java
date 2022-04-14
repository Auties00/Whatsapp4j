package it.auties.whatsapp.model.message.standard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import it.auties.protobuf.api.model.ProtobufProperty;
import it.auties.whatsapp.api.Whatsapp;
import it.auties.whatsapp.model.message.model.ContextualMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import static it.auties.protobuf.api.model.ProtobufProperty.Type.*;

/**
 * A model class that represents a WhatsappMessage sent by a contact and that holds a location inside.
 * This class is only a model, this means that changing its values will have no real effect on WhatsappWeb's servers.
 * Instead, methods inside {@link Whatsapp} should be used.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(builderMethodName = "newLocationMessage", buildMethodName = "create")
@Jacksonized
@Accessors(fluent = true)
public final class LocationMessage extends ContextualMessage {
  /**
   * The latitude of the location that this message wraps
   */
  @ProtobufProperty(index = 1, type = DOUBLE)
  private double latitude;

  /**
   * The longitude of the location that this message wraps
   */
  @ProtobufProperty(index = 2, type = DOUBLE)
  private double longitude;

  /**
   * The name of the location that this message wraps
   */
  @ProtobufProperty(index = 3, type = STRING)
  private String name;

  /**
   * The address of the location that this message wraps
   */
  @ProtobufProperty(index = 4, type = STRING)
  private String address;

  /**
   * A URL to visit the location that this message wraps in Google Maps
   */
  @ProtobufProperty(index = 5, type = STRING)
  private String url;

  /**
   * Determines whether this message is a {@link LiveLocationMessage}
   */
  @ProtobufProperty(index = 6, type = BOOLEAN)
  private boolean live;

  /**
   * The accuracy in meters of the location that this message wraps
   */
  @ProtobufProperty(index = 7, type = UINT32)
  private int accuracy;

  /**
   * The speed in meters per second of the device that sent this live location message
   */
  @ProtobufProperty(index = 8, type = FLOAT)
  private float speed;

  /**
   * Degrees Clockwise from Magnetic North
   */
  @ProtobufProperty(index = 9, type = UINT32)
  private int magneticNorthOffset;

  /**
   * The caption of this message
   */
  @ProtobufProperty(index = 11, type = STRING)
  private String caption;

  /**
   * The thumbnail for this image message encoded as jpeg in an array of bytes
   */
  @ProtobufProperty(index = 16, type = BYTES)
  private byte[] thumbnail;
}
