package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Optional;

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
   *
   * @return {@code true} if this transaction changes this account's ways of authorizing transactions, otherwise
   *         {@code false}.
   */
  @JsonProperty("auth_change")
  boolean authChange();

  /**
   * The Transaction Cost of this transaction, in drops of XRP.
   *
   * @return An {@link XrpCurrencyAmount} representing the transaction cost.
   */
  XrpCurrencyAmount fee();

  /**
   * The transaction cost of this transaction, relative to the minimum cost for this type of transaction, in
   * <a href="https://xrpl.org/transaction-cost.html#fee-levels">fee levels</a>.
   *
   * @return An {@link XrpCurrencyAmount} representing the fee level.
   */
  @JsonProperty("fee_level")
  XrpCurrencyAmount feeLevel();

  /**
   * The maximum amount of XRP, in drops, this transaction could send or destroy.
   *
   * @return An {@link XrpCurrencyAmount} representing the maximum amount this transaction could send or destroy.
   */
  @JsonProperty("max_spend_drops")
  XrpCurrencyAmount maxSpendDrops();

  /**
   * The Sequence Number of this transaction.
   *
   * @return An {@link UnsignedInteger} denoting the sequence.
   */
  @JsonProperty("seq")
  UnsignedInteger sequence();

}
