package com.ripple.xrplj4.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSignerEntry.class)
@JsonDeserialize(as = ImmutableSignerEntry.class)
public interface SignerEntry {

  static ImmutableSignerEntry.Builder builder() {
    return ImmutableSignerEntry.builder();
  }

  @JsonProperty("Account")
  Address account();

  @JsonProperty("SignerWeight")
  UnsignedInteger signerWeight();

}
