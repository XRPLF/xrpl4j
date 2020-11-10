package com.ripple.xrpl4j.xrplj4.client.model.fees;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.jackson.modules.LedgerIndexSerializer;
import com.ripple.xrpl4j.xrplj4.client.model.JsonRpcResult;
import org.immutables.value.Value.Immutable;

/**
 * The result of a fee rippled API call, which reports the current state of the open-ledger requirements
 * for the transaction cost.
 */
@Immutable
@JsonSerialize(as = ImmutableFeeResult.class)
@JsonDeserialize(as = ImmutableFeeResult.class)
public interface FeeResult extends JsonRpcResult {

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
  @JsonSerialize(using = LedgerIndexSerializer.class)
  @JsonProperty("ledger_current_index")
  UnsignedInteger ledgerCurrentIndex();

  /**
   * Various information about the transaction cost, in
   * <a href="https://xrpl.org/transaction-cost.html#fee-levels">fee levels</a>. The ratio in fee
   * levels applies to any transaction relative to the minimum cost of that particular transaction.
   */
  FeeLevels levels();

  /**
   * The maximum number of transactions that the transaction queue can currently hold.
   */
  @JsonProperty("max_queue_size")
  String maxQueueSize();

}
