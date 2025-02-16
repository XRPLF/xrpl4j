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
 * Provides a wrapper for {@link Signer}s, in order to conform to the XRPL transaction JSON structure.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSignerWrapper.class)
@JsonDeserialize(as = ImmutableSignerWrapper.class)
public interface SignerWrapper {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSignerWrapper.Builder}.
   */
  static ImmutableSignerWrapper.Builder builder() {
    return ImmutableSignerWrapper.builder();
  }

  /**
   * Construct a {@link SignerWrapper} wrapping the given {@link Signer}.
   *
   * @param signer A {@link Signer}.
   * @return A {@link SignerWrapper}.
   */
  static SignerWrapper of(Signer signer) {
    return builder().signer(signer).build();
  }

  /**
   * The {@link Signer} that this wrapper wraps.
   *
   * @return The {@link Signer} that this wrapper wraps.
   */
  @JsonProperty("Signer")
  Signer signer();
}
