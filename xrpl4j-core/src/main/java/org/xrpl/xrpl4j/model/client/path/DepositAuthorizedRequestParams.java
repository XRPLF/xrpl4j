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
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.HashSet;
import java.util.List;

/**
 * Request parameters for a "deposit_authorized" rippled API method call.
 *
 * @see "https://xrpl.org/deposit_authorized.html"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDepositAuthorizedRequestParams.class)
@JsonDeserialize(as = ImmutableDepositAuthorizedRequestParams.class)
public interface DepositAuthorizedRequestParams extends XrplRequestParams {

  /**
   * Construct a builder.
   *
   * @return {@link ImmutableDepositAuthorizedRequestParams.Builder}
   */
  static ImmutableDepositAuthorizedRequestParams.Builder builder() {
    return ImmutableDepositAuthorizedRequestParams.builder();
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
   * If this field is included, then the credential will be taken into account when analyzing whether the sender can
   * send funds to the destination.
   *
   * @return A list of {@link Hash256} representing unique IDs of Credential entry in the ledger.
   */
  @JsonProperty("credentials")
  List<Hash256> credentials();

  /**
   * Specifies the ledger version to request. A ledger version can be specified by ledger hash, numerical ledger index,
   * or a shortcut value.
   *
   * @return A {@link LedgerSpecifier} specifying the ledger version to request.
   */
  @Value.Default
  @JsonUnwrapped
  default LedgerSpecifier ledgerSpecifier() {
    return LedgerSpecifier.CURRENT;
  }

  /**
   * Validate {@link DepositAuthorizedRequestParams#credentials} has less than or equal to 8 credentials.
   */
  @Value.Check
  default void validateCredentialsLength() {
    if (!credentials().isEmpty()) {
      Preconditions.checkArgument(
        credentials().size() <= 8,
        "credentials should have less than or equal to 8 items."
      );
    }
  }

  /**
   * Validate {@link DepositAuthorizedRequestParams#credentials} are unique.
   */
  @Value.Check
  default void validateUniqueCredentials() {
    if (!credentials().isEmpty()) {
      Preconditions.checkArgument(
        new HashSet<>(credentials()).size() == credentials().size(),
        "credentials should have unique values."
      );
    }
  }
}
