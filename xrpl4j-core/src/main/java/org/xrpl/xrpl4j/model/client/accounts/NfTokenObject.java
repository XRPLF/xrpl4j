package org.xrpl.xrpl4j.model.client.accounts;

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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;

import java.util.Optional;

/**
 * Structure of an NFToken stored on the ledger.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNfTokenObject.class)
@JsonDeserialize(as = ImmutableNfTokenObject.class)
public interface NfTokenObject {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableNfTokenObject.Builder}.
   */
  static ImmutableNfTokenObject.Builder builder() {
    return ImmutableNfTokenObject.builder();
  }

  /**
   * The unique TokenID of the token.
   *
   * @return The unique TokenID of the token.
   */
  @JsonProperty("NFTokenID")
  NfTokenId nfTokenId();

  /**
   * The URI for the data of the token.
   *
   * @return The URI for the data of the token.
   */
  @JsonProperty("URI")
  Optional<NfTokenUri> uri();
}
