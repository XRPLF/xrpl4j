package org.xrpl.xrpl4j.model.client.fees;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * A sub-object of {@link FeeResult} containing various information about the transaction cost in fee levels
 * for the current open ledger.
 *
 * <p>The ratio in fee levels applies to any transaction relative to the minimum cost of that particular transaction.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableFeeLevels.class)
@JsonDeserialize(as = ImmutableFeeLevels.class)
public interface FeeLevels {

  static ImmutableFeeLevels.Builder builder() {
    return ImmutableFeeLevels.builder();
  }

  /**
   * The median transaction cost among transactions in the previous validated ledger, represented in fee levels.
   *
   * @return An {@link XrpCurrencyAmount} representing the median level.
   */
  @JsonProperty("median_level")
  XrpCurrencyAmount medianLevel();

  /**
   * The minimum transaction cost required to be queued for a future ledger, represented in fee levels.
   *
   * @return An {@link XrpCurrencyAmount} representing the minimum level.
   */
  @JsonProperty("minimum_level")
  XrpCurrencyAmount minimumLevel();

  /**
   * The minimum transaction cost required to be included in the current open ledger, represented in fee levels.
   *
   * @return An {@link XrpCurrencyAmount} representing the open ledger level.
   */
  @JsonProperty("open_ledger_level")
  XrpCurrencyAmount openLedgerLevel();

  /**
   * The equivalent of the minimum transaction cost, represented in fee levels.
   *
   * @return An {@link XrpCurrencyAmount} representing the reference level.
   */
  @JsonProperty("reference_level")
  XrpCurrencyAmount referenceLevel();

}
