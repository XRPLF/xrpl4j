package org.xrpl.xrpl4j.model.ledger;

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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Contains the contents of a given ledger on the XRPL.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLedgerHeader.class)
@JsonDeserialize(as = ImmutableLedgerHeader.class)
public interface LedgerHeader {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableLedgerHeader.Builder}.
   */
  static ImmutableLedgerHeader.Builder builder() {
    return ImmutableLedgerHeader.builder();
  }

  /**
   * The ledger index of the ledger. In other objects, this would be a
   * {@link org.xrpl.xrpl4j.model.client.common.LedgerIndex}, however the ledger
   * method returns the ledger_index as a {@link String} representing an unsigned 32 bit integer.
   *
   * @return A {@link String} containing the ledger index.
   */
  @JsonProperty("ledger_index")
  LedgerIndex ledgerIndex();

  /**
   * The SHA-512Half of this ledger version. This serves as a unique identifier for this ledger and all its contents.
   *
   * @return An {@link Optional} of type {@link Hash256} containing the hash of the ledger.
   */
  @JsonProperty("ledger_hash")
  Optional<Hash256> ledgerHash();

  /**
   * The SHA-512Half of this ledger's state tree information.
   *
   * @return An {@link Optional} of type {@link Hash256} containing the hash of this ledger's state tree.
   */
  @JsonProperty("account_hash")
  Optional<Hash256> accountHash();

  /**
   * The approximate time this ledger version closed, as the number of
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
   * This value is rounded based on the {@link #closeTimeResolution()}}.
   *
   * @return An {@link Optional} of type {@link UnsignedLong} representing the close time.
   */
  @JsonProperty("close_time")
  Optional<UnsignedLong> closeTime();

  /**
   * The time this ledger was closed, in human-readable format. Always uses the UTC time zone.
   *
   * @return A {@link ZonedDateTime} representing the {@link #closeTime()} in human-readable format.
   */
  @JsonProperty("close_time_human")
  @JsonFormat(pattern = "yyyy-MMM-dd HH:mm:ss.SSSSSSSSS z", locale = "en_US")
  Optional<ZonedDateTime> closeTimeHuman();

  /**
   * If true, this ledger version is no longer accepting new transactions. (However, unless this ledger
   * version is validated, it might be replaced by a different ledger version with a different set of transactions.)
   *
   * @return {@code true} if this ledger version is no longer accepting new transactions, otherwise {@code false}.
   */
  @Value.Default
  default boolean closed() {
    return false;
  }

  /**
   * The {@link #ledgerHash()} value of the previous ledger version that is the direct predecessor of this one.
   * If there are different versions of the previous ledger index, this indicates from which one the ledger was derived.
   *
   * @return A {@link Hash256} containing the hash of this ledger's parent ledger.
   */
  @JsonProperty("parent_hash")
  Hash256 parentHash();

  /**
   * The time at which the previous ledger was closed.
   *
   * @return An {@link UnsignedLong} denoting this ledger's parent ledger's close time.
   */
  @JsonProperty("parent_close_time")
  Optional<UnsignedLong> parentCloseTime();

  /**
   * The total number of drops of XRP owned by accounts in the ledger. This omits XRP that has been
   * destroyed by transaction fees. The actual amount of XRP in circulation is lower because some
   * accounts are "black holes" whose keys are not known by anyone.
   *
   * @return An {@link Optional} of type {@link XrpCurrencyAmount} representing the total number of coins.
   */
  @JsonProperty("total_coins")
  Optional<XrpCurrencyAmount> totalCoins();

  /**
   * The SHA-512Half of the transactions included in this ledger.
   *
   * @return A {@link Hash256} containing the hash of all transactions in this ledger.
   */
  @JsonProperty("transaction_hash")
  Optional<Hash256> transactionHash();

  /**
   * Transactions applied in this ledger version.
   *
   * @return A {@link List} of {@link TransactionResult}s containing all the transactions in this ledger.
   */
  List<TransactionResult<? extends Transaction>> transactions();

  /**
   * An {@link UnsignedInteger} in the range [2,120] indicating the maximum number of seconds by which the
   * {@link #closeTime()} could be rounded.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the close time resolution.
   */
  @JsonProperty("close_time_resolution")
  Optional<UnsignedInteger> closeTimeResolution();

}
