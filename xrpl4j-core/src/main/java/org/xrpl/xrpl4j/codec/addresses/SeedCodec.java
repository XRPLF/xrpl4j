package org.xrpl.xrpl4j.codec.addresses;

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
      Lists.newArrayList(VersionType.ED25519, VersionType.SECP256K1),
      Lists.newArrayList(Version.ED25519_SEED, Version.FAMILY_SEED),
      Optional.of(UnsignedInteger.valueOf(16))
    );
  }

  /**
   * Encodes a byte array to a Base58Check {@link String} using the given {@link VersionType}.
   *
   * @param entropy An {@link UnsignedByteArray} containing the seed entropy to encode.
   * @param type    The cryptographic algorithm type to be encoded in the resulting seed.
   *
   * @return A Base58Check encoded XRPL keypair seed.
   */
  public String encodeSeed(final UnsignedByteArray entropy, final VersionType type) {
    Objects.requireNonNull(entropy);
    Objects.requireNonNull(type);

    if (entropy.getUnsignedBytes().size() != 16) {
      throw new EncodeException("entropy must have length 16.");
    }

    Version version = type.equals(VersionType.ED25519) ? Version.ED25519_SEED : Version.FAMILY_SEED;
    return AddressBase58.encode(entropy, Lists.newArrayList(version), UnsignedInteger.valueOf(16));
  }
}
