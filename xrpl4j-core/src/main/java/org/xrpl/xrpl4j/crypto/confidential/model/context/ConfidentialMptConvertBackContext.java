package org.xrpl.xrpl4j.crypto.confidential.model.context;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.immutables.value.Value.Lazy;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMptContextUtil;

/**
 * Represents the context hash for a ConfidentialMptConvertBack transaction.
 *
 * <p>The context hash is a 32-byte SHA512Half hash that binds the proof to a specific transaction.
 * Use {@link ConfidentialMptContextUtil#generateConvertBackContext} to create instances.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableConfidentialMptConvertBackContext.class)
@JsonDeserialize(as = ImmutableConfidentialMptConvertBackContext.class)
public interface ConfidentialMptConvertBackContext {

  /**
   * Creates a new builder for {@link ConfidentialMptConvertBackContext}.
   *
   * @return A new builder.
   */
  static ImmutableConfidentialMptConvertBackContext.Builder builder() {
    return ImmutableConfidentialMptConvertBackContext.builder();
  }

  /**
   * Creates a context from an {@link UnsignedByteArray}.
   *
   * @param value The 32-byte context hash.
   *
   * @return A {@link ConfidentialMptConvertBackContext}.
   *
   * @throws IllegalArgumentException if value is not exactly 32 bytes.
   */
  static ConfidentialMptConvertBackContext of(final UnsignedByteArray value) {
    return builder().value(value).build();
  }

  /**
   * Creates a context from a hex string.
   *
   * @param hex The 64-character hex string representing the context hash.
   *
   * @return A {@link ConfidentialMptConvertBackContext}.
   *
   * @throws IllegalArgumentException if hex is not a valid 32-byte hex string.
   */
  static ConfidentialMptConvertBackContext fromHex(final String hex) {
    return of(UnsignedByteArray.fromHex(hex));
  }

  /**
   * The 32-byte context hash.
   *
   * @return The context hash as an {@link UnsignedByteArray}.
   */
  UnsignedByteArray value();

  /**
   * Validates that the context hash is exactly 32 bytes.
   */
  @Value.Check
  default void check() {
    Preconditions.checkArgument(
      value().length() == 32,
      "Context hash must be %s bytes, but was %s bytes",
      32, value().length()
    );
  }

  /**
   * Returns the context hash as an uppercase hex string.
   *
   * @return A {@link String}.
   */
  @Lazy
  @JsonIgnore
  default String hexValue() {
    return BaseEncoding.base16().encode(value().toByteArray());
  }
}

