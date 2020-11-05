package com.ripple.xrplj4.client.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.CurrencyAmount;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.Hash256;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableCheck.class)
@JsonDeserialize(as = ImmutableCheck.class)
public interface Check extends LedgerObject {

  static ImmutableCheck.Builder builder() {
    return ImmutableCheck.builder();
  }

  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default String ledgerEntryType() {
    return "Check";
  }

  @JsonProperty("Account")
  Address account();

  @JsonProperty("SourceTag")
  Optional<UnsignedInteger> sourceTag();

  @JsonProperty("Destination")
  Address destination();

  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

  /**
   *
   * No flags are defined for {@link Check}, so this value is always 0.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags flags() {
    return Flags.UNSET;
  }

  @JsonProperty("OwnerNode")
  String ownerNode();

  @JsonProperty("PreviousTxnID")
  Hash256 previousTxnId();

  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  @JsonProperty("SendMax")
  CurrencyAmount sendMax();

  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  @JsonProperty("DestinationNode")
  Optional<String> destinationNode();

  @JsonProperty("Expiration")
  Optional<UnsignedInteger> expiration();

  @JsonProperty("InvoiceID")
  Optional<Hash256> invoiceId();

  Hash256 index();
}
