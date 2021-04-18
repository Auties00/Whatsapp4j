package it.auties.whatsapp4j.response.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.auties.whatsapp4j.response.model.JsonResponseModel;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;


import java.util.List;
import java.util.Objects;

/**
 * A json model that contains information about an update about the metadata of a WhatsappChat
 * @param jid  the jid of the WhatsappChat this update regards
 * @param cmd  a nullable String used to describe the update
 * @param data a list of objects that represent the encoded update
 */
public record ChatCmdResponse(@JsonProperty("id") @NotNull String jid, String cmd,
                              @NotNull List<Object> data) implements JsonResponseModel {

}