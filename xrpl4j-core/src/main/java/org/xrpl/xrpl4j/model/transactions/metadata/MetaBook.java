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
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

/**
 * Object referencing a specific page of an offer directory.
 */
@Immutable
@JsonSerialize(as = ImmutableMetaBook.class)
@JsonDeserialize(as = ImmutableMetaBook.class)
public interface MetaBook {

  /**
   * The ID of the Offer Directory that links to this offer.
   *
   * @return An optionally-present {@link Hash256} containing the ID.
   */
  @JsonProperty("BookDirectory")
  Optional<Hash256> bookDirectory();

  /**
   * A hint indicating which page of the offer directory links to this object, in case the directory consists of
   * multiple pages.
   *
   * @return An optionally-present {@link String} containing the hint.
   */
  @JsonProperty("BookNode")
  Optional<String> bookNode();

}