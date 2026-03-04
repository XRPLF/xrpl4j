package org.xrpl.xrpl4j.crypto.confidential.model;

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

import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Represents a Pedersen Commitment: C = amount * G + rho * H.
 *
 * <p>This immutable stores the commitment as compressed bytes (33 bytes) and provides
 * convenient methods for serialization in various formats needed for transactions and proofs.</p>
 */
@Value.Immutable
public interface PedersenCommitment {

  /**
   * The compressed point size in bytes.
   */
  int COMPRESSED_SIZE = 33;

  /**
   * Creates a PedersenCommitment from an UnsignedByteArray containing compressed bytes.
   *
   * @param compressedBytes The 33-byte compressed point.
   *
   * @return A new PedersenCommitment instance.
   */
  static PedersenCommitment of(UnsignedByteArray compressedBytes) {
    return ImmutablePedersenCommitment.builder()
      .value(compressedBytes)
      .build();
  }

  /**
   * Creates a PedersenCommitment from compressed bytes (33 bytes).
   *
   * @param compressedBytes The 33-byte compressed point.
   *
   * @return A new PedersenCommitment instance.
   *
   * @throws IllegalArgumentException if the bytes are not 33 bytes.
   */
  static PedersenCommitment of(byte[] compressedBytes) {
    return of(UnsignedByteArray.of(compressedBytes));
  }

  /**
   * Creates a PedersenCommitment from a hex string.
   *
   * @param hex The hex string of the 33-byte compressed point.
   *
   * @return A new PedersenCommitment instance.
   */
  static PedersenCommitment fromHex(String hex) {
    return of(BaseEncoding.base16().decode(hex.toUpperCase()));
  }

  /**
   * Returns the commitment as 33-byte compressed format.
   *
   * @return The 33-byte compressed point as UnsignedByteArray.
   */
  UnsignedByteArray value();

  /**
   * Validates that the value is exactly 33 bytes.
   */
  @Value.Check
  default void check() {
    if (value().length() != COMPRESSED_SIZE) {
      throw new IllegalArgumentException(
        "PedersenCommitment must be " + COMPRESSED_SIZE + " bytes, got " + value().length()
      );
    }
  }

  /**
   * Returns the commitment as hex string of the compressed format.
   *
   * @return The hex string of the 33-byte compressed point.
   */
  @Value.Lazy
  default String hexValue() {
    return BaseEncoding.base16().encode(value().toByteArray());
  }
}

