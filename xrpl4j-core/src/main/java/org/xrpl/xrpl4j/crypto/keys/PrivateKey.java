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
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.util.Objects;

/**
 * A typed instance of an XRPL private-key.
 */
public class PrivateKey implements PrivateKeyable, javax.security.auth.Destroyable {

  /**
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
  private boolean destroyed;

  /**
   * Instantiates a new instance of {@link PrivateKey} using the supplied bytes.
   *
   * @param value An {@link UnsignedByteArray} containing this key's binary value.
   *
   * @return A {@link PrivateKey}.
   */
  public static PrivateKey of(final UnsignedByteArray value) {
    Objects.requireNonNull(value);

    final UnsignedByte firstByte = value.get(0);
    Preconditions.checkArgument(
      value.length() == 33 && (ED2559_PREFIX.equals(firstByte) || SECP256K1_PREFIX.equals(firstByte)),
      "Constructing a PrivateKey with raw bytes requires a one-byte prefix in front of the 32 natural " +
        "bytes of a private key. Use the prefix `0xED` for ed25519 private keys, or `0x00` for secp256k1 private keys."
    );

    return new PrivateKey(value);
  }

  /**
   * Required-args Constructor.
   *
   * @param value An {@link UnsignedByteArray} for this key's value.
   */
  private PrivateKey(final UnsignedByteArray value) {
    this.value = Objects.requireNonNull(value);
  }

  /**
   * Accessor for the key value, in binary (Note: will be 33 bytes).
   *
   * @return An instance of {@link UnsignedByteArray}.
   */
  public UnsignedByteArray value() {
    return UnsignedByteArray.of(value.toByteArray());
  }

  /**
   * Accessor for the byte value in {@link #value()} but in a more natural form (i.e., the size of the returned value
   * will be 32 bytes). Natural ed25519 or secp256k1 private keys will ordinarily contain only 32 bytes. However, in
   * XRPL, private keys are represented with a single-byte prefix (i.e., `0xED` for ed25519 and `0x00` for secp256k1
   * keys).
   *
   * @return An instance of {@link UnsignedByteArray}.
   */
  public UnsignedByteArray valueWithoutPrefix() {
    // Note: value.slice() will take a view of the existing UBA, then `.toByteArray()` will perform a copy, which is
    // what we want in order to enforce immutability of this PrivateKey (because Java 8 doesn't support immutable byte
    // arrays).
    return UnsignedByteArray.of(value.slice(1, 33).toByteArray());
  }

  /**
   * The type of this key (either {@link KeyType#ED25519} or {@link KeyType#SECP256K1}).
   *
   * @return A {@link KeyType}.
   */
  public final KeyType keyType() {
    final UnsignedByte prefixByte = this.value().get(0);
    if (ED2559_PREFIX.equals(prefixByte)) {
      return KeyType.ED25519;
    } else if (SECP256K1_PREFIX.equals(prefixByte)) {
      return KeyType.SECP256K1;
    } else {
      throw new IllegalStateException("Prefix may only be 0xED or 0x00");
    }
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
    if (!(obj instanceof PrivateKey)) {
      return false;
    }

    PrivateKey that = (PrivateKey) obj;

    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return "PrivateKey{" +
      "value=[redacted]" +
      ", destroyed=" + destroyed +
      '}';
  }
}
