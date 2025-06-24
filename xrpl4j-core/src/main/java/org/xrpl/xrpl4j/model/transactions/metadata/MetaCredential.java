package org.xrpl.xrpl4j.model.transactions.metadata;

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
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;

import java.util.Optional;

/**
 * {@link MetaCredential} inner object with Issuer and CredentialType details.
 */
@Immutable
@JsonSerialize(as = ImmutableMetaCredential.class)
@JsonDeserialize(as = ImmutableMetaCredential.class)
public interface MetaCredential {

  /**
   * The issuer of the credential.
   *
   * @return The {@link Address} of the issuer this credential.
   */
  @JsonProperty("Issuer")
  Optional<Address> issuer();

  /**
   * A (hex-encoded) value to identify the type of credential from the issuer.
   *
   * @return The {@link CredentialType} denoting the CredentialType.
   */
  @JsonProperty("CredentialType")
  Optional<CredentialType> credentialType();

}