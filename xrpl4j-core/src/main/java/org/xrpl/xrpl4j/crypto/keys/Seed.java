package org.xrpl.xrpl4j.crypto.keys;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.primitives.UnsignedInteger;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.xrpl.xrpl4j.codec.addresses.AddressBase58;
import org.xrpl.xrpl4j.codec.addresses.Base58;
import org.xrpl.xrpl4j.codec.addresses.Decoded;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.SeedCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.Version;
import org.xrpl.xrpl4j.codec.addresses.exceptions.DecodeException;
import org.xrpl.xrpl4j.crypto.HashingUtils;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

/**
 * A compact value that is used to derive the actual private and public keys for an XRPL account.
 *
 * @see "https://xrpl.org/cryptographic-keys.html#seed"
 */
public interface Seed extends javax.security.auth.Destroyable {

  /**
   * The decoded details of this seed.
   *
   * @return An instance of {@link Decoded}.
   */
  Decoded decodedSeed();

  /**
   * Derive a public/private keypair from a Base58Check encoded 16 byte seed.
   *
   * @return The {@link KeyPair} derived from the seed.
   */
  KeyPair deriveKeyPair();

  /**
   * Construct an Ed25519-compatible {@link Seed} from the supplied {@link Passphrase}.
   *
   * @param passphrase A {@link Passphrase} to generate a seed from.
   *
   * @return A {@link Seed}.
   */
  static Seed ed25519SeedFromPassphrase(final Passphrase passphrase) {
    Objects.requireNonNull(passphrase);

    final byte[] entropyBytes = new byte[16];

    // 16 bytes of deterministic entropy.
    Hashing.sha512()
      .hashBytes(passphrase.value())
      .writeBytesTo(entropyBytes, 0, 16);

    return ed25519SeedFromEntropy(Entropy.of(entropyBytes));
  }

  /**
   * Construct an secp256k1-compatible {@link Seed} from the supplied {@link Passphrase}.
   *
   * @param passphrase A {@link Passphrase} to generate a seed from.
   *
   * @return A {@link Seed}.
   */
  static Seed secp256k1SeedFromPassphrase(final Passphrase passphrase) {
    Objects.requireNonNull(passphrase);

    final byte[] entropyBytes = new byte[16];

    // 16 bytes of deterministic entropy.
    Hashing.sha512()
      .hashBytes(passphrase.value())
      .writeBytesTo(entropyBytes, 0, 16);

    return secp256k1SeedFromEntropy(Entropy.of(entropyBytes));
  }

  /**
   * Construct an Ed25519-compatible {@link Seed} using a random {@link Entropy} instance. This random {@link Entropy}
   * is created using {@link Entropy#newInstance()}.
   *
   * @return A {@link Seed}.
   */
  static Seed ed25519Seed() {
    return ed25519SeedFromEntropy(Entropy.newInstance());
  }

  /**
   * Construct an Ed25519-compatible {@link Seed} from the supplied {@link Entropy}.
   *
   * @param entropy A {@link Entropy} to generate a {@link Seed} from.
   *
   * @return A {@link Seed}.
   */
  static Seed ed25519SeedFromEntropy(final Entropy entropy) {
    Objects.requireNonNull(entropy);

    final String base58EncodedSeed = AddressBase58.encode(
      entropy.value(),
      Lists.newArrayList(Version.ED25519_SEED),
      UnsignedInteger.valueOf(entropy.value().length())
    );

    return new DefaultSeed(UnsignedByteArray.of(AddressBase58.decode(base58EncodedSeed)));
  }

  /**
   * Construct a secp256k1-compatible {@link Seed} using a random {@link Entropy} instance. This random {@link Entropy}
   * is created using {@link Entropy#newInstance()}.
   *
   * @return A {@link Seed}.
   */
  static Seed secp256k1Seed() {
    return secp256k1SeedFromEntropy(Entropy.newInstance());
  }

  /**
   * Construct a secp256k1-compatible {@link Seed} from the supplied {@link Entropy}.
   *
   * @param entropy A {@link Entropy} to generate a {@link Seed} from.
   *
   * @return A {@link Seed}.
   */
  static Seed secp256k1SeedFromEntropy(final Entropy entropy) {
    Objects.requireNonNull(entropy);

    final String base58EncodedSeed = AddressBase58.encode(
      entropy.value(),
      Lists.newArrayList(Version.FAMILY_SEED),
      UnsignedInteger.valueOf(entropy.value().length())
    );

    return new DefaultSeed(UnsignedByteArray.of(Base58.decode(base58EncodedSeed)));
  }

