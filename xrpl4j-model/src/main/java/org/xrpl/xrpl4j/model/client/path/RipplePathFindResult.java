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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.List;

/**
 * The result of a "ripple_path_find" rippled API method call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableRipplePathFindResult.class)
@JsonDeserialize(as = ImmutableRipplePathFindResult.class)
public interface RipplePathFindResult extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableRipplePathFindResult.Builder}.
   */
  static ImmutableRipplePathFindResult.Builder builder() {
    return ImmutableRipplePathFindResult.builder();
  }

  /**
   * A {@link List} of {@link PathAlternative}s with possible paths to take. If empty, then are are no paths
   * connecting the source and destination accounts.
   *
   * @return A {@link List} of {@link PathAlternative}s.
   */
  List<PathAlternative> alternatives();

  /**
   * Unique {@link org.xrpl.xrpl4j.model.transactions.Address} of the account that would receive a
   * {@link org.xrpl.xrpl4j.model.transactions.Payment} transaction.
   *
   * @return The unique {@link Address} of the destination account.
   */
  @JsonProperty("destination_account")
  Address destinationAccount();

  /**
   * {@link List} of {@link String}s representing the currencies that the {@link #destinationAccount()} accepts,
   * as 3-letter codes like "USD" or as 40-character hex like "015841551A748AD2C1F76FF6ECB0CCCD00000000".
   *
   * @return A {@link List} of {@link String}s of destination currency codes.
   */
  @JsonProperty("destination_currencies")
  List<String> destinationCurrencies();

}
