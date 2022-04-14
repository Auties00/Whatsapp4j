package it.auties.whatsapp.model.signal.session;

import com.fasterxml.jackson.annotation.*;
import java.util.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@AllArgsConstructor
@Data
@Builder
@Jacksonized
@Accessors(fluent = true)
public class Money {

  @JsonProperty(value = "3", required = false)
  @JsonPropertyDescription("string")
  private String currencyCode;

  @JsonProperty(value = "2", required = false)
  @JsonPropertyDescription("uint32")
  private int offset;

  @JsonProperty(value = "1", required = false)
  @JsonPropertyDescription("int64")
  private long value;
}
