package org.xrpl.xrpl4j.model.client.nft;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Marker;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;

import java.util.Optional;

/**
 * Request params for "nft_buy_offers" rippled API method call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNftBuyOffersRequestParams.class)
@JsonDeserialize(as = ImmutableNftBuyOffersRequestParams.class)
public interface NftBuyOffersRequestParams extends XrplRequestParams {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableNftBuyOffersRequestParams.Builder}.
   */
  static ImmutableNftBuyOffersRequestParams.Builder builder() {
    return ImmutableNftBuyOffersRequestParams.builder();
  }

  /**
   *  The TokenID of the NFToken object.
   *
   *  @return the TokenID of the {@link org.xrpl.xrpl4j.model.client.accounts.NfTokenObject} object.
   */
  @JsonProperty("nft_id")
  NfTokenId nfTokenId();

  /**
   * Specifies the ledger version to request. A ledger version can be specified by ledger hash,
   * numerical ledger index, or a shortcut value.
   *
   * @return A {@link LedgerSpecifier} specifying the ledger version to request.
   */
  @JsonUnwrapped
  @Value.Default
  // This field was missing in xrpl4j <= 3.1.2. Normally, this would be a required field, but in order
  // to not make a breaking change, this needs to be defaulted. rippled will default to "validated" for you,
  // so defaulting to LedgerSpecifier.VALIDATED preserves the existing 3.x.x behavior.
  default LedgerSpecifier ledgerSpecifier() {
    return LedgerSpecifier.VALIDATED;
  }

  /**
   * Limit the number of buy offers for the {@link NfTokenId}. The server is not required to honor
   * this value. Must be within the inclusive range 10 to 400.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the response limit.
   */
  Optional<UnsignedInteger> limit();

  /**
   * Value from a previous paginated response. Resume retrieving data where that response left off.
   *
   * @return An optionally-present {@link String} containing the marker.
   */
  Optional<Marker> marker();
}
