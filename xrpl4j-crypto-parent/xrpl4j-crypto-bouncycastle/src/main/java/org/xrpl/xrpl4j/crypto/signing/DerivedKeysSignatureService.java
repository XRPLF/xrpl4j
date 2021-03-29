package org.xrpl.xrpl4j.crypto.signing;

import static org.xrpl.xrpl4j.crypto.KeyStoreType.DERIVED_SERVER_SECRET;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.hash.Hashing;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.KeyStoreType;
import org.xrpl.xrpl4j.crypto.PrivateKey;
import org.xrpl.xrpl4j.crypto.PublicKey;
import org.xrpl.xrpl4j.crypto.Seed;
import org.xrpl.xrpl4j.crypto.ServerSecretSupplier;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.keypairs.KeyPair;
import org.xrpl.xrpl4j.keypairs.KeyPairService;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Arrays;
import java.util.Objects;

/**
 * An implementation that uses an in-memory secret key in order to deterministically create a seed value that can then
 * be used to deterministically generate XRPL private keys. Any derived key can then be used for signing XRP
 * transactions. This implementation keeps a cache of instances of {@link SingleKeySignatureService} that it delegates
 * to based upon {@link KeyMetadata} supplied on each call.
 *
 * <p>WARNING: This implementation stores private seed-generation material in-memory, and is thus only meant for
 * lower-security environments. For higher security deployments, prefer an HSM-based implementation instead.</p>
 */
public class DerivedKeysSignatureService implements SignatureService {

  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

  private final VersionType versionType;
  private final KeyPairService keyPairService;

  private final LoadingCache<KeyMetadata, SingleKeySignatureService> keyMetadataLoadingCache;

  private final ServerSecretSupplier serverSecretSupplier;

  /**
   * Required-args Constructor for use in development mode.
   *
   * @param serverSecretSupplier A {@link ServerSecretSupplier} that can be used to generate seed values, which can then
   *                             be used to generate private keys.
   * @param versionType          A {@link VersionType} that defines which type of key this signature service uses.
   */
  public DerivedKeysSignatureService(
    final ServerSecretSupplier serverSecretSupplier,
    final VersionType versionType
  ) {
    this(serverSecretSupplier, versionType, new DefaultKeyPairService());
  }

  /**
   * Required-args Constructor.
   *
   * @param serverSecretSupplier A {@link ServerSecretSupplier} that can be used to generate seed values, which can
   * @param versionType          A {@link VersionType} that defines which type of key this signature service uses.
   * @param keyPairService       A {@link KeyPairService}.
   */
  public DerivedKeysSignatureService(
    final ServerSecretSupplier serverSecretSupplier,
    final VersionType versionType,
    final KeyPairService keyPairService
  ) {
    this(
      serverSecretSupplier,
      versionType,
      keyPairService,
      CaffeineSpec.parse("maximumSize=10000,expireAfterWrite=30s")
    );
  }

  /**
   * Required-args Constructor.
   *
   * @param serverSecretSupplier A {@link ServerSecretSupplier} that can be used to generate seed values, which can
   * @param versionType          A {@link VersionType} that defines which type of key this signature service uses.
   * @param keyPairService       A {@link KeyPairService}.
   * @param caffeineSpec         A {@link CaffeineSpec} that can be initialized externally to configure the Caffeine
   *                             cache constructed by this service.
   */
  public DerivedKeysSignatureService(
    final ServerSecretSupplier serverSecretSupplier,
    final VersionType versionType,
    final KeyPairService keyPairService,
    final CaffeineSpec caffeineSpec
  ) {
    this.serverSecretSupplier = Objects.requireNonNull(serverSecretSupplier);
    this.versionType = Objects.requireNonNull(versionType);
    this.keyPairService = Objects.requireNonNull(keyPairService);
    this.keyMetadataLoadingCache = Caffeine
      .from(Objects.requireNonNull(caffeineSpec))
      .build(this::constructSignatureService);
  }


