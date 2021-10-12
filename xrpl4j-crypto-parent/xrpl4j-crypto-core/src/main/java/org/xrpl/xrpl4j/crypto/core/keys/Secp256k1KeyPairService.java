package org.xrpl.xrpl4j.crypto.core.keys;

import static org.xrpl.xrpl4j.crypto.core.keys.Secp256k1KeyPairService.Secp256k1.EC_DOMAIN_PARAMETERS;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.xrpl.xrpl4j.codec.addresses.SeedCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.HashingUtils;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of {@link KeyPairService} which uses the ECDSA algorithm with the secp256k1 curve to derive keys and
 * sign/verify signatures.
 */
public class Secp256k1KeyPairService implements KeyPairService {

  /**
   * Static constants for Secp256k1.
   */
  public interface Secp256k1 {

    X9ECParameters EC_PARAMETERS = SECNamedCurves.getByName("secp256k1");
    ECDomainParameters EC_DOMAIN_PARAMETERS = new ECDomainParameters(
      EC_PARAMETERS.getCurve(),
      EC_PARAMETERS.getG(),
      EC_PARAMETERS.getN(),
      EC_PARAMETERS.getH()
    );

  }

  private static final Secp256k1KeyPairService INSTANCE = new Secp256k1KeyPairService();

  private final SeedCodec seedCodec;

  /**
   * Private constructor to enforce singleton pattern.
   */
  private Secp256k1KeyPairService() {
    this.seedCodec = SeedCodec.getInstance();
  }

  /**
   * Accessor for the singleton instance of this service.
   *
   * @return A {@link Secp256k1KeyPairService}.
   */
  public static Secp256k1KeyPairService getInstance() {
    return INSTANCE;
  }

  @Override
  public Seed generateSeed(final Entropy entropy) {
    Objects.requireNonNull(entropy);
    return Seed.secp256k1SeedFromEntropy(entropy);
  }

  @Override
  public KeyPair deriveKeyPair(final Seed seed) {
    Objects.requireNonNull(seed);
    // The seed `bytes` here are actually the bytes of the decoded seed, which reduces to just the entropy.
    return deriveKeyPair(seed.decodedSeed().bytes(), 0);
  }

  /**
   * Note that multiple keyp-airs can be derived from the same seedBytes using the secp2551k algorithm by deriving the
   * keys from a seedBytes and an account index UInt32. However, this use case is incredibly uncommon, and a vast
   * majority of users use 0 for the account index. Thus, this implementation does not allow for custom account indexes
   * for deriving secp2551k keys.
   *
   * @param seedBytes An {@link UnsignedByteArray} of length 16 containing a seedBytes.
   *
   * @return A {@link KeyPair} containing a public/private keypair derived from seedBytes using the secp2561k algorithm.
   */
  private KeyPair deriveKeyPair(final UnsignedByteArray seedBytes, final int accountNumber) {
    Objects.requireNonNull(seedBytes);

    // private key needs to be a BigInteger so we can derive the public key by multiplying G by the private key.
    final BigInteger privateKeyInt = derivePrivateKey(seedBytes, accountNumber);
    final UnsignedByteArray publicKeyInt = derivePublicKey(privateKeyInt);

    return KeyPair.builder()
      .privateKey(PrivateKey.of(UnsignedByteArray.of(privateKeyInt.toByteArray())))
      .publicKey(PublicKey.fromBase16EncodedPublicKey(
        UnsignedByteArray.of(publicKeyInt.toByteArray()).hexValue()
      ))
      .build();
  }

  private UnsignedByteArray derivePublicKey(final BigInteger privateKey) {
    Objects.requireNonNull(privateKey);
    return UnsignedByteArray.of(EC_DOMAIN_PARAMETERS.getG().multiply(privateKey).getEncoded(true));
  }

  private BigInteger derivePrivateKey(final UnsignedByteArray seed, final int accountNumber) {
    Objects.requireNonNull(seed);
    BigInteger privateGen = deriveScalar(seed);
    if (accountNumber == -1) {
      return privateGen;
    }

    UnsignedByteArray publicGen = UnsignedByteArray
      .of(EC_DOMAIN_PARAMETERS.getG().multiply(privateGen).getEncoded(true));
    return deriveScalar(publicGen, accountNumber)
      .add(privateGen)
      .mod(EC_DOMAIN_PARAMETERS.getN());
  }

  private BigInteger deriveScalar(final UnsignedByteArray seed) {
    Objects.requireNonNull(seed);
    return deriveScalar(seed, Optional.empty());
  }

  private BigInteger deriveScalar(final UnsignedByteArray seed, final Integer discriminator) {
    Objects.requireNonNull(seed);
    Objects.requireNonNull(discriminator);
    return deriveScalar(seed, Optional.of(discriminator));
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private BigInteger deriveScalar(final UnsignedByteArray seed, final Optional<Integer> discriminator) {
    Objects.requireNonNull(seed);
    Objects.requireNonNull(discriminator);

    BigInteger key = null;
    UnsignedByteArray seedCopy = UnsignedByteArray.of(seed.toByteArray());
    for (long i = 0; i <= 0xFFFFFFFFL; i++) {
      discriminator.map(d -> HashingUtils.addUInt32(seedCopy, d));
      HashingUtils.addUInt32(seedCopy, (int) i);
      UnsignedByteArray hash = HashingUtils.sha512Half(seedCopy);
      key = new BigInteger(1, hash.toByteArray());
      if (key.compareTo(BigInteger.ZERO) > 0 && key.compareTo(EC_DOMAIN_PARAMETERS.getN()) < 0) {
        break;
      }
    }

    return key;
  }
}
