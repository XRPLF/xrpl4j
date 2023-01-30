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
import org.xrpl.xrpl4j.codec.addresses.exceptions.EncodeException;
import org.xrpl.xrpl4j.codec.addresses.exceptions.EncodingFormatException;

import java.util.Objects;
import java.util.Optional;

/**
 * A Codec for encoding/decoding various seed primitives.
 */
@SuppressWarnings( {"OptionalUsedAsFieldOrParameterType", "ParameterName", "MethodName"})
public class SeedCodec {

  private static final SeedCodec INSTANCE = new SeedCodec();

  public static SeedCodec getInstance() {
    return INSTANCE;
  }

  /**
   * Decodes a Base58Check encoded XRPL secret key base58EncodedSeed value. Works for ed25519 and secp256k1 seeds.
   *
   * @param base58EncodedSeed A Base58Check encoded XRPL keypair base58EncodedSeed.
   *
   * @return The decoded base58EncodedSeed, base58EncodedSeed type, and algorithm used to encode the base58EncodedSeed.
   *
   * @see "https://xrpl.org/cryptographic-keys.html#seed"
   */
  public Decoded decodeSeed(final String base58EncodedSeed) throws EncodingFormatException {
    Objects.requireNonNull(base58EncodedSeed);

    return AddressBase58.decode(
      base58EncodedSeed,
      Lists.newArrayList(KeyType.ED25519, KeyType.SECP256K1),
      Lists.newArrayList(Version.ED25519_SEED, Version.FAMILY_SEED),
      Optional.of(UnsignedInteger.valueOf(16))
    );
  }

  /**
   * Encodes a byte array to a Base58Check {@link String} using the given {@link KeyType}.
   *
   * @param entropy An {@link UnsignedByteArray} containing the seed entropy to encode.
   * @param type    The cryptographic algorithm type to be encoded in the resulting seed.
   *
   * @return A Base58Check encoded XRPL keypair seed.
   */
  public String encodeSeed(final UnsignedByteArray entropy, final KeyType type) {
    Objects.requireNonNull(entropy);
    Objects.requireNonNull(type);

    if (entropy.getUnsignedBytes().size() != 16) {
      throw new EncodeException("entropy must have length 16.");
    }

    Version version = type.equals(KeyType.ED25519) ? Version.ED25519_SEED : Version.FAMILY_SEED;
    return AddressBase58.encode(entropy, Lists.newArrayList(version), UnsignedInteger.valueOf(16));
  }
}
