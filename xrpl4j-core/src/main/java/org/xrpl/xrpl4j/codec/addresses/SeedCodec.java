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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.codec.addresses.exceptions.DecodeException;
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

  // A 16-byte ED25519 or SECP256K1 seed Base58Check-encodes to 31 or 29 characters, respectively; a 32-byte
  // SECP256K1 seed (used only for ElGamal seeds; see #encodeSeed) encodes to 51 characters. No other length is
  // decodable -- e.g. a 32-byte ED25519 seed would encode to 53 characters, but #encodeSeed refuses to produce one.
  private static final ImmutableSet<Integer> VALID_SEED_LENGTHS = ImmutableSet.of(29, 31, 51);

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

    if (!VALID_SEED_LENGTHS.contains(base58EncodedSeed.length())) {
      throw new DecodeException(
        String.format("Invalid seed length %s; expected %s.", base58EncodedSeed.length(), VALID_SEED_LENGTHS)
      );
    }

    // A 32-byte secp256k1 seed (used only for ElGamal seeds; see #encodeSeed) Base58Check-encodes to 51 characters,
    // versus 29 for the standard 16-byte payload. Detect that length here since AddressBase58.decode needs the
    // expected payload size up front to pick the right branch.
    if (base58EncodedSeed.length() == 51) {
      return AddressBase58.decode(
        base58EncodedSeed,
        Lists.newArrayList(KeyType.SECP256K1),
        Lists.newArrayList(Version.FAMILY_SEED),
        Optional.of(UnsignedInteger.valueOf(32))
      );
    }

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

    if (entropy.getUnsignedBytes().size() != 16 && entropy.getUnsignedBytes().size() != 32) {
      throw new EncodeException("entropy must have length 16 or 32.");
    }

    // 32-byte entropy exists only to support ElGamal secp256k1 seeds (see Seed#elGamalSecp256k1SeedFromEntropy).
    // Encoding 32 bytes under the ED25519 prefix yields a 53-character seed that decodeSeed cannot decode -- it
    // matches neither the 51-character secp256k1 branch nor the 16-byte fallback -- so it would be silently
    // unrecoverable. Reject it rather than return unusable key material.
    if (entropy.getUnsignedBytes().size() == 32 && type.equals(KeyType.ED25519)) {
      throw new EncodeException("32-byte entropy is only supported for SECP256K1 seeds, but was ED25519.");
    }

    Version version = type.equals(KeyType.ED25519) ? Version.ED25519_SEED : Version.FAMILY_SEED;
    return AddressBase58.encode(
      entropy, Lists.newArrayList(version), UnsignedInteger.valueOf(entropy.getUnsignedBytes().size())
    );
  }
}
