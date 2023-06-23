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
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.ledger.ImmutableSignerEntryWrapper;

/**
 * A wrapper for {@link MetaSignerEntry} to conform to the rippled API JSON structure.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMetaSignerEntryWrapper.class)
@JsonDeserialize(as = ImmutableMetaSignerEntryWrapper.class)
public interface MetaSignerEntryWrapper {

  /**
   * Construct a {@code MetaSignerEntryWrapper} builder.
   *
   * @return An {@link ImmutableMetaSignerEntryWrapper.Builder}.
   */
  static ImmutableMetaSignerEntryWrapper.Builder builder() {
    return ImmutableMetaSignerEntryWrapper.builder();
  }

  /**
   * The {@link MetaSignerEntry} that this wrapper wraps.
   *
   * @return A {@link MetaSignerEntry}.
   */
  @JsonProperty("SignerEntry")
  MetaSignerEntry signerEntry();

}
