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
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.util.Optional;

/**
 * Request parameters for the {@code account_sponsoring} RPC method, which retrieves a list of ledger objects
 * that an account is sponsoring.
 *
 * <p>This class will be marked {@link Beta} until the featureSponsorship amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/blob/master/XLS-0068-sponsored-fees-and-reserves/README.md"
 */
@Beta
@Value.Immutable
@JsonSerialize(as = ImmutableAccountSponsoringRequestParams.class)
@JsonDeserialize(as = ImmutableAccountSponsoringRequestParams.class)
public interface AccountSponsoringRequestParams extends XrplRequestParams {

  /**
   * Construct a {@code AccountSponsoringRequestParams} builder.
   *
   * @return An {@link ImmutableAccountSponsoringRequestParams.Builder}.
   */
  static ImmutableAccountSponsoringRequestParams.Builder builder() {
    return ImmutableAccountSponsoringRequestParams.builder();
  }

  /**
   * Construct an {@link AccountSponsoringRequestParams} for a given account and otherwise default parameters.
   *
   * @param classicAddress The classic {@link Address} of the account to request sponsored objects for.
   *
   * @return An {@link AccountSponsoringRequestParams} for the given {@link Address}.
   */
  static AccountSponsoringRequestParams of(Address classicAddress) {
    return builder()
      .account(classicAddress)
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .build();
  }

  /**
   * The unique {@link Address} for the account that is sponsoring other accounts or objects.
   *
   * @return The unique {@link Address} of the account.
   */
  @JsonProperty("account")
  Address account();

  /**
   * If true, the response only includes objects that would block this account from being deleted.
   * The default is false.
   *
   * @return {@code true} if only deletion blockers should be returned, otherwise {@code false}.
   */
  @JsonProperty("deletion_blockers_only")
  @Value.Default
  default boolean deletionBlockersOnly() {
    return false;
  }

  /**
   * Specifies the ledger version to request. A ledger version can be specified by ledger hash, numerical ledger index,
   * or a shortcut value. Defaults to {@link LedgerSpecifier#CURRENT}.
   *
   * @return A {@link LedgerSpecifier} specifying the ledger version to request.
   */
  @JsonUnwrapped
  LedgerSpecifier ledgerSpecifier();

  /**
   * Limit the number of ledger objects to retrieve. The server is not required to honor this value.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the response limit.
   */
  @JsonProperty("limit")
  Optional<UnsignedInteger> limit();

  /**
   * Value from a previous paginated response. Resume retrieving data where that response left off.
   *
   * @return An optionally-present {@link Marker} containing the marker.
   */
  @JsonProperty("marker")
  Optional<Marker> marker();

  /**
   * Filter results by a ledger entry type.
   *
   * @return An optionally-present {@link AccountObjectsRequestParams.AccountObjectType} to filter by.
   */
  Optional<AccountObjectsRequestParams.AccountObjectType> type();

}

