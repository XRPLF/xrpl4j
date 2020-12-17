package org.xrpl.xrpl4j.model.client.fees;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * A sub-object of {@link FeeResult} containing various information about the transaction cost, in drops of XRP,
 * for the current open ledger.
 */
@Immutable
@JsonSerialize(as = ImmutableFeeDrops.class)
@JsonDeserialize(as = ImmutableFeeDrops.class)
public interface FeeDrops {

  static ImmutableFeeDrops.Builder builder() {
    return ImmutableFeeDrops.builder();
  }

  /**
   * The transaction cost required for a reference transaction to be included in a ledger under minimum load,
   * represented in drops of XRP.
   *
   * @return An {@link XrpCurrencyAmount} representing the base fee.
   */
  @JsonProperty("base_fee")
  XrpCurrencyAmount baseFee();

  /**
   * The minimum transaction cost for a reference transaction to be queued for a later ledger, represented
   * in drops of XRP. If greater than {@code base_fee}, the transaction queue is full.
   *
   * @return An {@link XrpCurrencyAmount} representing the minimum fee.
   */
  @JsonProperty("minimum_fee")
  XrpCurrencyAmount minimumFee();

  /**
   * An approximation of the median transaction cost among transactions included in the previous
   * validated ledger, represented in drops of XRP.
   *
   * @return An {@link XrpCurrencyAmount} representing the median fee.
   */
  @JsonProperty("median_fee")
  XrpCurrencyAmount medianFee();

  /**
   * The minimum transaction cost that a reference transaction must pay to be included in the current
   * open ledger, represented in drops of XRP.
   *
   * @return An {@link XrpCurrencyAmount} representing the open ledger fee.
   */
  @JsonProperty("open_ledger_fee")
  XrpCurrencyAmount openLedgerFee();
}
