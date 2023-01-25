package org.xrpl.xrpl4j.model.client.accounts;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Represents a transaction that exists in a given account's transaction queue.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableQueueTransaction.class)
@JsonDeserialize(as = ImmutableQueueTransaction.class)
public interface QueueTransaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableQueueTransaction.Builder}.
   */
  static ImmutableQueueTransaction.Builder builder() {
    return ImmutableQueueTransaction.builder();
  }

  /**
   * Whether this transaction changes this address's ways of authorizing transactions.
   *
   * @return {@code true} if this transaction changes this account's ways of authorizing transactions, otherwise
   *   {@code false}.
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
