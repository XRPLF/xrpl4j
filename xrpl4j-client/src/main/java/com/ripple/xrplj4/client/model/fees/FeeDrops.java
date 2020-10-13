package com.ripple.xrplj4.client.model.fees;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.transactions.XrpCurrencyAmount;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableFeeDrops.class)
@JsonDeserialize(as = ImmutableFeeDrops.class)
public interface FeeDrops {

  static ImmutableFeeDrops.Builder builder() {
    return ImmutableFeeDrops.builder();
  }

  @JsonProperty("base_fee")
  XrpCurrencyAmount baseFee();

  @JsonProperty("minimum_fee")
  XrpCurrencyAmount minimumFee();

  @JsonProperty("median_fee")
  XrpCurrencyAmount medianFee();

  @JsonProperty("open_ledger_fee")
  XrpCurrencyAmount openLedgerFee();
}
