package com.ripple.xrpl4j.transactions;

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

  @JsonProperty("Signer")
  Signer signer();
}
