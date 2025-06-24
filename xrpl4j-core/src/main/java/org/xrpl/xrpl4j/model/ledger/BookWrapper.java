package org.xrpl.xrpl4j.model.ledger;

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

/**
 * A wrapper for {@link Book} to conform to the rippled API JSON structure.
 */
@Immutable
@JsonSerialize(as = ImmutableBookWrapper.class)
@JsonDeserialize(as = ImmutableBookWrapper.class)
public interface BookWrapper {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableBookWrapper.Builder}.
   */
  static ImmutableBookWrapper.Builder builder() {
    return ImmutableBookWrapper.builder();
  }

  /**
   * A Book object.
   *
   * @return A {@link Book} object.
   */
  @JsonProperty("Book")
  Book book();

}