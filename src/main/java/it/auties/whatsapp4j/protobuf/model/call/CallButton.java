package it.auties.whatsapp4j.protobuf.model.call;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.auties.whatsapp4j.protobuf.message.business.HighlyStructuredMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Accessors(fluent = true)
public class CallButton {
  @JsonProperty(value = "2")
  private HighlyStructuredMessage phoneNumber;

  @JsonProperty(value = "1")
  private HighlyStructuredMessage displayText;
}