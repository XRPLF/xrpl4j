package org.xrpl.xrpl4j.model.client.path;

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
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;

import java.util.List;
import java.util.Optional;

/**
 * Request parameters for a "ripple_path_find" rippled API method call.
 *
 * <p>This method is only enabled in the JSON RPC API.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableRipplePathFindRequestParams.class)
@JsonDeserialize(as = ImmutableRipplePathFindRequestParams.class)
public interface RipplePathFindRequestParams extends XrplRequestParams {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableRipplePathFindRequestParams.Builder}.
   */
  static ImmutableRipplePathFindRequestParams.Builder builder() {
    return ImmutableRipplePathFindRequestParams.builder();
  }

  /**
   * Unique {@link Address} of the account that would send funds in a transaction.
   *
   * @return The unique {@link Address} of the source account.
   */
  @JsonProperty("source_account")
  Address sourceAccount();

  /**
   * Unique {@link Address} of the account that would receive funds in a transaction.
   *
   * @return The unique {@link Address} of the destination account.
   */
  @JsonProperty("destination_account")
  Address destinationAccount();

  /**
   * {@link CurrencyAmount} that the destination account would receive in a transaction.
   *
   * <p>Special case: You can specify "-1" (for XRP) or provide "-1" as the contents of
   * {@link org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount#value()} (for non-XRP currencies).
   * This requests a path to deliver as much as possible, while spending no more than the amount specified in
   * {@link #sendMax()} (if provided).
   *
   * @return A {@link CurrencyAmount} denoting the destination amount.
   */
  // TODO: XrpCurrencyAmount currently doesn't support negative values, so one would not be able to set this to
  //  "-1" for XRP.  We should still type XrpCurrencyAmount as a wrapper of a String to allow for this.
  @JsonProperty("destination_amount")
  CurrencyAmount destinationAmount();

  /**
   * {@link CurrencyAmount} that would be spent in the transaction. Cannot be used with {@link #sourceCurrencies()}.
   *
   * @return A {@link CurrencyAmount} denoting the send max.
   */
  @JsonProperty("send_max")
  Optional<CurrencyAmount> sendMax();

  /**
   * A {@link List} of {@link PathCurrency} that the source account might want to spend.
   *
   * <p>Cannot contain more than 18 source currencies.
   *
   * @return A {@link List} of {@link PathCurrency} containing all of the source currencies.
   */
  @JsonProperty("source_currencies")
  List<PathCurrency> sourceCurrencies();

  /**
   * Specifies the ledger version to request. A ledger version can be specified by ledger hash,
   * numerical ledger index, or a shortcut value.
   *
   * @return A {@link LedgerSpecifier} specifying the ledger version to request.
   */
  @JsonUnwrapped
  LedgerSpecifier ledgerSpecifier();
  
}
