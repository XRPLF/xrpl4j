package com.ripple.xrpl4j.transactions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;


@JsonSerialize(as = ImmutablePayment.class)
@JsonDeserialize(as = ImmutablePayment.class)
public interface Payment extends Transaction {

  static ImmutablePayment.Builder builder() {
    return ImmutablePayment.builder();
  }

  @JsonProperty("Amount")
  CurrencyAmount amount();

  @JsonProperty("Destination")
  Address destination();

  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

  @JsonProperty("InvoiceID")
  Optional<Hash256> invoiceId();

  @Override
  @Value.Derived
  @JsonIgnore
  default Flags transactionFlags() {
    return (tfNoDirectRipple() ? Flags.Payment.NO_RIPPLE_DIRECT : Flags.UNSET)
      .bitwiseOr(
        (tfPartialPayment() ? Flags.Payment.PARTIAL_PAYMENT : Flags.UNSET))
      .bitwiseOr(
        (tfLimitQuality() ? Flags.Payment.LIMIT_QUALITY : Flags.UNSET));
  }

  @JsonIgnore
  @Value.Default
  default boolean tfNoDirectRipple() {
    return false;
  }

  @JsonIgnore
  @Value.Default
  default boolean tfPartialPayment() {
    return false;
  }

  @JsonIgnore
  @Value.Default
  default boolean tfLimitQuality() {
    return false;
  }

  @JsonProperty("Paths")
  List<List<Path>> paths();

  @JsonProperty("SendMax")
  Optional<CurrencyAmount> sendMax();

  @JsonProperty("DeliverMin")
  Optional<CurrencyAmount> deliverMin();

  @Value.Immutable
  abstract class AbstractPayment implements Payment {

    /**
     * Ensures that sendMax() is present for cross-currency/cross-issue payments,
     * and empty for XRP-to-XRP payments.
     *
     * @see "https://xrpl.org/payment.html#payment-fields"
     */
    @Value.Check
    public void validateSendMax() {
      if (isXrpToXrpPayment()) {
        Preconditions.checkArgument(!sendMax().isPresent(), "SendMax cannot be set for an XRP-to-XRP payment.");
      } else {
        Preconditions.checkArgument(sendMax().isPresent(), "SendMax must be set for an issued currency or cross-currency payment");
      }
    }

    /**
     * Validates that deliverMin() is only present if this is a partial payment.
     *
     * @see "https://xrpl.org/payment.html#payment-fields"
     */
    @Value.Check
    public void validateDeliverMinOnlyForPartialPayment() {
      if (deliverMin().isPresent()) {
        Preconditions.checkArgument(tfPartialPayment(), "DeliverMin is only valid for partial payments.");
      }
    }

    @Value.Check
    public void validateAccountAndDestinationAreDifferent() {
      Preconditions.checkArgument(account() != destination(), "Sender and receiver addresses cannot be the same.");
    }

    @Value.Check
    public void validateNoPathsForXrpToXrp() {
      if (isXrpToXrpPayment()) {
        Preconditions.checkArgument(paths().size() == 0, "Cannot specify paths for XRP to XRP payments.");
      }
    }

    private boolean isXrpToXrpPayment() {
      return XrpCurrencyAmount.class.isAssignableFrom(amount().getClass());
    }
  }


}
