package org.xrpl.xrpl4j.model.client.ledger;

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


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.LegacyLedgerSpecifierUtils;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Request parameters for the "ledger" rippled API method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLedgerRequestParams.class)
@JsonDeserialize(as = ImmutableLedgerRequestParams.class)
public interface LedgerRequestParams extends XrplRequestParams {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableLedgerRequestParams.Builder}.
   */
  static ImmutableLedgerRequestParams.Builder builder() {
    return ImmutableLedgerRequestParams.builder();
  }

  /**
   * A 20-byte hex string for the ledger version to use.
   *
   * @return An optionally-present {@link Hash256}.
   * @deprecated Ledger hash should be specified in {@link #ledgerSpecifier()}.
   */
  @JsonIgnore
  @Deprecated
  @Value.Auxiliary
  Optional<Hash256> ledgerHash();

  /**
   * The ledger index of the ledger to use, or a shortcut string to choose a ledger automatically.
   *
   * @return A {@link LedgerIndex}.  Defaults to {@link LedgerIndex#CURRENT}.
   * @deprecated Ledger index and any shortcut values should be specified in {@link #ledgerSpecifier()}.
   */
  @JsonIgnore
  @Deprecated
  @Nullable
  @Value.Auxiliary
  LedgerIndex ledgerIndex();

  /**
   * Specifies the ledger version to request. A ledger version can be specified by ledger hash,
   * numerical ledger index, or a shortcut value.
   *
   * @return A {@link LedgerSpecifier} specifying the ledger version to request.
   */
  @Value.Default
  @JsonUnwrapped
  default LedgerSpecifier ledgerSpecifier() {
    return LegacyLedgerSpecifierUtils.computeLedgerSpecifier(ledgerHash(), ledgerIndex());
  }

  /**
   * If true, return full information on the entire ledger. Ignored if you did not specify a {@code ledgerHash} in
   * {@link #ledgerSpecifier()}}.
   * Defaults to false. (Equivalent to enabling transactions, accounts, and expand.)
   *
   * <p>Caution: This is a very large amount of data -- on the order of several hundred megabytes!
   *
   * <p>Note: You must be a rippled Admin to set to true.
   *
   * @return {@code true} if requesting full information on the entire ledger, otherwise {@code false}.
   *   Defaults to {@code false}.
   */
  @Value.Default
  default boolean full() {
    return false;
  }

  /**
   * If true, return information on accounts in the ledger. Ignored if you did not specify a {@code ledgerHash} in
   * {@link #ledgerSpecifier()}}.
   *
   * <p>Caution: This returns a very large amount of data!
   *
   * <p>Note: You must be a rippled Admin to set to true.
   *
   * @return {@code true} if requesting account information, otherwise {@code false}.
   *   Defaults to {@code false}.
   */
  @Value.Default
  default boolean accounts() {
    return false;
  }

  /**
   * If true, return information on transactions in the specified ledger version. Defaults to false.
   * Ignored if you did not specify a {@code ledgerHash} in
   * {@link #ledgerSpecifier()}}.
   *
   * @return {@code true} if requesting transactions, otherwise {@code false}.
   *   Defaults to {@code false}.
   */
  @Value.Default
  default boolean transactions() {
    return false;
  }

  /**
   * Provide full JSON-formatted information for transaction/account information instead of only hashes.
   * Defaults to false. Ignored unless you request {@link #transactions()}, {@link #accounts()}, or both.
   *
   * @return {@code true} if requesting expanded transactions, otherwise {@code false}. Always {@code true}.
   */
  @Value.Derived
  default boolean expand() {
    return true;
  }

  /**
   * If true, include the {@code "owner_funds"} field in the metadata of
   * {@link org.xrpl.xrpl4j.model.transactions.OfferCreate} transactions in the response. Defaults to false.
   * Ignored unless {@link #transactions()} and {@link #expand()} are true.
   *
   * @return {@code true} if requesting the {@code "owner_funds"} field, otherwise {@code false}.
   *   Defaults to {@code false}.
   */
  @JsonProperty("owner_funds")
  @Value.Default
  default boolean ownerFunds() {
    return false;
  }

  /**
   * If true, and {@link #transactions()} and {@link #expand()} are both also true, return transaction information
   * in binary format (hexadecimal string) instead of JSON format.
   *
   * @return {@code true} if requesting transactions in binary format, otherwise {@code false}. Always {@code false}.
   */
  @Value.Derived
  default boolean binary() {
    return false;
  }

  /**
   * If true, and the command is requesting the current ledger, includes an array of queued transactions in the results.
   *
   * @return {@code true} if requesting queued transactions, otherwise {@code false}.
   *   Defaults to {@code false}.
   */
  @Value.Default
  default boolean queue() {
    return false;
  }

}
