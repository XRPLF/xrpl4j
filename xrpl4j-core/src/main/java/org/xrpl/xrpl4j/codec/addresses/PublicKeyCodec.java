package org.xrpl.xrpl4j.codec.addresses;

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

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;

import java.util.Objects;

/**
 * A Codec for encoding/decoding various seed primitives.
 */
public class PublicKeyCodec {

  private static final PublicKeyCodec INSTANCE = new PublicKeyCodec();

  public static PublicKeyCodec getInstance() {
    return INSTANCE;
  }

  /**
   * Encode an XRPL Node Public Key to a Base58Check encoded {@link String}.
   *
   * @param publicKey An {@link UnsignedByteArray} containing the public key to be encoded.
   *
   * @return The Base58 representation of publicKey.
   */
  public String encodeNodePublicKey(final UnsignedByteArray publicKey) {
    Objects.requireNonNull(publicKey);

    return AddressBase58.encode(
      publicKey,
      Lists.newArrayList(Version.NODE_PUBLIC),
      UnsignedInteger.valueOf(33)
    );
  }

  /**
   * Decode a Base58Check encoded XRPL Node Public Key.
   *
   * @param publicKey The Base58 encoded public key to be decoded.
   *
   * @return An {@link UnsignedByteArray} containing the decoded public key.
   *
   * @see "https://xrpl.org/base58-encodings.html"
   */
  public UnsignedByteArray decodeNodePublicKey(final String publicKey) {
    Objects.requireNonNull(publicKey);

    return AddressBase58.decode(
      publicKey,
      Lists.newArrayList(Version.NODE_PUBLIC),
      UnsignedInteger.valueOf(33)
    ).bytes();
  }

  /**
   * Encode an XRPL Account Public Key to a Base58Check encoded {@link String}.
   *
   * @param publicKey An {@link UnsignedByteArray} containing the public key to be encoded.
   *
   * @return The Base58 representation of publicKey.
   */
  public String encodeAccountPublicKey(final UnsignedByteArray publicKey) {
    Objects.requireNonNull(publicKey);

    return AddressBase58.encode(
      publicKey,
      Lists.newArrayList(Version.ACCOUNT_PUBLIC_KEY),
      UnsignedInteger.valueOf(33)
    );
  }

  /**
   * Decode a Base58Check encoded XRPL Account Public Key.
   *
   * @param publicKey The Base58 encoded public key to be decoded.
   *
   * @return An {@link UnsignedByteArray} containing the decoded public key.
   *
   * @see "https://xrpl.org/base58-encodings.html"
   */
  public UnsignedByteArray decodeAccountPublicKey(final String publicKey) {
    Objects.requireNonNull(publicKey);

    return AddressBase58.decode(
      publicKey,
      Lists.newArrayList(Version.ACCOUNT_PUBLIC_KEY),
      UnsignedInteger.valueOf(33)
    ).bytes();
  }

}
