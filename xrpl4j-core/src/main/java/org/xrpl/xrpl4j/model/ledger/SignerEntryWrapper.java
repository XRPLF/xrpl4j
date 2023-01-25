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
import org.immutables.value.Value;

/**
 * A wrapper for {@link SignerEntry} to conform to the rippled API JSON structure.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSignerEntryWrapper.class)
@JsonDeserialize(as = ImmutableSignerEntryWrapper.class)
public interface SignerEntryWrapper {

  /**
   * Construct a new wrapper for the given {@link SignerEntry}.
   *
   * @param entry A {@link SignerEntry}.
   *
   * @return A {@link SignerEntryWrapper} wrapping the given {@link SignerEntry}.
   */
  static SignerEntryWrapper of(SignerEntry entry) {
    return ImmutableSignerEntryWrapper.builder()
      .signerEntry(entry)
      .build();
  }

  /**
   * The {@link SignerEntry} that this wrapper wraps.
   *
   * @return A {@link SignerEntry}.
   */
  @JsonProperty("SignerEntry")
  SignerEntry signerEntry();

}
