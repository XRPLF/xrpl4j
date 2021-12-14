package org.xrpl.xrpl4j.crypto.bc.signing;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.bc.BcAddressUtils;
import org.xrpl.xrpl4j.crypto.bc.keys.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.bc.keys.Ed25519KeyPairService;
import org.xrpl.xrpl4j.crypto.bc.keys.Secp256k1KeyPairService;
import org.xrpl.xrpl4j.crypto.core.AddressUtils;
import org.xrpl.xrpl4j.crypto.core.HashingUtils;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.ServerSecret;
import org.xrpl.xrpl4j.crypto.core.ServerSecretSupplier;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.crypto.core.signing.AbstractDelegatedSignatureService;
import org.xrpl.xrpl4j.crypto.core.signing.AbstractDelegatedTransactionSigner;
import org.xrpl.xrpl4j.crypto.core.signing.AbstractDelegatedTransactionVerifier;
import org.xrpl.xrpl4j.crypto.core.signing.DelegatedSignatureService;
import org.xrpl.xrpl4j.crypto.core.signing.Signature;
import org.xrpl.xrpl4j.crypto.core.signing.SignatureUtils;
import org.xrpl.xrpl4j.crypto.core.signing.SignatureWithKeyMetadata;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSingedTransaction;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.math.BigInteger;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * An implementation of {@link DelegatedSignatureService} that uses an in-memory secret key in order to
 * deterministically create a seed value that can then be used to generate XRPL private keys. This implementation keeps
 * a cache of instances of {@link DelegatedSignatureService} that it delegates to based upon the {@link KeyMetadata}
 * supplied on each call.
 *
 * <p>WARNING: This implementation stores private seed-generation material in-memory. Depending on your security
 * requirements, consider a different implementation of {@link DelegatedSignatureService}.</p>
 */
@SuppressWarnings("UnstableApiUsage")
public class DerivedKeyDelegatedSignatureService implements DelegatedSignatureService {

  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

  private final VersionType versionType;
  // TODO: Maybe just make this a KeyPairService?
  private final Ed25519KeyPairService ed25519KeyPairService;
  private final Secp256k1KeyPairService secp256k1KeyPairService;

  private final LoadingCache<KeyMetadata, SingleKeyDelegatedSignatureService> keyMetadataLoadingCache;

  private final ServerSecretSupplier serverSecretSupplier;

  /**
   * Required-args Constructor for use in development mode.
   *
   * @param serverSecretSupplier A {@link ServerSecretSupplier} that can be used to generate seed values, which can then
   *                             be used to generate private keys.
   * @param versionType          A {@link VersionType} that defines which type of key this signature service uses.
   */
  public DerivedKeyDelegatedSignatureService(
    final ServerSecretSupplier serverSecretSupplier,
    final VersionType versionType
  ) {
    this(
      serverSecretSupplier,
      versionType,
      Ed25519KeyPairService.getInstance(),
      Secp256k1KeyPairService.getInstance()
    );
  }

  /**
   * Required-args Constructor.
   *
   * @param serverSecretSupplier    A {@link ServerSecretSupplier} that can be used to generate seed values, which can
   * @param versionType             A {@link VersionType} that defines which type of key this signature service uses.
   * @param ed25519KeyPairService   An {@link Ed25519KeyPairService}.
   * @param secp256k1KeyPairService A {@link Secp256k1KeyPairService}.
   */
  public DerivedKeyDelegatedSignatureService(
    final ServerSecretSupplier serverSecretSupplier,
    final VersionType versionType,
    final Ed25519KeyPairService ed25519KeyPairService,
    final Secp256k1KeyPairService secp256k1KeyPairService
  ) {
    this(
      serverSecretSupplier,
      versionType,
      ed25519KeyPairService,
      secp256k1KeyPairService,
      CaffeineSpec.parse("maximumSize=10000,expireAfterWrite=30s")
    );
  }

