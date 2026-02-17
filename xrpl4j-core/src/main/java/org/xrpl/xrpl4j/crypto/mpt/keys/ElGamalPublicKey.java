package org.xrpl.xrpl4j.crypto.mpt.keys;

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

import com.google.common.base.Preconditions;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;

import java.util.Objects;

/**
 * An ElGamal public key for secp256k1 used in Confidential MPT operations.
 *
 * <p>This class represents a public key specifically for ElGamal encryption operations.
 * Unlike {@link org.xrpl.xrpl4j.crypto.keys.PublicKey}, this class intentionally does NOT provide a
 * {@code deriveAddress()} method to prevent accidental use as an XRPL account key.</p>
 *
 * @see ElGamalPrivateKey
 * @see ElGamalKeyPair
 */
public final class ElGamalPublicKey {

  public static final int COMPRESSED_KEY_LENGTH = 33;
  private static final Secp256k1Operations SECP256K1 = new Secp256k1Operations();

  private final UnsignedByteArray value;

  private ElGamalPublicKey(final UnsignedByteArray value) {
    this.value = UnsignedByteArray.of(value.toByteArray());
  }

  public static ElGamalPublicKey fromCompressedBytes(final UnsignedByteArray value) {
    Objects.requireNonNull(value, "value must not be null");
    Preconditions.checkArgument(
      value.length() == COMPRESSED_KEY_LENGTH,
      "Compressed ElGamal public key must be %s bytes, but was %s bytes",
      COMPRESSED_KEY_LENGTH, value.length()
    );
    return new ElGamalPublicKey(value);
  }

  public static ElGamalPublicKey fromCompressedBytes(final byte[] value) {
    Objects.requireNonNull(value, "value must not be null");
    return fromCompressedBytes(UnsignedByteArray.of(value));
  }

  public static ElGamalPublicKey fromEcPoint(final ECPoint point) {
    Objects.requireNonNull(point, "point must not be null");
    return fromCompressedBytes(SECP256K1.serializeCompressed(point));
  }

  public UnsignedByteArray value() {
    return UnsignedByteArray.of(value.toByteArray());
  }

  public ECPoint asEcPoint() {
    return SECP256K1.deserialize(value.toByteArray());
  }

  public UnsignedByteArray uncompressedValue() {
    ECPoint point = asEcPoint();
    return UnsignedByteArray.of(SECP256K1.serializeUncompressedWithoutPrefix(point));
  }

  public UnsignedByteArray uncompressedValueReversed() {
    byte[] uncompressed = uncompressedValue().toByteArray();
    byte[] reversed = new byte[64];
    // Reverse X coordinate (first 32 bytes)
    for (int i = 0; i < 32; i++) {
      reversed[i] = uncompressed[31 - i];
    }
    // Reverse Y coordinate (last 32 bytes)
    for (int i = 0; i < 32; i++) {
      reversed[32 + i] = uncompressed[63 - i];
    }
    return UnsignedByteArray.of(reversed);
  }

  public String toReversedHex64() {
    return uncompressedValueReversed().hexValue();
  }

  public String toCompressedHex() {
    return value.hexValue();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ElGamalPublicKey that = (ElGamalPublicKey) obj;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "ElGamalPublicKey{value=" + toCompressedHex() + "}";
  }
}

