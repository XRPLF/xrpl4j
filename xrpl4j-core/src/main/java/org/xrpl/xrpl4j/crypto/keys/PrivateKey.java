package org.xrpl.xrpl4j.crypto.keys;

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
import com.google.common.hash.HashCode;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.security.Key;
import java.util.Objects;

/**
 * A typed instance of an XRPL private-key.
 */
public class PrivateKey implements PrivateKeyable, javax.security.auth.Destroyable {

  /**
   * A one-byte prefix for ed25519 keys.
   *
   * @deprecated This value will be removed in a future version. Prefer {@link #ED2559_PREFIX} or
   *   {@link #SECP256K1_PREFIX} instead.
   */
  @Deprecated
  public static final UnsignedByte PREFIX = UnsignedByte.of(0xED);

  /**
   * Private keys (whether from the ed25519 or secp256k1 curves) have 32 bytes naturally. At the same time, secp256k1
   * public keys have 33 bytes naturally, whereas ed25519 public keys have 32 bytes naturally. Because of this, in XRPL,
   * ed25519 public keys are prefixed with a one-byte prefix (i.e., 0xED). For consistency, this library (and other XRPL
   * tooling) also prepends all private keys with artificial prefixes (0xED for ed25519 or 0x00 for secp256k1). This
   * value is the one-byte prefix for ed25519 keys.
   */
  public static final UnsignedByte ED2559_PREFIX = UnsignedByte.of(0xED);

  /**
   * Private keys (whether from the ed25519 or secp256k1 curves) have 32 bytes naturally. At the same time, secp256k1
   * public keys have 33 bytes naturally, whereas ed25519 public keys have 32 bytes naturally. Because of this, in XRPL,
   * ed25519 public keys are prefixed with a one-byte prefix (i.e., 0xED). For consistency, this library (and other XRPL
   * tooling) also prepends all private keys with artificial prefixes (0xED for ed25519 or 0x00 for secp256k1).  This
   * value is the one-byte prefix for secp256k1 keys.
   */
  public static final UnsignedByte SECP256K1_PREFIX = UnsignedByte.of(0x00);

  private final UnsignedByteArray value;

  private final KeyType keyType;

  private boolean destroyed;

  /**
   * Instantiates a new instance of {@link PrivateKey} using the supplied 32 bytes and specified key type.
   *
   * @param value   An {@link UnsignedByteArray} containing a private key's natural bytes (i.e., 32 bytes).
   * @param keyType A {@link KeyType} for this private key.
   *
   * @return A {@link PrivateKey}.
   */
  public static PrivateKey fromNaturalBytes(final UnsignedByteArray value, final KeyType keyType) {
    return new PrivateKey(value, keyType); // <-- rely on constructor for all validation and error messaging.
  }

  /**
   * Instantiates a new instance of {@link PrivateKey} using the supplied bytes by inspecting the first byte out of 33
   * to see which {@link KeyType} to assign.
   *
   * @param value An {@link UnsignedByteArray} containing a private key's natural bytes (i.e., 32 bytes).
   *
   * @return A {@link PrivateKey}.
   */
  public static PrivateKey fromPrefixedBytes(final UnsignedByteArray value) {
    Objects.requireNonNull(value);

    Preconditions.checkArgument(value.length() == 33, String.format(
      "The `fromPrefixedBytes` function requires input length of 33 bytes, but %s were supplied.",
      value.length()
    ));

    final UnsignedByte prefixByte = value.get(0); // <-- relies upon the above length check.
    if (ED2559_PREFIX.equals(prefixByte)) {
      return new PrivateKey(value.slice(1, 33), KeyType.ED25519);
    } else if (SECP256K1_PREFIX.equals(prefixByte)) {
      return new PrivateKey(value.slice(1, 33), KeyType.SECP256K1);
    } else {
      throw new IllegalArgumentException(String.format(
        "Constructing a PrivateKey with raw bytes requires a one-byte prefix in front of the 32 natural bytes of a" +
          " private key. Use the prefix `0xED` for ed25519 private keys, or `0x00` for secp256k1 private keys. " +
          "Length was %s bytes.", value.length())
      );
    }
  }

  /**
   * Instantiates a new instance of {@link PrivateKey} using the supplied bytes by inspecting the first byte out of 33
   * to see which {@link KeyType} to assign.
   *
   * @param value An {@link UnsignedByteArray} containing this key's binary value.
   *
   * @return A {@link PrivateKey}.
   *
   * @deprecated This method will be removed in a future version. Prefer {@link #fromPrefixedBytes(UnsignedByteArray)}
   *   instead.
   */
  @Deprecated
  public static PrivateKey of(final UnsignedByteArray value) {
    // Assumption: Any developer using this method before this method was deprecated (i.e., v3.3) will likely have
    // expected `value` to have been prefixed. That said, this assumption is technically invalid because it's ambiguous
    // what any particular developer would have meant (see #486) because prior to #486, there was no byte-length check
    // on these bytes.
    return fromPrefixedBytes(value);
  }

