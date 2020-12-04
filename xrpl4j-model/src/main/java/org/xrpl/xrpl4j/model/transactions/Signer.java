package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Represents a signer for a multi-signature XRPL Transaction.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSigner.class)
@JsonDeserialize(as = ImmutableSigner.class)
public interface Signer {

  static ImmutableSigner.Builder builder() {
    return ImmutableSigner.builder();
  }

  /**
   * The {@link Address} associated with this signature, as it appears in the signer list.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * A signature for a transaction, verifiable using the {@link Signer#signingPublicKey()}.
   */
  @JsonProperty("TxnSignature")
  String transactionSignature();

  /**
   * The public key used to create this signature.
   */
  @JsonProperty("SigningPubKey")
  String signingPublicKey();

}
