package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;

/**
 * Return escrowed XRP to the sender.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableEscrowCancel.class)
@JsonDeserialize(as = ImmutableEscrowCancel.class)
public interface EscrowCancel extends Transaction {

  static ImmutableEscrowCancel.Builder builder() {
    return ImmutableEscrowCancel.builder();
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

}
