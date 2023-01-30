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

import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.util.Objects;

/**
 * A typed instance of an XRPL private-key.
 */
public class PrivateKey implements PrivateKeyable, javax.security.auth.Destroyable {

  /**
   * Keys generated from the secp256k1 curve have 33 bytes in XRP Ledger. However, keys derived from the ed25519 curve
   * have only 32 bytes, and so get prefixed with this HEX value so that all keys in the ledger are 33 bytes.
   */
  public static final UnsignedByte PREFIX = UnsignedByte.of(0xED);

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
   * The type of this key (either {@link KeyType#ED25519} or {@link KeyType#SECP256K1}).
   *
   * @return A {@link KeyType}.
   */
  public final KeyType keyType() {
    final UnsignedByte prefixByte = this.value().get(0);
    return prefixByte.equals(PREFIX) ? KeyType.ED25519 : KeyType.SECP256K1;
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
