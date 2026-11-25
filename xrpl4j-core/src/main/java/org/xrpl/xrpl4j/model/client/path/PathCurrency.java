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
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a currency that an account holds on the XRPL, which can be used to specify the source currencies in
 * {@link RipplePathFindRequestParams}.
 *
 * <p>A {@link PathCurrency} is either a currency code with an optional issuer, or an MPToken issuance ID. This is
 * deliberately looser than {@link Issue}: the {@code source_currencies} field of a {@code ripple_path_find} request
 * uses a bare currency code to mean "this currency, from any issuer", which {@link Issue} cannot express.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePathCurrency.class)
@JsonDeserialize(as = ImmutablePathCurrency.class)
public interface PathCurrency {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutablePathCurrency.Builder}.
   */
  static ImmutablePathCurrency.Builder builder() {
    return ImmutablePathCurrency.builder();
  }

  /**
   * Construct a {@link PathCurrency} for a currency code held by any issuer.
   *
   * @param currency A {@link String} of either a 3 character currency code, or a 40 character hexadecimal encoded
   *                 currency code value.
   *
   * @return A new {@link PathCurrency}.
   */
  static PathCurrency of(final String currency) {
    Objects.requireNonNull(currency);
    return builder().currency(currency).build();
  }

  /**
   * Construct a {@link PathCurrency} for a currency code issued by a particular account.
   *
   * @param currency A {@link String} of either a 3 character currency code, or a 40 character hexadecimal encoded
   *                 currency code value.
   * @param issuer   The {@link Address} of the issuer of the currency.
   *
   * @return A new {@link PathCurrency}.
   */
  static PathCurrency of(final String currency, final Address issuer) {
    Objects.requireNonNull(currency);
    Objects.requireNonNull(issuer);
    return builder().currency(currency).issuer(issuer).build();
  }

  /**
   * Construct a {@link PathCurrency} from an {@link Issue}.
   *
   * @param issue An {@link Issue} representing the asset.
   *
   * @return A new {@link PathCurrency}.
   */
  static PathCurrency of(final Issue issue) {
    Objects.requireNonNull(issue);
    return issue.map(
      xrpIssue -> of(xrpIssue.currency()),
      iouIssue -> of(iouIssue.currency(), iouIssue.issuer()),
      mptIssue -> builder().mptIssuanceId(mptIssue.mptIssuanceId()).build()
    );
  }

  /**
   * Either a 3 character currency code, or a 40 character hexadecimal encoded currency code value. Empty if this
   * {@link PathCurrency} identifies an MPToken instead.
   *
   * @return An optionally-present {@link String} containing the currency code.
   */
  @JsonProperty("currency")
  Optional<String> currency();

  /**
   * The {@link Address} of the issuer of the currency. Empty when any issuer is acceptable, or when this
   * {@link PathCurrency} identifies an MPToken.
   *
   * @return The optionally-present {@link Address} of the issuer account.
   */
  @JsonProperty("issuer")
  Optional<Address> issuer();

  /**
   * The {@link MpTokenIssuanceId} of the MPToken this {@link PathCurrency} identifies. Empty if this
   * {@link PathCurrency} identifies a currency code instead.
   *
   * @return An optionally-present {@link MpTokenIssuanceId}.
   */
  @JsonProperty("mpt_issuance_id")
  Optional<MpTokenIssuanceId> mptIssuanceId();

  /**
   * Validate that exactly one asset type is specified, and that an MPToken carries no issuer.
   */
  @Value.Check
  default void checkExactlyOneAssetType() {
    if (currency().isPresent() == mptIssuanceId().isPresent()) {
      throw new IllegalStateException(
        "PathCurrency must have exactly one of currency or mptIssuanceId."
      );
    }

    if (mptIssuanceId().isPresent() && issuer().isPresent()) {
      throw new IllegalStateException("PathCurrency with an mptIssuanceId cannot have an issuer.");
    }
  }
}
