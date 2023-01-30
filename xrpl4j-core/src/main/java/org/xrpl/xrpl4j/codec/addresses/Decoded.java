package org.xrpl.xrpl4j.codec.addresses;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: address-codec
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

import org.immutables.value.Value;

import java.util.Optional;

/**
 * Represents a decoded Base58 {@link String}.
 */
@Value.Immutable
public interface Decoded {

  /**
   * Get a new {@link ImmutableDecoded.Builder} instance.
   *
   * @return A {@link ImmutableDecoded.Builder}.
   */
  static ImmutableDecoded.Builder builder() {
    return ImmutableDecoded.builder();
  }

  /**
   * The {@link Version} of the decoded Base58 {@link String}.
   *
   * @return A {@link Version}.
   */
  Version version();

  /**
   * The bytes of the decoded Base58 {@link String}.
   *
   * @return An {@link UnsignedByteArray}.
   */
  UnsignedByteArray bytes();

  /**
   * The {@link KeyType} of the decoded Base58 {@link String}.
   *
   * @return An optionally present {@link KeyType}.
   */
  Optional<KeyType> type();

}
