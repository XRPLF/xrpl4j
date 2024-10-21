package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * Clawback an issued currency that exists on a Trustline.
 *
 * <p>This class will be marked {@link Beta} until the Clawback amendment is enabled on mainnet. Its API is subject
 * to change.</p>
 */
@Immutable
@JsonSerialize(as = ImmutableClawback.class)
@JsonDeserialize(as = ImmutableClawback.class)
@Beta
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
  CurrencyAmount amount();

  @JsonProperty("MPTokenHolder")
  Optional<Address> mpTokenHolder();

  // TODO: I don't love that this is now a CurrencyAmount, because it can only ever be an IssuedCurrencyAmount
  //  or MptAmount, and mpTokenHolder can only be present when amount is an MptAmount.
  //  This might work, but we'd need to do some wonky jackson stuff to make it work
  /*@JsonUnwrapped
  ClawbackAmount amount();

  interface ClawbackAmount {}

  @Immutable
  @JsonSerialize(as = ImmutableIssuedCurrencyClawbackAmount.class)
  @JsonDeserialize(as = ImmutableIssuedCurrencyClawbackAmount.class)
  interface IssuedCurrencyClawbackAmount extends ClawbackAmount {
    IssuedCurrencyAmount amount();
  }

  interface MptClawbackAmount extends ClawbackAmount {
    MpTokenAmount amount();

    Optional<Address> mpTokenHolder();
  }*/
}
