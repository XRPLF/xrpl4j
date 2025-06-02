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
import org.immutables.value.Value;

/**
 * Wrapper object for a {@link Credential}, so that the JSON representation of a list of {@link Credential}s is correct,
 * according to the XRPL binary serialization specification.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCredentialWrapper.class)
@JsonDeserialize(as = ImmutableCredentialWrapper.class)
public interface CredentialWrapper {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableCredentialWrapper.Builder}.
   */
  static ImmutableCredentialWrapper.Builder builder() {
    return ImmutableCredentialWrapper.builder();
  }

  /**
   * A credential.
   *
   * @return A {@link Credential}.
   */
  @JsonProperty("Credential")
  Credential credential();

}