  /**
   * Required-args Constructor.
   *
   * @param value An {@link UnsignedByteArray} for this key's value.
   */
  private PrivateKey(final UnsignedByteArray value, final KeyType keyType) {
    Objects.requireNonNull(value); // <-- Check not-null first.
    this.keyType = Objects.requireNonNull(keyType);

    // We assert this precondition here because this is a private constructor that can be fully tested via unit test,
    // so this precondition should never be violated, and if it is, then it's a bug in xrpl4j.
    Preconditions.checkArgument(
      value.length() == 32,
      "Byte values passed to this constructor must be 32 bytes long, with no prefix."
    );

    this.value = UnsignedByteArray.of(value.toByteArray()); // <- Always copy to ensure immutability
  }

  /**
   * Accessor for the key value, in binary (Note: will be 33 bytes).
   *
   * @return An instance of {@link UnsignedByteArray}.
   *
   * @deprecated Prefer {@link #valueWithPrefixedBytes()} or {@link #valueWithNaturalBytes()} instead.
   */
  @Deprecated
  public UnsignedByteArray value() {
    // Check for empty value, which can occur if this PrivateKey is "destroyed" but still in memory.
    if (value.length() == 0) {
      return UnsignedByteArray.empty();
    } else {
      // This is technically wrong (because `value()` had an ambiguous meaning prior to fixing #486), but this mirrors
      // what's in v3 prior to fixing #486, and will be fixed in v4 once the deprecated `.value()` is removed.
      return this.valueWithPrefixedBytes();
    }
  }

  /**
   * Accessor for the byte value in {@link #value()} but in a more natural form (i.e., the size of the returned value
   * will be 32 bytes). Natural ed25519 or secp256k1 private keys will ordinarily contain only 32 bytes. However, in
   * XRPL, private keys are represented with a single-byte prefix (i.e., `0xED` for ed25519 and `0x00` for secp256k1
   * keys).
   *
   * @return An instance of {@link UnsignedByteArray}.
   */
  public UnsignedByteArray valueWithNaturalBytes() {
    // Check for empty value, which can occur if this PrivateKey is "destroyed" but still in memory.
    if (value.length() == 0) {
      return UnsignedByteArray.empty();
    } else {
      // Note: `toByteArray()` will perform a copy, which is what we want in order to enforce immutability of this
      // PrivateKey (because Java 8 doesn't support immutable byte arrays).
      return UnsignedByteArray.of(value.slice(0, 32).toByteArray());
    }
  }

  /**
   * Accessor for the byte value in {@link #value()} but in a more natural form (i.e., the size of the returned value
   * will be 32 bytes). Natural ed25519 or secp256k1 private keys will ordinarily contain only 32 bytes. However, in
   * XRPL, private keys are represented with a single-byte prefix (i.e., `0xED` for ed25519 and `0x00` for secp256k1
   * keys).
   *
   * @return An instance of {@link UnsignedByteArray}.
   */
  public UnsignedByteArray valueWithPrefixedBytes() {
    // Check for empty value, which can occur if this PrivateKey is "destroyed" but still in memory.
    if (value.length() == 0) {
      return UnsignedByteArray.empty();
    } else {
      // Note: value.slice() will take a view of the existing UBA, then `.toByteArray()` will perform a copy, which is
      // what we want in order to enforce immutability of this PrivateKey (because Java 8 doesn't support immutable byte
      // arrays).
      switch (keyType) {
        case ED25519: {
          return UnsignedByteArray.of(ED2559_PREFIX).append(value);
        }
        case SECP256K1: {
          return UnsignedByteArray.of(SECP256K1_PREFIX).append(value);
        }
        default: {
          // This should never happen; if it does, there's a bug in this implementation
          throw new IllegalStateException(String.format("Invalid keyType=%s", keyType));
        }
      }
    }
  }

  /**
   * The type of this key (either {@link KeyType#ED25519} or {@link KeyType#SECP256K1}).
   *
   * @return A {@link KeyType}.
   */
  public final KeyType keyType() {
    return this.keyType;
  }

  @Override
  public final void destroy() {
    this.value.destroy();
    this.destroyed = true;
  }

  @Override
  public final boolean isDestroyed() {
    return this.destroyed;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    PrivateKey that = (PrivateKey) obj;
    return Objects.equals(value, that.value) && keyType == that.keyType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, keyType);
  }

  @Override
  public String toString() {
    return String.format("PrivateKey{" +
      "value=[redacted]," +
      "keyType=%s," +
      "destroyed=%s" +
      "}", this.keyType(), this.isDestroyed()
    );
  }
}
