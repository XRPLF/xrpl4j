package com.ripple.xrpl4j.xrplj4.client.model.fees;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * A sub-object of {@link FeeResult} containing various information about the transaction cost in fee levels
 * for the current open ledger.
 *
 * The ratio in fee levels applies to any transaction relative to the minimum cost of that particular transaction.
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
   */
  @JsonProperty("median_level")
  String medianLevel();

  /**
   * The minimum transaction cost required to be queued for a future ledger, represented in fee levels.
   */
  @JsonProperty("minimum_level")
  String minimumLevel();

  /**
   * The minimum transaction cost required to be included in the current open ledger, represented in fee levels.
   */
  @JsonProperty("open_ledger_level")
  String openLedgerLevel();

  /**
   * The equivalent of the minimum transaction cost, represented in fee levels.
   */
  @JsonProperty("reference_level")
  String referenceLevel();

}
