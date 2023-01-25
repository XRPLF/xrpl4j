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
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Represents a TrustLine between two accounts on the XRPL. This representation is only present in responses
 * to "account_lines" rippled API method calls.
 *
 * <p>The values in this object are from the perspective of the requesting account.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTrustLine.class)
@JsonDeserialize(as = ImmutableTrustLine.class)
public interface TrustLine {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableTrustLine.Builder}.
   */
  static ImmutableTrustLine.Builder builder() {
    return ImmutableTrustLine.builder();
  }

  /**
   * The unique {@link Address} of the counterparty to this trust line.
   *
   * @return The unique {@link Address} of the counterparty to this trust line.
   */
  Address account();

  /**
   * Representation of the numeric balance currently held against this line. A positive balance means that
   * the perspective account holds value; a negative balance means that the perspective account owes value.
   *
   * @return A {@link String} representing a numeric balance.
   */
  String balance();

  /**
   * A Currency Code identifying what currency this trust line can hold.
   *
   * @return A {@link String} containing the currency code.
   */
  String currency();

  /**
   * The maximum amount of the given currency that this account is willing to owe the peer account.
   *
   * @return A {@link String} containing the numeric limit amount.
   */
  String limit();

  /**
   * The maximum amount of currency that the counterparty account is willing to owe the perspective account.
   *
   * @return A {@link String} containing the numeric limit amount for the peer account.
   */
  @JsonProperty("limit_peer")
  String limitPeer();

  /**
   * Rate at which the account values incoming balances on this trust line, as a ratio of this value per 1
   * billion units. (For example, a value of 500 million represents a 0.5:1 ratio.)
   *
   * <p>As a special case, 0 is treated as a 1:1 ratio.
   *
   * @return An {@link UnsignedInteger} representing the quality in ratio.
   */
  @JsonProperty("quality_in")
  UnsignedInteger qualityIn();

  /**
   * Rate at which the account values outgoing balances on this trust line, as a ratio of this value per 1
   * billion units. (For example, a value of 500 million represents a 0.5:1 ratio.)
   *
   * <p>As a special case, 0 is treated as a 1:1 ratio.
   *
   * @return An {@link UnsignedInteger} representing the quality out ratio.
   */
  @JsonProperty("quality_out")
  UnsignedInteger qualityOut();

  /**
   * Whether or not this account has enabled the lsfNoRipple flag for this line.
   *
   * @return {@code true} if this account has set the lsfNoRipple flag for this trust line, otherwise {@code false}.
   */
  @JsonProperty("no_ripple")
  @Value.Default
  default boolean noRipple() {
    return false;
  }

  /**
   * Whether or not the peer account has enabled the lsfNoRipple flag for this line.
   *
   * @return {@code true} if the peer account has set the lsfNoRipple flag for this trust line, otherwise {@code false}.
   */
  @JsonProperty("no_ripple_peer")
  @Value.Default
  default boolean noRipplePeer() {
    return false;
  }

  /**
   * Whether or not this account has authorized this trust line.
   *
   * @return {@code true} if this account has authorized this trust line, otherwise {@code false}.
   */
  @Value.Default
  default boolean authorized() {
    return false;
  }

  /**
   * Whether or not the peer account has authorized this trust line.
   *
   * @return {@code true} if the peer account has authorized this trust line, otherwise {@code false}.
   */
  @JsonProperty("peer_authorized")
  @Value.Default
  default boolean peerAuthorized() {
    return false;
  }

  /**
   * Whether or not this account has frozen this trust line.
   *
   * @return {@code true} if this account has frozen this trust line, otherwise {@code false}.
   */
  @Value.Default
  default boolean freeze() {
    return false;
  }

  /**
   * Whether or not the peer account has frozen this trust line.
   *
   * @return {@code true} if the peer account has frozen this trust line, otherwise {@code false}.
   */
  @JsonProperty("freeze_peer")
  @Value.Default
  default boolean freezePeer() {
    return false;
  }

}