  @Override
  public <T extends Transaction> SignedTransaction<T> sign(
    final KeyMetadata keyMetadata, final T transaction
  ) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(transaction);
    return this.keyMetadataLoadingCache.get(keyMetadata).sign(keyMetadata, transaction);
  }

  @Override
  public Signature signWithBehavior(KeyMetadata keyMetadata, Transaction transaction, SigningBehavior behavior) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(transaction);
    return this.keyMetadataLoadingCache.get(keyMetadata).signWithBehavior(keyMetadata, transaction, behavior);
  }

  @Override
  public KeyStoreType keyStoreType() {
    return DERIVED_SERVER_SECRET;
  }

  @Override
  public PublicKey getPublicKey(final KeyMetadata keyMetadata) {
    Objects.requireNonNull(keyMetadata);
    return this.keyMetadataLoadingCache.get(keyMetadata).getPublicKey(keyMetadata);
  }

  @Override
  public <T extends Transaction> boolean verify(
    final KeyMetadata keyMetadata,
    final SignedTransaction<T> transactionWithSignature
  ) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(transactionWithSignature);
    return this.keyMetadataLoadingCache.get(keyMetadata).verify(keyMetadata, transactionWithSignature);
  }

  //////////////////
  // Private Helpers
  //////////////////

  /**
   * Construct a new {@link SingleKeySignatureService} using the provided {@code privateKeyMetadata}.
   *
   * @param privateKeyMetadata A {@link KeyMetadata} with information about a private key.
   *
   * @return A {@link SingleKeySignatureService}.
   */
  @VisibleForTesting
  protected SingleKeySignatureService constructSignatureService(final KeyMetadata privateKeyMetadata) {
    Objects.requireNonNull(privateKeyMetadata);

    final Seed seed;
    if (VersionType.ED25519 == getVersionType()) {
      seed = this.generateEd25519XrplSeed(privateKeyMetadata.keyIdentifier());
    } else if (VersionType.SECP256K1 == getVersionType()) {
      seed = this.generateSecp256k1Seed(privateKeyMetadata.keyIdentifier());
    } else {
      throw new IllegalArgumentException("Invalid VersionType: " + getVersionType());
    }

    final KeyPair keyPair = keyPairService.deriveKeyPair(seed.value());
    final String privateKeyHex = keyPair.privateKey();
    final PrivateKey privateKey = PrivateKey.fromBase16EncodedPrivateKey(privateKeyHex);

    return new SingleKeySignatureService(privateKey);
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

    final byte[] serverSecretBytes = serverSecretSupplier.get();
    byte[] passphrase = EMPTY_BYTE_ARRAY; // <-- to avoid an NPE in the finally.
    try {
      passphrase = Hashing
        .hmacSha512(serverSecretBytes) // <-- This is equivalent to the `passphrase` in the xrpl.org docs.
        .hashBytes(accountIdentifier.getBytes())
        .asBytes();
      return Seed.ed25519SeedFromPassphrase(passphrase);
    } finally {
      // Zero-out all bytes in the both arrays so secret material exists in-memory for as little time as possible.
      Arrays.fill(serverSecretBytes, (byte) 0);
      Arrays.fill(passphrase, (byte) 0);
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

    final byte[] serverSecretBytes = serverSecretSupplier.get();
    byte[] passphrase = EMPTY_BYTE_ARRAY; // <-- to avoid an NPE in the finally.
    try {
      passphrase = Hashing
        .hmacSha512(serverSecretBytes) // <-- This is equivalent to the `passphrase` in the xrpl.org docs.
        .hashBytes(accountIdentifier.getBytes())
        .asBytes();
      return Seed.secp256k1SeedFromPassphrase(passphrase);
    } finally {
      // Zero-out all bytes in the both arrays so secret material exists in-memory for as little time as possible.
      Arrays.fill(serverSecretBytes, (byte) 0);
      Arrays.fill(passphrase, (byte) 0);
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
}
