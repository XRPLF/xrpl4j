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
public class PrivateKeyCodec {

  private static final PrivateKeyCodec INSTANCE = new PrivateKeyCodec();

  public static PrivateKeyCodec getInstance() {
    return INSTANCE;
  }

  /**
   * Encode an XRPL Node Private Key to a Base58Check encoded {@link String}.
   *
   * @param privateKeyBytes An {@link UnsignedByteArray} containing the public key to be encoded.
   *
   * @return The Base58 representation of privateKeyBytes.
   */
  public String encodeNodePrivateKey(final UnsignedByteArray privateKeyBytes) {
    Objects.requireNonNull(privateKeyBytes);

    return AddressBase58.encode(
      privateKeyBytes,
      Lists.newArrayList(Version.NODE_PRIVATE),
      UnsignedInteger.valueOf(32)
    );
  }

  /**
   * Decode a Base58Check encoded XRPL Node Private Key.
   *
   * @param privateKeyBytes The Base58 encoded public key to be decoded.
   *
   * @return An {@link UnsignedByteArray} containing the decoded public key.
   *
   * @see "https://xrpl.org/base58-encodings.html"
   */
  public UnsignedByteArray decodeNodePrivateKey(final String privateKeyBase58) {
    Objects.requireNonNull(privateKeyBytes);

    return AddressBase58.decode(
      privateKeyBytes,
      Lists.newArrayList(Version.NODE_PRIVATE),
      UnsignedInteger.valueOf(32)
    ).bytes();
  }

  /**
   * Encode an XRPL Account Private Key to a Base58Check encoded {@link String}.
   *
   * @param privateKeyBytes An {@link UnsignedByteArray} containing the public key to be encoded.
   *
   * @return The Base58 representation of privateKeyBytes.
   */
  public String encodeAccountPrivateKey(final UnsignedByteArray privateKeyBytes) {
    Objects.requireNonNull(privateKeyBytes);

    return AddressBase58.encode(
      privateKeyBytes,
      Lists.newArrayList(Version.ACCOUNT_SECRET_KEY),
      UnsignedInteger.valueOf(32)
    );
  }

  /**
   * Decode a Base58Check encoded XRPL Account Private Key.
   *
   * @param publicKey The Base58 encoded public key to be decoded.
   *
   * @return An {@link UnsignedByteArray} containing the decoded public key.
   *
   * @see "https://xrpl.org/base58-encodings.html"
   */
  public UnsignedByteArray decodeAccountPrivateKey(final String privateKeyBase58) {
    Objects.requireNonNull(publicKey);

    return AddressBase58.decode(
      publicKey,
      Lists.newArrayList(Version.ACCOUNT_SECRET_KEY),
      UnsignedInteger.valueOf(32)
    ).bytes();
  }

}
