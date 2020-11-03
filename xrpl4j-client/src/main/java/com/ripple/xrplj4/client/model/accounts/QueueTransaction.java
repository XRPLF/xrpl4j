package com.ripple.xrplj4.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

/**
 * Represents a transaction that exists in a given account's transaction queue.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableQueueTransaction.class)
@JsonDeserialize(as = ImmutableQueueTransaction.class)
public interface QueueTransaction {

  static ImmutableQueueTransaction.Builder builder() {
    return ImmutableQueueTransaction.builder();
  }

  /**
   * Whether this transaction changes this address's ways of authorizing transactions.
   */
  @JsonProperty("auth_change")
  boolean authChange();

  /**
   * The Transaction Cost of this transaction, in drops of XRP.
   */
  String fee();

  /**
   * The transaction cost of this transaction, relative to the minimum cost for this type of transaction, in
   * <a href="https://xrpl.org/transaction-cost.html#fee-levels">fee levels</a>.
   */
  @JsonProperty("fee_level")
  String feeLevel();

  /**
   * The maximum amount of XRP, in drops, this transaction could send or destroy.
   */
  @JsonProperty("max_spend_drops")
  String maxSpendDrops();

  /**
   * The Sequence Number of this transaction.
   */
  UnsignedInteger seq();

}
