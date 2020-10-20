package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

/**
 * A Payment transaction represents a transfer of value from one account to another. (Depending on the path taken,
 * this can involve additional exchanges of value, which occur atomically.) This transaction type can be used for
 * several types of payments, including direct XRP-to-XRP payments, creating or redeeming issued currency, cross-currency
 * payments, partial payments, and currency conversions on the XRPL Decentralized Exchange.
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePayment.class)
@JsonDeserialize(as = ImmutablePayment.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface Payment extends Transaction {

  static ImmutablePayment.Builder builder() {
    return ImmutablePayment.builder();
  }

  @Override
  @Value.Derived
  @JsonProperty("TransactionType")
  default TransactionType type() {
    return TransactionType.PAYMENT;
  }

  /**
   * The amount of currency to deliver. If the {@link Payment#tfPartialPayment()} flag is set, deliver up to
   * this amount instead.
   */
  @JsonProperty("Amount")
  CurrencyAmount amount();

  /**
   * The unique {@link Address} of the account receiving the payment.
   */
  @JsonProperty("Destination")
  Address destination();

  /**
   * Arbitrary {@link UnsignedInteger} tag that identifies the reason for the payment to the destination,
   * or a hosted recipient to pay.
   */
  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

  /**
   * Arbitrary 256-bit hash representing a specific reason or identifier for this payment.
   */
  @JsonProperty("InvoiceID")
  Optional<Hash256> invoiceId();

  /**
   * A {@link List} of {@link List}s of payment paths to be used for this transaction.
   * Must be omitted for XRP-to-XRP transactions.
   *
   * This field is auto-fillable
   * @see "https://xrpl.org/transaction-common-fields.html#auto-fillable-fields"
   */
  @JsonProperty("Paths")
  List<List<PathStep>> paths();

  /**
   * Highest amount of source currency this transaction is allowed to cost, including transfer fees,
   * exchange rates, and slippage. Does not include the XRP destroyed as a cost for submitting the transaction.
   *
   * Must be supplied for cross-currency/cross-issue payments. Must be omitted for XRP-to-XRP payments.
   *
   * @see "https://xrpl.org/transfer-fees.html"
   * @see "https://en.wikipedia.org/wiki/Slippage_%28finance%29"
   */
  @JsonProperty("SendMax")
  Optional<CurrencyAmount> sendMax();

  /**
   * Minimum amount of destination currency this {@link Payment} should deliver. Only valid if this the
   * {@link Payment#tfPartialPayment()} flag is set.
   */
  @JsonProperty("DeliverMin")
  Optional<CurrencyAmount> deliverMin();

  /**
   * Unique transaction hash/id. Set by rippled. Only present on payments that have been applied to the ledger.
   * @return
   */
  @JsonProperty("hash")
  Optional<String> hash();

  /**
   * Flag indicated to only use paths included in the {@link Payment#paths()} field.
   * This is intended to force the transaction to take arbitrage opportunities. Most clients do not need this.
   */
  @JsonIgnore
  @Value.Default
  default boolean tfNoDirectRipple() {
    return false;
  }

  /**
   * If the specified {@link Payment#amount()} cannot be sent without spending more than {@link Payment#sendMax()},
   * reduce the received amount instead of failing outright.
   *
   * @see "https://xrpl.org/partial-payments.html"
   */
  @JsonIgnore
  @Value.Default
  default boolean tfPartialPayment() {
    return false;
  }

  /**
   * Only take paths where all the conversions have an input:output ratio that is equal or better than the ratio of
   * {@link Payment#amount()}:{@link Payment#sendMax()}.
   * @return
   */
  @JsonIgnore
  @Value.Default
  default boolean tfLimitQuality() {
    return false;
  }

  /**
   * {@link Payment#flags()} must be constructed from the set of boolean flags in this transaction. However, because
   * the individual flags are not present in the JSON representation of this {@link Payment}, when deserializing
   * JSON to a {@link Payment}, we must be able to derive the boolean flags from the {@link Payment#flags()} field.
   *
   * The best way to do this with a {@link Value.Immutable} object like {@link Payment} is to normalize the object
   * after it has been initially constructed.  Without this flag, there would be no way to determine if
   * this normalization has already occurred, and an infinite recursive loop would occur during normalization.
   */
  @JsonIgnore
  @Value.Default
  default boolean flagsConstructed() {
    return false;
  }

  /**
   * Constructs {@link Payment#flags()} from {@link Payment#tfFullyCanonicalSig()}, {@link Payment#tfNoDirectRipple()},
   * {@link Payment#tfPartialPayment()}, and {@link Payment#tfLimitQuality()} if {@link Payment#flags()} is empty.
   * Otherwise, the individual boolean flags are derived from {@link Payment#flags()}
   */
  @Value.Check
  default Payment constructFlags() {
    // Avoid infinite recursion by tagging that the object has gone through this transformation
    if (flagsConstructed()) {
      return this;
    }

    // If flags() is null, then this Payment wasn't constructed from JSON, so we need to derive flags()
    // from the boolean switches
    if (!flags().isPresent()) {
      Flags flags = (tfFullyCanonicalSig() ? Flags.Universal.FULLY_CANONICAL_SIG : Flags.UNSET)
        .bitwiseOr((tfNoDirectRipple() ? Flags.Payment.NO_DIRECT_RIPPLE : Flags.UNSET)
          .bitwiseOr(
            (tfPartialPayment() ? Flags.Payment.PARTIAL_PAYMENT : Flags.UNSET))
          .bitwiseOr(
            (tfLimitQuality() ? Flags.Payment.LIMIT_QUALITY : Flags.UNSET)));

      return Payment.builder()
        .from(this)
        .flags(flags)
        .flagsConstructed(true)
        .build();
    }

    // Otherwise, we know flags() from JSON, so we can derive the boolean switches from the flags
    else {
      return Payment.builder()
        .from(this)
        .tfFullyCanonicalSig(flags().get().isSet(Flags.Universal.FULLY_CANONICAL_SIG))
        .tfNoDirectRipple(flags().get().isSet(Flags.Payment.NO_DIRECT_RIPPLE))
        .tfPartialPayment(flags().get().isSet(Flags.Payment.PARTIAL_PAYMENT))
        .tfLimitQuality(flags().get().isSet(Flags.Payment.LIMIT_QUALITY))
        .flagsConstructed(true)
        .build();
    }
  }
}
