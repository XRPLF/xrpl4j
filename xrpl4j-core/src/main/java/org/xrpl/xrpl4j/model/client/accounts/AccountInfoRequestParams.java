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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.LegacyLedgerSpecifierUtils;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Request parameters for the "account_info" API method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountInfoRequestParams.class)
@JsonDeserialize(as = ImmutableAccountInfoRequestParams.class)
public interface AccountInfoRequestParams extends XrplRequestParams {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAccountInfoRequestParams.Builder}.
   */
  static ImmutableAccountInfoRequestParams.Builder builder() {
    return ImmutableAccountInfoRequestParams.builder();
  }

  /**
   * Constructs an {@link AccountInfoRequestParams} for a specified account, with all other fields set to their
   * defaults.
   *
   * @param account The {@link Address} of the account.
   *
   * @return An {@link AccountInfoRequestParams} for the specified account.
   */
  static AccountInfoRequestParams of(Address account) {
    return builder()
      .account(account)
      .build();
  }

  /**
   * A unique {@link Address} for the account.
   *
   * @return The {@link Address} of the account.
   */
  Address account();

  /**
   * Specifies the ledger version to request. A ledger version can be specified by ledger hash,
   * numerical ledger index, or a shortcut value.
   *
   * @return A {@link LedgerSpecifier} specifying the ledger version to request.
   */
  @JsonUnwrapped
  LedgerSpecifier ledgerSpecifier();

  /**
   * A boolean indicating if the {@link #account()} field only accepts a public key or XRP Ledger {@link Address}.
   * Always true, as {@link #account()} is always an {@link Address}.
   *
   * @return {@code true} if the account field only accepts a public key or XRP Ledger address, otherwise {@code false}.
   *   Defaults to {@code true}.
   */
  @Value.Derived
  default boolean strict() {
    return true;
  }

  /**
   * If true, and the <a href="https://xrpl.org/known-amendments.html#feeescalation">FeeEscalation</a> amendment is
   * enabled, also returns stats about queued transactions associated with this account. Can only be used when
   * querying for the data from the current open ledger.
   *
   * @return {@code true} if queue transactions should be returned in the response, {@code false} if not.
   *   Defaults to {@code false}.
   */
  @Value.Default
  default boolean queue() {
    return false;
  }

  /**
   * If true, and the <a href="https://xrpl.org/known-amendments.html#multisign">MultiSign amendment</a> is enabled,
   * also returns any {@link org.xrpl.xrpl4j.model.ledger.SignerListObject}s associated with this account.
   *
   * @return {@code true} if signer lists should be returns, {@code false} if not. Defaults to {@code true}.
   */
  @Value.Default
  @JsonProperty("signer_lists")
  default boolean signerLists() {
    return true;
  }

}
