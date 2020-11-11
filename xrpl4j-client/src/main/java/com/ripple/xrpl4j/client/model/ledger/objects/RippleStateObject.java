package com.ripple.xrpl4j.client.model.ledger.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.Hash256;
import com.ripple.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableRippleStateObject.class)
@JsonDeserialize(as = ImmutableRippleStateObject.class)
public interface RippleStateObject extends LedgerObject {

  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.RIPPLE_STATE;
  }

  @JsonProperty("Flags")
  Flags.RippleStateFlags flags();

  @JsonProperty("Balance")
  IssuedCurrencyAmount balance();

  @JsonProperty("LowLimit")
  IssuedCurrencyAmount lowLimit();

  @JsonProperty("HighLimit")
  IssuedCurrencyAmount highLimit();

  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  @JsonProperty("LowNode")
  Optional<String> lowNode();

  @JsonProperty("HighNode")
  Optional<String> highNode();

  @JsonProperty("LowQualityIn")
  Optional<UnsignedInteger> lowQualityIn();

  @JsonProperty("LowQualityOut")
  Optional<UnsignedInteger> lowQualityOut();

  @JsonProperty("HighQualityIn")
  Optional<UnsignedInteger> highQualityIn();

  @JsonProperty("HighQualityOut")
  Optional<UnsignedInteger> highQualityOut();

  String index();
}
