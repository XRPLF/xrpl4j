package com.ripple.xrpl4j.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSigner.class)
@JsonDeserialize(as = ImmutableSigner.class)
public interface Signer {

  static ImmutableSigner.Builder builder() {
    return ImmutableSigner.builder();
  }

  @JsonProperty("Account")
  Address account();

  @JsonProperty("TxnSignature")
  String transactionSignature();

  @JsonProperty("SigningPubKey")
  String signingPublicKey();

}
