package org.xrpl.xrpl4j.model.client.fees;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.XrplResult;

import java.util.Optional;

/**
 * The result of a fee rippled API call, which reports the current state of the open-ledger requirements
 * for the transaction cost.
 */
@Immutable
@JsonSerialize(as = ImmutableFeeResult.class)
@JsonDeserialize(as = ImmutableFeeResult.class)
public interface FeeResult extends XrplResult {

  static ImmutableFeeResult.Builder builder() {
    return ImmutableFeeResult.builder();
  }

  /**
   * Number of transactions provisionally included in the in-progress ledger.
   */
  @JsonProperty("current_ledger_size")
  String currentLedgerSize();

  /**
   * Number of transactions currently queued for the next ledger.
   */
  @JsonProperty("current_queue_size")
  String currentQueueSize();

  /**
   * Various information about the transaction cost in drops of XRP.
   */
  FeeDrops drops();

  /**
   * The approximate number of transactions expected to be included in the current ledger.
   * This is based on the number of transactions in the previous ledger.
   */
  @JsonProperty("expected_ledger_size")
  String expectedLedgerSize();

  /**
   * The Ledger Index of the current open ledger these stats describe.
   */
  @JsonProperty("ledger_current_index")
  LedgerIndex ledgerCurrentIndex();

  /**
   * Various information about the transaction cost, in
   * <a href="https://xrpl.org/transaction-cost.html#fee-levels">fee levels</a>. The ratio in fee
   * levels applies to any transaction relative to the minimum cost of that particular transaction.
   */
  FeeLevels levels();

  /**
   * The maximum number of transactions that the transaction queue can currently hold.
   * Optional because this may not be present on older versions of rippled.
   */
  @JsonProperty("max_queue_size")
  Optional<String> maxQueueSize();

}