  /**
   * Required-args Constructor.
   *
   * @param serverSecretSupplier    A {@link ServerSecretSupplier} that can be used to generate seed values, which can
   * @param versionType             A {@link VersionType} that defines which type of key this signature service uses.
   * @param ed25519KeyPairService   An {@link Ed25519KeyPairService}.
   * @param secp256k1KeyPairService A {@link Secp256k1KeyPairService}.
   * @param caffeineSpec            A {@link CaffeineSpec} that can be initialized externally to configure the Caffeine
   *                                cache constructed by this service.
   */
  public DerivedKeyDelegatedSignatureService(
    final ServerSecretSupplier serverSecretSupplier,
    final VersionType versionType,
    final Ed25519KeyPairService ed25519KeyPairService,
    final Secp256k1KeyPairService secp256k1KeyPairService,
    final CaffeineSpec caffeineSpec
  ) {
    this.serverSecretSupplier = Objects.requireNonNull(serverSecretSupplier);
    this.versionType = Objects.requireNonNull(versionType);
    this.ed25519KeyPairService = Objects.requireNonNull(ed25519KeyPairService);
    this.secp256k1KeyPairService = Objects.requireNonNull(secp256k1KeyPairService);
    this.keyMetadataLoadingCache = Caffeine
      .from(Objects.requireNonNull(caffeineSpec))
      .build(this::constructSignatureService);
  }

  @Override
  public <T extends Transaction> SingleSingedTransaction<T> sign(
    final KeyMetadata keyMetadata, final T transaction
  ) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(transaction);

