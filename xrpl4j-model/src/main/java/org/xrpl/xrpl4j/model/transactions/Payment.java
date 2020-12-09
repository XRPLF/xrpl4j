package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Flags.PaymentFlags;

import java.util.List;
import java.util.Optional;

/**
 * A Payment transaction represents a transfer of value from one account to another. (Depending on the path taken, this
 * can involve additional exchanges of value, which occur atomically.) This transaction type can be used for several
 * types of payments, including direct XRP-to-XRP payments, creating or redeeming issued currency, cross-currency
 * payments, partial payments, and currency conversions on the XRPL Decentralized Exchange.
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePayment.class)
@JsonDeserialize(as = ImmutablePayment.class)
public interface Payment extends Transaction {

  static ImmutablePayment.Builder builder() {
    return ImmutablePayment.builder();
  }

  /**
   * Set of {@link Flags.PaymentFlags}s for this {@link Payment}, which have been properly combined to yield a
   * {@link Flags} object containing the {@link Long} representation of the set bits.
   *
   * <p>The value of the flags can either be set manually, or constructed using {@link Flags.PaymentFlags.Builder}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default PaymentFlags flags() {
    return PaymentFlags.builder().tfFullyCanonicalSig(true).build();
  }

  /**
   * The amount of currency to deliver. If the {@link Flags.PaymentFlags#tfPartialPayment()} flag is set, deliver
   * up to this amount instead.
   */
  @JsonProperty("Amount")
  CurrencyAmount amount();

  /**
   * The unique {@link Address} of the account receiving the payment. Maybe be empty for an AccountSet or other
   * transaction that is not a payment.
   */
  @JsonProperty("Destination")
  Address destination();

  /**
   * Arbitrary {@link UnsignedInteger} tag that identifies the reason for the payment to the destination, or a hosted
   * recipient to pay.
   */
  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

  /**
   * Arbitrary 256-bit hash representing a specific reason or identifier for this payment.
   */
  @JsonProperty("InvoiceID")
  Optional<Hash256> invoiceId();

  /**
   * A {@link List} of {@link List}s of payment paths to be used for this transaction. Must be omitted for XRP-to-XRP
   * transactions.
   *
   * <p>This field is auto-fillable
   *
   * @see "https://xrpl.org/transaction-common-fields.html#auto-fillable-fields"
   */
  @JsonProperty("Paths")
  List<List<PathStep>> paths();

  /**
   * Highest amount of source currency this transaction is allowed to cost, including transfer fees, exchange rates, and
   * slippage. Does not include the XRP destroyed as a cost for submitting the transaction.
   *
   * <p>Must be supplied for cross-currency/cross-issue payments. Must be omitted for XRP-to-XRP payments.
   *
   * @see "https://xrpl.org/transfer-fees.html"
   * @see "https://en.wikipedia.org/wiki/Slippage_%28finance%29"
   */
  @JsonProperty("SendMax")
  Optional<CurrencyAmount> sendMax();

  /**
   * Minimum amount of destination currency this {@link Payment} should deliver. Only valid if this the {@link
   * Flags.PaymentFlags#tfPartialPayment()} flag is set.
   */
  @JsonProperty("DeliverMin")
  Optional<CurrencyAmount> deliverMin();

}
