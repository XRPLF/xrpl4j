package org.xrpl.xrpl4j.crypto;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: crypto :: core
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

import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.xrpl.xrpl4j.codec.addresses.Base58;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.util.Objects;

/**
 * A typed instance of an XRPL private-key.
 *
 * @deprecated consider using the variant from org.xrpl.xrpl4j.crypto.core.
 */
@Deprecated
public interface PrivateKey {

  /**
   * Keys generated from the secp256k1 curve have 33 bytes in XRP Ledger. However, keys derived from the ed25519 curve
   * have only 32 bytes, and so get prefixed with this HEX value so that all keys in the ledger are 33 bytes.
   */
  UnsignedByte PREFIX = UnsignedByte.of(0xED);

  /**
   * Instantiates a new builder.
   *
   * @return A {@link ImmutableDefaultPrivateKey.Builder}.
   */
  static ImmutableDefaultPrivateKey.Builder builder() {
    return ImmutableDefaultPrivateKey.builder();
  }

  /**
   * Construct a {@link PrivateKey} from a base58-encoded {@link String}.
   *
   * @param base58EncodedPrivateKey A base58-encoded {@link String}.
   *
   * @return A {@link PrivateKey}.
   */
  static PrivateKey fromBase58EncodedPrivateKey(final String base58EncodedPrivateKey) {
    return PrivateKey.builder()
      .value(UnsignedByteArray.of(Base58.decode(base58EncodedPrivateKey)))
      .build();
  }

  /**
   * Construct a {@link PrivateKey} from a base16-encoded (HEX) {@link String}.
   *
   * @param base16EncodedPrivateKey A base16-encoded (HEX) {@link String}.
   *
   * @return A {@link PrivateKey}.
   */
  static PrivateKey fromBase16EncodedPrivateKey(final String base16EncodedPrivateKey) {
    Objects.requireNonNull(base16EncodedPrivateKey);

    return PrivateKey.builder()
      .value(UnsignedByteArray.of(BaseEncoding.base16().decode(base16EncodedPrivateKey.toUpperCase())))
      .build();
  }

  /**
   * The key, in binary (Note: will be 33 bytes).
   *
   * @return An instance of {@link UnsignedByteArray}.
   */
  UnsignedByteArray value();

  /**
   * The key, as a Base58-encoded string.
   *
   * @return A {@link String}.
   */
  String base58Encoded();

  /**
   * The key, as a Base16-encoded (i.e., HEX) string. Note that if this is an Ed25519 private-key, then this value
   * contains a leading prefix of `ED`, in hex.
   *
   * @return A {@link String}.
   */
  String base16Encoded();

  /**
   * The type of this key (either {@link KeyType#ED25519} or {@link KeyType#SECP256K1}).
   *
   * @return A {@link KeyType}.
   */
  KeyType versionType();

  /**
   * Abstract implementation for immutables.
   */
  @Value.Immutable
  abstract class DefaultPrivateKey implements PrivateKey {

    @Override
    @Derived
    public String base58Encoded() {
      return Base58.encode(value().toByteArray());
    }

    @Override
    @Derived
    public String base16Encoded() {
      return this.value().hexValue();
    }

    @Derived
    @Override
    public KeyType versionType() {
      return this.base16Encoded().startsWith("ED") ? KeyType.ED25519 : KeyType.SECP256K1;
    }

    @Override
    public String toString() {
      return base58Encoded();
    }
  }

}