  /**
   * Construct a {@link Seed} from the supplied {@code base58EncodedSecret}. Values for this function are most commonly
   * found from an XRP Faucet, for example for the XRP devnet or testnet. On the xrpl.org documentation page, this value
   * is often referred to as an account "secret", but it is actually just a base58-encoded string that contains an
   * encoded 16-bytes of entropy, in addition to other binary padding and identification data.
   *
   * @param base58EncodedSecret A base58-encoded {@link String} that represents an encoded seed.
   *
   * @return A {@link Seed}.
   * @see "https://xrpl.org/xrp-testnet-faucet.html"
   */
  static Seed fromBase58EncodedSecret(final Base58EncodedSecret base58EncodedSecret) {
    Objects.requireNonNull(base58EncodedSecret);

    return new DefaultSeed(base58EncodedSecret.decodedValueBytes());
  }

  /**
   * A default implementation of a {@link Seed}.
   */
  class DefaultSeed implements Seed {

    // This holds all Seed bytes (e.g., entropy bytes, version type, etc).
    private final UnsignedByteArray value;
    private boolean destroyed;

    /**
     * Required-args Constructor. Purposefully package-private for testing purposes only (use a static method instead
     * for construction).
     *
     * @param value This seed's full binary value (including the entropy bytes and keyType).
     */
    @VisibleForTesting
    DefaultSeed(final UnsignedByteArray value) {
      this.value = Objects.requireNonNull(value);
    }

    /**
     * Copy constructor.
     *
     * @param seed This seed's full binary value (including the entropy bytes and keyType).
     */
    @VisibleForTesting
    DefaultSeed(final DefaultSeed seed) {
      Objects.requireNonNull(seed);
      this.value = UnsignedByteArray.of(seed.value.toByteArray());
      this.destroyed = seed.isDestroyed();
    }

    /**
     * The decoded details of this seed.
     *
     * @return An instance of {@link Decoded}.
     */
    public Decoded decodedSeed() {
      final byte[] copiedByteValue = new byte[this.value.length()];
      System.arraycopy(this.value.toByteArray(), 0, copiedByteValue, 0, value.length());

      return SeedCodec.getInstance().decodeSeed(
        Base58.encode(copiedByteValue)
      );
    }

    @Override
    public KeyPair deriveKeyPair() {
      KeyType type = this.decodedSeed().type()
        .orElseThrow(() -> new IllegalArgumentException("Unsupported seed type."));

      switch (type) {
        case ED25519: {
          return Ed25519KeyPairService.deriveKeyPair(this);
        }
        case SECP256K1: {
          return Secp256k1KeyPairService.deriveKeyPair(this);
        }
        default: {
          throw new IllegalArgumentException("Unsupported seed type.");
        }
      }
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
      if (!(obj instanceof Seed)) {
        return false;
      }

      Seed that = (Seed) obj;
      return that.decodedSeed().equals(this.decodedSeed());
    }

    @Override
    public int hashCode() {
      return value.hashCode();
    }

    @Override
    public String toString() {
      return "Seed{" +
        "value=[redacted]" +
        ", destroyed=" + destroyed +
        '}';
    }

    /**
     * Encapsulates algorithms to derive ED25519 keys.
     */
    @VisibleForTesting
    static class Ed25519KeyPairService {

      /**
       * Private, no-args constructor to prevent instantiation.
       */
      private Ed25519KeyPairService() {
      }

