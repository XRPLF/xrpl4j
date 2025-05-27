package org.xrpl.xrpl4j.model.transactions;

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
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * A {@link DepositPreAuth} transaction gives another account pre-approval to deliver payments to the sender of
 * this transaction. This is only useful if the sender of this transaction is using (or plans to use)
 * <a href="https://xrpl.org/depositauth.html">Deposit Authorization</a>.
 *
 * <p>You can use this transaction to preauthorize certain counterparties before you enable Deposit Authorization.
 * This may be useful to ensure a smooth transition from not requiring deposit authorization to requiring it.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDepositPreAuth.class)
@JsonDeserialize(as = ImmutableDepositPreAuth.class)
public interface DepositPreAuth extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableDepositPreAuth.Builder}.
   */
  static ImmutableDepositPreAuth.Builder builder() {
    return ImmutableDepositPreAuth.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link DepositPreAuth}, which only allows the
   * {@code tfFullyCanonicalSig} flag, which is deprecated.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The XRP Ledger {@link Address} of the sender to preauthorize.
   *
   * @return An {@link Optional} of type {@link Address} of the sender to preauthorize.
   */
  @JsonProperty("Authorize")
  Optional<Address> authorize();

  /**
   * The XRP Ledger {@link Address} of a sender whose preauthorization should be revoked.
   *
   * @return An {@link Optional} of type {@link Address} of the sender to unauthorize.
   */
  @JsonProperty("Unauthorize")
  Optional<Address> unauthorize();

  /**
   * The {@link CredentialWrapper}'s to preauthorize.
   *
   * @return An {@link Optional} list of type {@link CredentialWrapper} to preauthorize.
   */
  @JsonProperty("AuthorizeCredentials")
  Optional<List<CredentialWrapper>> authorizeCredentials();

  /**
   * The {@link CredentialWrapper}'s whose preauthorization should be revoked.
   *
   * @return An {@link Optional} list of type {@link CredentialWrapper} to unauthorize.
   */
  @JsonProperty("UnauthorizeCredentials")
  Optional<List<CredentialWrapper>> unauthorizeCredentials();

  /**
   * Validate that exactly one of {@link DepositPreAuth#authorize()} or {@link DepositPreAuth#unauthorize()}
   * or {@link DepositPreAuth#authorizeCredentials()} or {@link DepositPreAuth#unauthorizeCredentials()}
   * is present.
   */
  @Value.Check
  default void validateExactOneFieldPresence() {
    int fieldsPresent = 0;
    if (authorize().isPresent()) {
      fieldsPresent++;
    }
    if (unauthorize().isPresent()) {
      fieldsPresent++;
    }
    if (authorizeCredentials().isPresent()) {
      fieldsPresent++;
    }
    if (unauthorizeCredentials().isPresent()) {
      fieldsPresent++;
    }

    Preconditions.checkArgument(fieldsPresent == 1,
            "Exactly one of Authorize, Unauthorize, AuthorizeCredentials, or UnauthorizeCredentials must be present.");
  }

  /**
   * Validate {@link DepositPreAuth#authorizeCredentials()} and {@link DepositPreAuth#unauthorizeCredentials()}
   * has less than or equal to 8 credentials.
   */
  @Value.Check
  default void validateCredentialList() {
    authorizeCredentials().ifPresent(creds ->
            Preconditions.checkArgument(
                    !creds.isEmpty() && creds.size() <= 8,
                    "AuthorizeCredentials shouldn't be empty and must have less than or equal to 8 credentials."
            )
    );

    unauthorizeCredentials().ifPresent(creds ->
            Preconditions.checkArgument(
                    !creds.isEmpty() && creds.size() <= 8,
                    "UnauthorizeCredentials shouldn't be empty and must have less than or equal to 8 credentials."
            )
    );
  }

  /**
   * Validate {@link DepositPreAuth#authorizeCredentials()} and {@link DepositPreAuth#unauthorizeCredentials()}
   * has unique credentials each.
   */
  @Value.Check
  default void validateForUniqueValues() {
    authorizeCredentials().ifPresent(creds -> {
      Preconditions.checkArgument(
              new HashSet<>(creds).size() == creds.size(),
              "AuthorizeCredentials should have unique credentials."
      );
    });

    unauthorizeCredentials().ifPresent(creds -> {
      Preconditions.checkArgument(
              new HashSet<>(creds).size() == creds.size(),
              "UnauthorizeCredentials should have unique credentials."
      );
    });
  }
}
