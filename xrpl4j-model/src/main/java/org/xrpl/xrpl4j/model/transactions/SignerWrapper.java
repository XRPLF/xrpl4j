package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSignerWrapper.class)
@JsonDeserialize(as = ImmutableSignerWrapper.class)
public interface SignerWrapper {

  static ImmutableSignerWrapper.Builder builder() {
    return ImmutableSignerWrapper.builder();
  }

  static SignerWrapper of(Signer signer) {
    return builder().signer(signer).build();
  }

  @JsonProperty("Signer")
  Signer signer();
}
