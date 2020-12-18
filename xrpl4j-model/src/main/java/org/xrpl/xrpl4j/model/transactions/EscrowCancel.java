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

  /**
   * Set of {@link Flags.TransactionFlags}s for this {@link EscrowCancel}, which only allows {@code tfFullyCanonicalSig}
   * flag.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   *
   * @return Always {@link Flags.TransactionFlags} with {@code tfFullyCanonicalSig} set.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags.TransactionFlags flags() {
    return new Flags.TransactionFlags.Builder().tfFullyCanonicalSig(true).build();
  }

  /**
   * {@link Address} of the source account that funded the escrow payment.
   *
   * @return The {@link Address} of the escrow owner.
   */
  @JsonProperty("Owner")
  Address owner();

  /**
   * The {@link EscrowCreate#sequence()} of the transaction that created the escrow to cancel.
   *
   * @return An {@link UnsignedInteger} representing the sequence of the {@link EscrowCreate} transaction that created
   *     the escrow.
   */
  @JsonProperty("OfferSequence")
  UnsignedInteger offerSequence();

}
