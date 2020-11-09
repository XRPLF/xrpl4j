package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Deliver XRP from a held payment to the recipient.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableEscrowFinish.class)
@JsonDeserialize(as = ImmutableEscrowFinish.class)
public interface EscrowFinish extends Transaction<Flags.TransactionFlags> {

  static ImmutableEscrowFinish.Builder builder() {
    return ImmutableEscrowFinish.builder();
  }

  @JsonProperty("Flags")
  @Value.Derived
  default Flags.TransactionFlags flags() {
    return new Flags.TransactionFlags.Builder().fullyCanonicalSig(true).build();
  }

  /**
   * {@link Address} of the source account that funded the escrow payment.
   */
  @JsonProperty("Owner")
  Address owner();

  /**
   * The {@link EscrowCreate#sequence()} of the transaction that created the escrow to cancel.
   */
  @JsonProperty("OfferSequence")
  UnsignedInteger offerSequence();

  /**
   * Hex value matching the previously-supplied PREIMAGE-SHA-256 crypto-condition of the held payment.
   */
  @JsonProperty("Condition")
  Optional<String> condition();

  /**
   * Hex value of the PREIMAGE-SHA-256 crypto-condition fulfillment matching the held payment's {@code condition}.
   */
  @JsonProperty("Fulfillment")
  Optional<String> fulfillment();

}
