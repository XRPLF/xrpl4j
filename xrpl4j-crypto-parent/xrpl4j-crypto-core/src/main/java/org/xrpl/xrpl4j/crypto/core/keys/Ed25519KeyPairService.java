package org.xrpl.xrpl4j.crypto.core.keys;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.xrpl.xrpl4j.codec.addresses.Decoded;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.Version;
import org.xrpl.xrpl4j.codec.addresses.exceptions.DecodeException;
import org.xrpl.xrpl4j.crypto.core.HashingUtils;

import java.util.Objects;

/**
 * Implementation of {@link KeyPairService} which uses the ED25519 algorithm to derive keys and sign/verify signatures.
 */
public class Ed25519KeyPairService implements KeyPairService {

  private static final Ed25519KeyPairService INSTANCE = new Ed25519KeyPairService();

  /**
   * No-args Constructor.
   */
  private Ed25519KeyPairService() {
  }

  public static Ed25519KeyPairService getInstance() {
    return INSTANCE;
  }

  @Override
  public Seed generateSeed(final Entropy entropy) {
    Objects.requireNonNull(entropy);
    return Seed.ed25519SeedFromEntropy(entropy);
  }

  @Override
  public KeyPair deriveKeyPair(final Seed seed) {
    Objects.requireNonNull(seed);

    final Decoded decoded = seed.decodedSeed();
    if (!decoded.version().equals(Version.ED25519_SEED)) {
      throw new DecodeException("Seed must use ED25519 algorithm. Algorithm was " + decoded.version());
    }

    return deriveKeyPair(decoded.bytes());
  }

  private KeyPair deriveKeyPair(UnsignedByteArray seed) {
    UnsignedByteArray rawPrivateKey = HashingUtils.sha512Half(seed);
    Ed25519PrivateKeyParameters privateKey = new Ed25519PrivateKeyParameters(rawPrivateKey.toByteArray(), 0);

    Ed25519PublicKeyParameters publicKey = privateKey.generatePublicKey();

    // XRPL ED25519 keys are prefixed with 0xED so that the keys are 33 bytes and match the length of sekp256k1 keys.
    // Bouncy Castle only deals with 32 byte keys, so we need to manually add the prefix
    final UnsignedByte prefix = UnsignedByte.of(0xED);
    final UnsignedByteArray prefixedPrivateKey = UnsignedByteArray.of(prefix)
      .append(UnsignedByteArray.of(privateKey.getEncoded()));
    final UnsignedByteArray prefixedPublicKey = UnsignedByteArray.of(prefix)
      .append(UnsignedByteArray.of(publicKey.getEncoded()));

    return KeyPair.builder()
      .privateKey(PrivateKey.of(prefixedPrivateKey))
      .publicKey(PublicKey.fromBase16EncodedPublicKey(prefixedPublicKey.hexValue()))
      .build();
  }
}
