package it.auties.whatsapp.protobuf.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import it.auties.whatsapp.protobuf.sync.SyncActionMessageRange;
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
public class MarkChatAsReadAction {
  @JsonProperty("2")
  @JsonPropertyDescription("SyncActionMessageRange")
  private SyncActionMessageRange messageRange;

  @JsonProperty("1")
  @JsonPropertyDescription("bool")
  private boolean read;
}