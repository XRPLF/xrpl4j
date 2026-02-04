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
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.util.Objects;
import javax.security.auth.Destroyable;

/**
 * An in-memory ElGamal private key (32-byte secp256k1 scalar).
 *
 * <p>This class holds the actual private key bytes in memory and implements
 * {@link Destroyable} to allow secure cleanup of sensitive material.</p>
 *
 * <p><strong>WARNING:</strong> This implementation holds private key material in memory.
 * For server-side applications, consider using {@link ElGamalPrivateKeyReference} instead
 * to keep private keys in an external system (HSM/KMS).</p>
 *
 * @see ElGamalPrivateKeyable
 * @see ElGamalPrivateKeyReference
 */
public final class ElGamalPrivateKey implements ElGamalPrivateKeyable, Destroyable {

  /**
   * The length of an ElGamal private key in bytes (secp256k1 scalar).
   */
  public static final int KEY_LENGTH = 32;

  private final UnsignedByteArray value;
  private boolean destroyed;

  /**
   * Private constructor. Use static factory methods to create instances.
   *
   * @param value The 32-byte private key value.
   */
  private ElGamalPrivateKey(final UnsignedByteArray value) {
    // Copy to ensure immutability
    this.value = UnsignedByteArray.of(value.toByteArray());
    this.destroyed = false;
  }

  /**
   * Creates an {@link ElGamalPrivateKey} from raw bytes.
   *
   * @param value The 32-byte private key as an {@link UnsignedByteArray}.
   *
   * @return An {@link ElGamalPrivateKey}.
   *
   * @throws NullPointerException     if value is null.
   * @throws IllegalArgumentException if value is not exactly 32 bytes.
   */
  public static ElGamalPrivateKey of(final UnsignedByteArray value) {
    Objects.requireNonNull(value, "value must not be null");
    Preconditions.checkArgument(
        value.length() == KEY_LENGTH,
        "ElGamal private key must be %s bytes, but was %s bytes",
        KEY_LENGTH,
        value.length()
    );
    return new ElGamalPrivateKey(value);
  }

  /**
   * Creates an {@link ElGamalPrivateKey} from a byte array.
   *
   * @param value The 32-byte private key as a byte array.
   *
   * @return An {@link ElGamalPrivateKey}.
   *
   * @throws NullPointerException     if value is null.
   * @throws IllegalArgumentException if value is not exactly 32 bytes.
   */
  public static ElGamalPrivateKey of(final byte[] value) {
    Objects.requireNonNull(value, "value must not be null");
    return of(UnsignedByteArray.of(value));
  }

  /**
   * Returns the 32-byte private key value.
   *
   * <p>This method returns a defensive copy of the internal bytes to ensure
   * immutability of this key instance.</p>
   *
   * @return A copy of the private key bytes as an {@link UnsignedByteArray},
   *         or an empty array if this key has been destroyed.
   */
  public UnsignedByteArray naturalBytes() {
    if (destroyed) {
      return UnsignedByteArray.empty();
    }
    // Return a defensive copy
    return UnsignedByteArray.of(value.toByteArray());
  }

  @Override
  public void destroy() {
    this.value.destroy();
    this.destroyed = true;
  }

  @Override
  public boolean isDestroyed() {
    return destroyed;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ElGamalPrivateKey that = (ElGamalPrivateKey) obj;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "ElGamalPrivateKey{value=[redacted], destroyed=" + destroyed + "}";
  }
}

