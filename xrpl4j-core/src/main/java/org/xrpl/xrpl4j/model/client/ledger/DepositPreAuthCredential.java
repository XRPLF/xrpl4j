package org.xrpl.xrpl4j.model.client.ledger;

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
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;

/**
 * {@link DepositPreAuthCredential} inner object with Issuer and CredentialType details.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDepositPreAuthCredential.class)
@JsonDeserialize(as = ImmutableDepositPreAuthCredential.class)
public interface DepositPreAuthCredential {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableDepositPreAuthCredential.Builder}.
   */
  static ImmutableDepositPreAuthCredential.Builder builder() {
    return ImmutableDepositPreAuthCredential.builder();
  }

  /**
   * The issuer of the credential.
   *
   * @return The unique {@link Address} of the issuer this credential.
   */
  @JsonProperty("issuer")
  Address issuer();

  /**
   * A (hex-encoded) value to identify the type of credential from the issuer.
   *
   * @return A {@link CredentialType} defining the type of credential.
   */
  @JsonProperty("credential_type")
  CredentialType credentialType();
}