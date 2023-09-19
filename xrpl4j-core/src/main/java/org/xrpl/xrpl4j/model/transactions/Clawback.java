package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

/**
 * Clawback an issued currency that exists on a Trustline.
 */
@Immutable
@JsonSerialize(as = ImmutableClawback.class)
@JsonDeserialize(as = ImmutableClawback.class)
public interface Clawback extends Transaction {

  /**
   * Construct a {@code Clawback} builder.
   *
   * @return An {@link ImmutableClawback.Builder}.
   */
  static ImmutableClawback.Builder builder() {
    return ImmutableClawback.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link Clawback}, which only allows the
   * {@code tfFullyCanonicalSig} flag, which is deprecated.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * Indicates the amount being clawed back, as well as the counterparty from which the amount is being clawed back
   * from. This amount must not exceed the holder's balance and must be greater than zero. The issuer in this amount
   * must not be the same as the source account of this transaction.
   *
   * @return An {@link IssuedCurrencyAmount} indicating the amount to clawback.
   */
  @JsonProperty("Amount")
  IssuedCurrencyAmount amount();


}
