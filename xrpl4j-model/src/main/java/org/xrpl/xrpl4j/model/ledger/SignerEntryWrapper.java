package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSignerEntryWrapper.class)
@JsonDeserialize(as = ImmutableSignerEntryWrapper.class)
public interface SignerEntryWrapper {

  static SignerEntryWrapper of(SignerEntry entry) {
    return ImmutableSignerEntryWrapper.builder()
        .signerEntry(entry)
        .build();
  }

  @JsonProperty("SignerEntry")
  SignerEntry signerEntry();

}
