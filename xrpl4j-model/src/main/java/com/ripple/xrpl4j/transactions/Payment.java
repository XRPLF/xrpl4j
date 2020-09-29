package com.ripple.xrpl4j.transactions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePayment.class)
@JsonDeserialize(as = ImmutablePayment.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public interface Payment {

  static ImmutablePayment.Builder builder() {
    return ImmutablePayment.builder();
  }

  @JsonProperty("Account")
  Address account();

  @Value.Derived
  @JsonProperty("TransactionType")
  default TransactionType type() {
    return TransactionType.PAYMENT;
  };

  @JsonProperty("Fee")
  String fee();

  @JsonProperty("Sequence")
  Optional<UnsignedInteger> sequence();

  @JsonProperty("AccountTxnID")
  Optional<Hash256> accountTransactionId();

  @JsonProperty("Flags")
  Optional<Flags> flags();

  @JsonProperty("LastLedgerSequence")
  Optional<UnsignedInteger> lastLedgerSequence();

  @JsonProperty("Memos")
  List<MemoWrapper> memos();

  @JsonProperty("Signers")
  List<SignerWrapper> signers();

  @JsonProperty("SourceTag")
  Optional<UnsignedInteger> sourceTag();

  @JsonProperty("SigningPubKey")
  Optional<String> signingPublicKey();

  @JsonProperty("TxnSignature")
  Optional<String> transactionSignature();

  @JsonProperty("Amount")
  CurrencyAmount amount();

  @JsonProperty("Destination")
  Address destination();

  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

  @JsonProperty("InvoiceID")
  Optional<Hash256> invoiceId();

  @JsonProperty("Paths")
  List<List<Path>> paths();

  @JsonProperty("SendMax")
  Optional<CurrencyAmount> sendMax();

  @JsonProperty("DeliverMin")
  Optional<CurrencyAmount> deliverMin();

  @JsonIgnore
  @Value.Default
  default boolean tfFullyCanonicalSig() {
    return true;
  };

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

  @JsonIgnore
  @Value.Default
  default boolean flagsConstructed() {
    return false;
  }

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
