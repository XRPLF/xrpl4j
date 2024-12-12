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
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.util.Optional;

/**
 * Represents the request parameters for an "account_objects" rippled API call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountObjectsRequestParams.class)
@JsonDeserialize(as = ImmutableAccountObjectsRequestParams.class)
public interface AccountObjectsRequestParams extends XrplRequestParams {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAccountObjectsRequestParams.Builder}.
   */
  static ImmutableAccountObjectsRequestParams.Builder builder() {
    return ImmutableAccountObjectsRequestParams.builder();
  }

  /**
   * Construct an {@link AccountObjectsRequestParams} for a given account and otherwise default parameters.
   *
   * @param classicAddress The classic {@link Address} of the account to request objects for.
   *
   * @return An {@link AccountObjectsRequestParams} for the given {@link Address}.
   */
  static AccountObjectsRequestParams of(Address classicAddress) {
    return builder()
      .account(classicAddress)
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .build();
  }

  /**
   * The unique XRPL {@link Address} for the account.
   *
   * @return The unique XRPL {@link Address} for the account.
   */
  Address account();

  /**
   * If included, filter results to include only this type of ledger object.
   *
   * @return An optionally-present {@link AccountObjectType} to filter by.
   */
  Optional<AccountObjectType> type();

  /**
   * If true, the response only includes {@link org.xrpl.xrpl4j.model.ledger.LedgerObject}s that would block this
   * account from being deleted. The default is false.
   *
   * @return {@code true} if requesting only ledger objects that would block this account from being deleted, otherwise
   *   {@code false}.
   */
  @JsonProperty("deletion_blockers_only")
  @Value.Default
  default boolean deletionBlockersOnly() {
    return false;
  }

  /**
   * Specifies the ledger version to request. A ledger version can be specified by ledger hash,
   * numerical ledger index, or a shortcut value.
   *
   * @return A {@link LedgerSpecifier} specifying the ledger version to request.
   */
  @JsonUnwrapped
  LedgerSpecifier ledgerSpecifier();

  /**
   * The maximum number of {@link org.xrpl.xrpl4j.model.ledger.LedgerObject}s to include in the resulting
   * {@link AccountObjectsResult#accountObjects()}. Must be within the inclusive range 10 to 400 on non-admin
   * connections. The default is 200.
   *
   * @return An optionally-present {@link UnsignedInteger} denoting the response limit.
   */
  Optional<UnsignedInteger> limit();

  /**
   * Value from a previous paginated response. Resume retrieving data where that response left off.
   *
   * @return An optionally-present {@link String} containing the marker.
   */
  Optional<Marker> marker();

  /**
   * The enum Account object type.
   */
  enum AccountObjectType {
    /**
     * Check account object type.
     */
    CHECK("check"),
    /**
     * Desposit pre auth account object type.
     */
    DESPOSIT_PRE_AUTH("deposit_preauth"),
    /**
     * Escrow account object type.
     */
    ESCROW("escrow"),
    /**
     * Offer account object type.
     */
    OFFER("offer"),
    /**
     * Payment channel account object type.
     */
    PAYMENT_CHANNEL("payment_channel"),
    /**
     * Signer list account object type.
     */
    SIGNER_LIST("signer_list"),
    /**
     * Ticket account object type.
     */
    TICKET("ticket"),
    /**
     * State account object type.
     */
    STATE("state"),
    /**
     * NFT offer object type.
     */
    NFT_OFFER("nft_offer");

    private final String value;

    AccountObjectType(String value) {
      this.value = value;
    }

    /**
     * Value string.
     *
     * @return the string
     */
    @JsonValue
    public String value() {
      return value;
    }
  }
}
