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
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Request parameters for the gateway_balances rippled method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGatewayBalancesRequestParams.class)
@JsonDeserialize(as = ImmutableGatewayBalancesRequestParams.class)
public interface GatewayBalancesRequestParams extends XrplRequestParams {
  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableGatewayBalancesRequestParams.Builder}.
   */
  static ImmutableGatewayBalancesRequestParams.Builder builder() {
    return ImmutableGatewayBalancesRequestParams.builder();
  }

  /**
   * The unique {@link Address} for the account, which should be an issuer. The request assets and
   * balances associated with the issuer account.
   *
   * @return The {@link Address} for the account.
   */
  Address account();

  /**
   * An optional set of addresses of operational accounts that should not be included in the
   * balances field of the response. Defaults to an empty set.
   * balances field of the response. Defaults to an empty set.
   *
   * @return An optionally specified set of operational address balances to exclude.
   */
  @Value.Default
  @JsonProperty("hotwallet")
  default Set<Address> hotWallets() {
    return Collections.emptySet();
  }

  /**
   * Specifies the ledger version to request. A ledger version can be specified by ledger hash,
   * numerical ledger index, or a shortcut value.
   *
   * @return A {@link LedgerSpecifier} specifying the ledger version to request.
   */
  @JsonUnwrapped
  @Nullable
  LedgerSpecifier ledgerSpecifier();

  /**
   * Forcibly set to true as true implies either a public key or address is being specified as the
   * account. Setting this field to false allows for secrets to be passed in which this API explictly
   * discourages.
   *
   * @return true to force usage of either a public key or address and not a secret.
   */
  @Value.Derived
  default boolean strict() {
    return true;
  }

}