      /**
       * Derive a {@link KeyPair} from the supplied {@code seed}.
       *
       * @param seed A {@link Seed}.
       *
       * @return A newly generated {@link KeyPair}.
       */
      public static KeyPair deriveKeyPair(final Seed seed) {
        Objects.requireNonNull(seed);

        final Decoded decoded = seed.decodedSeed();
        if (!decoded.version().equals(Version.ED25519_SEED)) {
          throw new DecodeException("Seed must use ED25519 algorithm. Algorithm was " + decoded.version());
        }

        UnsignedByteArray rawPrivateKey = HashingUtils.sha512Half(decoded.bytes());
        Ed25519PrivateKeyParameters privateKey = new Ed25519PrivateKeyParameters(rawPrivateKey.toByteArray(), 0);

        Ed25519PublicKeyParameters publicKey = privateKey.generatePublicKey();

        // XRPL ED25519 keys are prefixed with 0xED so that the keys are 33 bytes and match the length of secp256k1
        // keys. Note that Bouncy Castle only deals with 32 byte keys, so we need to manually add the prefix
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

    /**
     * Encapsulates algorithms to derive ECDSA keys using the secp256k1 curve.
     */
    @VisibleForTesting
    static class Secp256k1KeyPairService {

      /**
       * Static constants for Secp256k1.
       */
      static X9ECParameters EC_PARAMETERS = SECNamedCurves.getByName("secp256k1");
      static ECDomainParameters EC_DOMAIN_PARAMETERS = new ECDomainParameters(
        EC_PARAMETERS.getCurve(),
        EC_PARAMETERS.getG(),
        EC_PARAMETERS.getN(),
        EC_PARAMETERS.getH()
      );

      /**
       * Private, no-args constructor to prevent instantiation.
       */
      private Secp256k1KeyPairService() {
      }

      /**
       * Derive a {@link KeyPair} from the supplied {@code seed}.
       *
       * @param seed A {@link Seed}.
       *
       * @return A newly generated {@link KeyPair}.
       */
      public static KeyPair deriveKeyPair(final Seed seed) {
        Objects.requireNonNull(seed);
        // The seed `bytes` here are actually the bytes of the decoded seed, which reduces to just the entropy.
        return deriveKeyPair(seed.decodedSeed().bytes(), 0);
      }

      /**
       * Derive a {@link KeyPair} from the supplied {@code seed} as an {@link UnsignedByteArray}.
       * <p>
       * Note that multiple keypairs can be derived from the same {@code seedBytes} using the secp256k1 algorithm by
       * deriving keys from a {@code seedBytes} and an account index of type {@code UInt32}. However, this use-case is
       * incredibly uncommon, and a vast majority of users use 0 for the account index. Thus, while this function allows
       * for an account index to be supplied, this implementation does not allow for custom account indexes for deriving
       * secp256k1 keys and always uses  value of `0`.
       * </p>
       *
       * @param seedBytes An {@link UnsignedByteArray} of length 16 containing a seedBytes.
       *
       * @return A {@link KeyPair} containing a public/private keypair derived from seedBytes using the secp2561k
       *   algorithm.
       */
      private static KeyPair deriveKeyPair(final UnsignedByteArray seedBytes, final int accountNumber) {
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

      /**
       * Derive a public key from the supplied {@code privateKey}.
       *
       * @param privateKey A {@link BigInteger} representing the private key component of a secp256k1 keypair.
       *
       * @return An {@link UnsignedInteger} representing the public key component of a secp256k1 keypair.
       */
      private static UnsignedByteArray derivePublicKey(final BigInteger privateKey) {
        Objects.requireNonNull(privateKey);
        return UnsignedByteArray.of(EC_DOMAIN_PARAMETERS.getG().multiply(privateKey).getEncoded(true));
      }

      /**
       * Derive a public key from the supplied {@code seed} and {@code accountNumber}.
       *
       * @param seed          A {@link UnsignedByteArray} representing a seed that can be used to generated an XRPL
       *                      address.
       * @param accountNumber An integer representing the account nunmber.
       *
       * @return An {@link UnsignedInteger} representing the public key component of a secp256k1 keypair.
       */
      private static BigInteger derivePrivateKey(final UnsignedByteArray seed, final int accountNumber) {
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

      /**
       * Helper to derive a scalar component for secp256k1.
       *
       * @param seed A {@link UnsignedByteArray} representing a seed.
       *
       * @return An {@link BigInteger} representing a corresponding scalar value.
       */
      private static BigInteger deriveScalar(final UnsignedByteArray seed) {
        Objects.requireNonNull(seed);
        return deriveScalar(seed, Optional.empty());
      }

      /**
       * Helper to derive a scalar component for secp256k1.
       *
       * @param seed          A {@link UnsignedByteArray} representing a seed.
       * @param discriminator An integer used as a discriminator for deriving a scalar.
       *
       * @return An {@link BigInteger} representing a corresponding scalar value.
       */
      private static BigInteger deriveScalar(final UnsignedByteArray seed, final Integer discriminator) {
        Objects.requireNonNull(seed);
        Objects.requireNonNull(discriminator);
        return deriveScalar(seed, Optional.of(discriminator));
      }

      /**
       * Helper to derive a scalar component for secp256k1.
       *
       * @param seed          A {@link UnsignedByteArray} representing a seed.
       * @param discriminator An optionally present {@link Integer} used as a discriminator for deriving a scalar.
       *
       * @return An {@link BigInteger} representing a corresponding scalar value.
       */
      @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
      private static BigInteger deriveScalar(final UnsignedByteArray seed, final Optional<Integer> discriminator) {
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

  }
}