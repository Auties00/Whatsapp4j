package it.auties.whatsapp.protobuf.signal.session;

import com.fasterxml.jackson.annotation.*;
import java.util.*;
import lombok.*;
import lombok.experimental.Accessors;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Accessors(fluent = true)
public class FutureProofMessage {

  @JsonProperty(value = "1", required = false)
  @JsonPropertyDescription("Message")
  private Message message;
}