    return getSignatureServiceSafe(keyMetadata).sign(keyMetadata, transaction);
  }

  @Override
  public Signature sign(KeyMetadata keyMetadata, UnsignedClaim unsignedClaim) {
    return getSignatureServiceSafe(keyMetadata).sign(keyMetadata, unsignedClaim);
  }

  @Override
  public <T extends Transaction> SignatureWithKeyMetadata multiSign(
    final KeyMetadata keyMetadata, final T transaction
  ) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(transaction);
    return getSignatureServiceSafe(keyMetadata).multiSign(keyMetadata, transaction);
  }

  @Override
  public PublicKey getPublicKey(final KeyMetadata keyMetadata) {
    Objects.requireNonNull(keyMetadata);
    return getSignatureServiceSafe(keyMetadata).getPublicKey(keyMetadata);
  }

  @Override
  public <T extends Transaction> boolean verifySingleSigned(
    final SignatureWithKeyMetadata signatureWithKeyMetadata, final T unsignedTransaction
  ) {
    Objects.requireNonNull(signatureWithKeyMetadata);
    Objects.requireNonNull(unsignedTransaction);

    return this
      .getSignatureServiceSafe(signatureWithKeyMetadata.signingKeyMetadata())
      .verifySingleSigned(signatureWithKeyMetadata, unsignedTransaction);
  }

  @Override
  public <T extends Transaction> boolean verifyMultiSigned(
    final Set<SignatureWithKeyMetadata> signatureWithKeyMetadataSet, final T unsignedTransaction, final int minSigners
  ) {
    Objects.requireNonNull(signatureWithKeyMetadataSet);
    Objects.requireNonNull(unsignedTransaction);
    Preconditions.checkArgument(minSigners > 0, "Valid multisigned transactions must have at least 1 signer");

    final long numValidSignatures = signatureWithKeyMetadataSet.stream()
      // Check signature against all public keys, hoping for a valid verification against one.
      .map(signatureWithKeyMetadata -> {
        final boolean oneValidSignature = this
          .getSignatureServiceSafe(signatureWithKeyMetadata.signingKeyMetadata())
          .verifyMultiSigned(Sets.newHashSet(signatureWithKeyMetadata), unsignedTransaction, 1);
        return oneValidSignature;
      })
      .filter($ -> $) // Only count it if it's 'true'
      .count();

    return numValidSignatures >= minSigners;
  }

  //////////////////
  // Private Helpers
  //////////////////

  /**
   * Construct a new {@link BcSignatureService} using the provided {@code privateKeyMetadata}.
   *
   * @param privateKeyMetadata A {@link KeyMetadata} with information about a private key.
   *
   * @return A {@link BcSignatureService}.
   */
  @VisibleForTesting
  protected SingleKeyDelegatedSignatureService constructSignatureService(final KeyMetadata privateKeyMetadata) {
    Objects.requireNonNull(privateKeyMetadata);

    final KeyPair keyPair;
    if (VersionType.ED25519 == getVersionType()) {
      final Seed seed = this.generateEd25519XrplSeed(privateKeyMetadata.keyIdentifier());
      keyPair = ed25519KeyPairService.deriveKeyPair(seed);
    } else if (VersionType.SECP256K1 == getVersionType()) {
      final Seed seed = this.generateSecp256k1Seed(privateKeyMetadata.keyIdentifier());
      keyPair = secp256k1KeyPairService.deriveKeyPair(seed);
    } else {
      throw new IllegalArgumentException("Invalid VersionType: " + getVersionType());
    }

    final PrivateKey privateKey = keyPair.privateKey();
    return new SingleKeyDelegatedSignatureService(privateKey);
  }

  /**
   * Deterministically generate a {@link Seed} based upon the supplied XRPL account identifier and a server-secret that
   * is loaded into memory.
   *
   * @param accountIdentifier A {@link String} that is combined with an in-memory server secret to deterministically
   *                          generate a seed for entropy.
   *
   * @return A {@link Seed} that can be used to generate an XRPL public/private key pair.
   *
   * @see "https://xrpl.org/cryptographic-keys.html#key-derivation"
   */
  @VisibleForTesting
  final Seed generateEd25519XrplSeed(final String accountIdentifier) {
    Objects.requireNonNull(accountIdentifier);

    final ServerSecret serverSecretBytes = serverSecretSupplier.get();
    byte[] passphraseBytes = EMPTY_BYTE_ARRAY; // <-- to avoid an NPE in the "finally" block.
    try {
      passphraseBytes = Hashing
        .hmacSha512(serverSecretBytes.value()) // <-- This is equivalent to the `passphraseBytes` in the xrpl.org docs.
        .hashBytes(accountIdentifier.getBytes())
        .asBytes();
      return Seed.ed25519SeedFromPassphrase(Passphrase.of(passphraseBytes));
    } finally {
      // Zero-out all bytes in the both arrays so secret material exists in-memory for as little time as possible.
      serverSecretBytes.destroy();
      Arrays.fill(passphraseBytes, (byte) 0);
    }
  }

  /**
   * Deterministically generate a {@link Seed} based upon the supplied XRPL account identifier and a server-secret that
   * is loaded into memory.
   *
   * @param accountIdentifier A {@link String} that is combined with an in-memory server secret to deterministically
   *                          generate a seed for entropy.
   *
   * @return A {@link Seed} that can be used to generate an XRPL public/private key pair.
   *
   * @see "https://xrpl.org/cryptographic-keys.html#key-derivation"
   */
  @VisibleForTesting
  final Seed generateSecp256k1Seed(final String accountIdentifier) {
    Objects.requireNonNull(accountIdentifier);

    final ServerSecret serverSecretBytes = serverSecretSupplier.get();
    byte[] passphraseBytes = EMPTY_BYTE_ARRAY; // <-- to avoid an NPE in the "finally" block.
    try {
      passphraseBytes = Hashing
        .hmacSha512(serverSecretBytes.value()) // <-- This is equivalent to the `passphraseBytes` in the xrpl.org docs.
        .hashBytes(accountIdentifier.getBytes())
        .asBytes();
      return Seed.secp256k1SeedFromPassphrase(Passphrase.of(passphraseBytes));
    } finally {
      // Zero-out all bytes in the both arrays so secret material exists in-memory for as little time as possible.
      serverSecretBytes.destroy();
      Arrays.fill(passphraseBytes, (byte) 0);
    }
  }

  /**
   * Accessor for the type of this service.
   *
   * @return A {@link VersionType}.
   */
  public VersionType getVersionType() {
    return versionType;
  }

  /**
   * <p>A {@link DelegatedSignatureService} that holds a single private key, in-memory, using BouncyCastle as the
   * underlying crypto implementation.</p>
   *
   * <p>WARNING: This implementation _might_ be appropriate for Android use, but should likely not be used in a
   * server-side context. In general, prefer an implementation that offers a higher level of security.</p>
   */
  private static class SingleKeyDelegatedSignatureService
    extends AbstractDelegatedSignatureService implements DelegatedSignatureService {

    private SingleKeyDelegatedSignatureService(
      final PrivateKey privateKey,
      final SignatureUtils signatureUtils,
      final AddressUtils addressUtils
    ) {
      super(
        new SingleKeyDelegatedTransactionSigner(
          privateKey,
          signatureUtils,
          addressUtils
        ),
        new SingleKeyDelegatedTransactionVerifier(
          privateKey,
          signatureUtils,
          addressUtils
        )
      );
    }

    /**
     * Required-args Constructor for use in development mode.
     *
     * @param privateKey A {@link KeyStore} to load all private keys from.
     */
    private SingleKeyDelegatedSignatureService(final PrivateKey privateKey) {
      this(
        privateKey,
        new SignatureUtils(ObjectMapperFactory.create(), new XrplBinaryCodec()),
        BcAddressUtils.getInstance()
      );
    }

    /**
     * <p>A {@link org.xrpl.xrpl4j.crypto.core.signing.DelegatedTransactionSigner} that holds a single private key,
     * in-memory, using BouncyCastle as the underlying crypto implementation.</p>
     */
    private static class SingleKeyDelegatedTransactionSigner extends AbstractDelegatedTransactionSigner {

      private final Ed25519Signer ed25519Signer;
      private final ECDSASigner ecdsaSigner;
      private final PrivateKey privateKey;

      private SingleKeyDelegatedTransactionSigner(
        PrivateKey privateKey,
        SignatureUtils signatureUtils,
        AddressUtils addressUtils
      ) {
        super(signatureUtils, addressUtils);
        this.ed25519Signer = new Ed25519Signer();
        this.ecdsaSigner = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        this.privateKey = privateKey;
      }

      @Override
      protected synchronized Signature edDsaSign(
        final KeyMetadata privateKeyMetadata, final UnsignedByteArray signableTransactionBytes
      ) {
        Objects.requireNonNull(privateKeyMetadata);
        Objects.requireNonNull(signableTransactionBytes);

        Ed25519PrivateKeyParameters privateKeyParameters = BcKeyUtils.toEd25519PrivateKeyParams(privateKey);

        ed25519Signer.reset();
        ed25519Signer.init(true, privateKeyParameters);
        ed25519Signer.update(
          signableTransactionBytes.toByteArray(), 0, signableTransactionBytes.getUnsignedBytes().size()
        );

        final UnsignedByteArray sigBytes = UnsignedByteArray.of(ed25519Signer.generateSignature());
        return Signature.builder()
          .value(sigBytes)
          .build();
      }

      @SuppressWarnings("checkstyle:LocalVariableName")
      @Override
      protected synchronized Signature ecDsaSign(
        final KeyMetadata keyMetadata, final UnsignedByteArray signableTransactionBytes
      ) {
        Objects.requireNonNull(keyMetadata);
        Objects.requireNonNull(signableTransactionBytes);

        final UnsignedByteArray messageHash = HashingUtils.sha512Half(signableTransactionBytes);

        final ECPrivateKeyParameters parameters = BcKeyUtils.toEcPrivateKeyParams(privateKey);

        ecdsaSigner.init(true, parameters);
        final BigInteger[] signatures = ecdsaSigner.generateSignature(messageHash.toByteArray());
        final BigInteger r = signatures[0];
        BigInteger s = signatures[1];
        final BigInteger otherS = Secp256k1.EC_DOMAIN_PARAMETERS.getN().subtract(s);
        if (s.compareTo(otherS) > 0) {
          s = otherS;
        }

        final EcDsaSignature sig = EcDsaSignature.builder()
          .r(r)
          .s(s)
          .build();

        UnsignedByteArray sigBytes = sig.der();
        return Signature.builder()
          .value(sigBytes)
          .build();
      }

      @Override
      public PublicKey getPublicKey(KeyMetadata keyMetadata) {
        Objects.requireNonNull(keyMetadata);
        return BcKeyUtils.toPublicKey(this.privateKey);
      }
    }

    /**
     * <p>A {@link org.xrpl.xrpl4j.crypto.core.signing.TransactionVerifier} that holds a single private key, in-memory,
     * using BouncyCastle as the underlying crypto implementation.</p>
     */
    private static class SingleKeyDelegatedTransactionVerifier extends AbstractDelegatedTransactionVerifier {

      private final Ed25519Signer ed25519Signer;
      private final ECDSASigner ecdsaSigner;
      private final PrivateKey privateKey;

      public SingleKeyDelegatedTransactionVerifier(
        PrivateKey privateKey,
        SignatureUtils signatureUtils,
        AddressUtils addressUtils
      ) {
        super(signatureUtils, addressUtils);
        this.ed25519Signer = new Ed25519Signer();
        this.ecdsaSigner = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        this.privateKey = privateKey;
      }

      @Override
      protected synchronized boolean edDsaVerify(
        final KeyMetadata keyMetadata,
        final UnsignedByteArray signableTransactionBytes,
        final Signature transactionSignature
      ) {
        Objects.requireNonNull(keyMetadata);
        Objects.requireNonNull(signableTransactionBytes);
        Objects.requireNonNull(transactionSignature);

        final PublicKey publicKey = this.getPublicKey(keyMetadata);
        final Ed25519PublicKeyParameters bcPublicKey = BcKeyUtils.toEd25519PublicKeyParameters(
          publicKey);

        ed25519Signer.reset();
        ed25519Signer.init(false, bcPublicKey);
        ed25519Signer.update(signableTransactionBytes.toByteArray(), 0,
          signableTransactionBytes.getUnsignedBytes().size());

        return ed25519Signer.verifySignature(
          transactionSignature.value().toByteArray()
        );
      }

      @Override
      protected synchronized boolean ecDsaVerify(
        final KeyMetadata keyMetadata,
        final UnsignedByteArray signableTransactionBytes,
        final Signature transactionSignature
      ) {
        Objects.requireNonNull(keyMetadata);
        Objects.requireNonNull(signableTransactionBytes);
        Objects.requireNonNull(transactionSignature);

        final PublicKey publicKey = this.getPublicKey(keyMetadata);
        final ECPublicKeyParameters bcPublicKey = BcKeyUtils.toEcPublicKeyParameters(publicKey);

        UnsignedByteArray messageHash = HashingUtils.sha512Half(signableTransactionBytes);
        EcDsaSignature sig = EcDsaSignature.fromDer(transactionSignature.value().toByteArray());
        if (sig == null) {
          return false;
        }

        ecdsaSigner.init(false, bcPublicKey);
        return ecdsaSigner.verifySignature(messageHash.toByteArray(), sig.r(), sig.s());
      }

      @Override
      public PublicKey getPublicKey(KeyMetadata keyMetadata) {
        Objects.requireNonNull(keyMetadata);
        return BcKeyUtils.toPublicKey(this.privateKey);
      }
    }
  }

  /**
   * Helper method to return an instance of {@link KeyMetadata} or else throw an exception.
   *
   * @param keyMetadata The {@link KeyMetadata} of the key to return.
   *
   * @return A {@link KeyMetadata}.
   *
   * @throws RuntimeException if no key metadata was found.
   */
  private DelegatedSignatureService getSignatureServiceSafe(final KeyMetadata keyMetadata) {
    Objects.requireNonNull(keyMetadata);
    return Optional.ofNullable(this.keyMetadataLoadingCache.get(keyMetadata))
      .orElseThrow(() -> new RuntimeException("No KeyMetadata Found"));
  }
}
