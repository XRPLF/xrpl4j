package org.xrpl.xrpl4j.crypto.signing.bc;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.crypto.ServerSecret;
import org.xrpl.xrpl4j.crypto.ServerSecretSupplier;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PrivateKeyReference;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SignatureUtils;
import org.xrpl.xrpl4j.crypto.signing.SignatureWithPublicKey;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * An implementation of {@link SignatureService} that uses an in-memory secret value to deterministically create a seed
 * value that can then be used to generate XRPL private keys. This implementation keeps a cache of instances of
 * {@link SignatureService} that it delegates to based upon the {@link PrivateKeyReference} supplied on each call.
 *
 * <p>WARNING: This implementation stores private seed-generation material in-memory. Depending on your security
 * requirements, consider a different implementation of {@link SignatureService}.</p>
 */
public class BcDerivedKeySignatureService implements SignatureService<PrivateKeyReference> {

  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

  private final LoadingCache<PrivateKeyReference, BcSingleKeyTransactionSigner> transactionSignerCache;

  private final ServerSecretSupplier serverSecretSupplier;

  // Supplied to the loading cache on each create.
  private final BcSignatureService commonBcSignatureService;

  /**
   * Required-args Constructor.
   *
   * @param serverSecretSupplier A {@link ServerSecretSupplier} that can be used to generate seed values, which can
   */
  public BcDerivedKeySignatureService(final ServerSecretSupplier serverSecretSupplier) {
    this(
      serverSecretSupplier,
      CaffeineSpec.parse("maximumSize=10000,expireAfterWrite=30s")
    );
  }

  /**
   * Required-args Constructor.
   *
   * @param serverSecretSupplier A {@link ServerSecretSupplier} that can be used to generate seed values, which can
   * @param caffeineSpec         A {@link CaffeineSpec} that can be initialized externally to configure the Caffeine
   *                             cache constructed by this service.
   */
  public BcDerivedKeySignatureService(
    final ServerSecretSupplier serverSecretSupplier,
    final CaffeineSpec caffeineSpec
  ) {
    this.serverSecretSupplier = Objects.requireNonNull(serverSecretSupplier);
    this.transactionSignerCache = Caffeine.from(Objects.requireNonNull(caffeineSpec))
      .build(this::constructTransactionSigner);

    this.commonBcSignatureService = new BcSignatureService(
      SignatureUtils.getInstance(),
      new Ed25519Signer(),
      new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()))
    );
  }

  @Override
  public PublicKey derivePublicKey(final PrivateKeyReference privateKeyable) {
    Objects.requireNonNull(privateKeyable);
    return this.getTransactionSigner(privateKeyable).getPublicKey();
  }

  @Override
  public <T extends Transaction> SingleSignedTransaction<T> sign(
    final PrivateKeyReference privateKeyReference, final T transaction
  ) {
    Objects.requireNonNull(privateKeyReference);
    Objects.requireNonNull(transaction);

    return this.getTransactionSigner(privateKeyReference).sign(transaction);
  }

  @Override
  public Signature sign(final PrivateKeyReference privateKeyReference, final UnsignedClaim unsignedClaim) {
    return getTransactionSigner(privateKeyReference).sign(unsignedClaim);
  }

  @Override
  public <T extends Transaction> Signature multiSign(
    final PrivateKeyReference privateKeyReference, final T transaction
  ) {
    Objects.requireNonNull(privateKeyReference);
    Objects.requireNonNull(transaction);
    return getTransactionSigner(privateKeyReference).multiSign(transaction);
  }

  @Override
  public <T extends Transaction> boolean verify(
    final SignatureWithPublicKey signatureWithPublicKey, final T unsignedTransaction
  ) {
    Objects.requireNonNull(signatureWithPublicKey);
    Objects.requireNonNull(unsignedTransaction);

    return this.commonBcSignatureService.verify(signatureWithPublicKey, unsignedTransaction);
  }

  @Override
  public <T extends Transaction> boolean verifyMultiSigned(
    final Set<SignatureWithPublicKey> signatureWithPrivateKeyReferences,
    final T unsignedTransaction,
    final int minSigners
  ) {
    Objects.requireNonNull(signatureWithPrivateKeyReferences);
    Objects.requireNonNull(unsignedTransaction);
    Preconditions.checkArgument(minSigners > 0, "Valid multisigned transactions must have at least 1 signer");

    final long numValidSignatures = signatureWithPrivateKeyReferences.stream()
      // Check signature against all public keys, hoping for a valid verification against one.
      .map(signatureWithPublicKey -> this.commonBcSignatureService.verifyMultiSigned(
          Sets.newHashSet(signatureWithPublicKey), unsignedTransaction, 1
        )
      )
      .filter($ -> $) // Only count it if it's 'true'
      .count();

    return numValidSignatures >= minSigners;
  }

  //////////////////
  // Private Helpers
  //////////////////

  /**
   * Construct a new {@link BcSingleKeyTransactionSigner} using the provided {@code privateKeyReference}.
   *
   * @param privateKeyReference A {@link PrivateKeyReference} with information about a private key.
   *
   * @return A {@link BcSingleKeyTransactionSigner}.
   */
  @VisibleForTesting
  protected BcSingleKeyTransactionSigner constructTransactionSigner(final PrivateKeyReference privateKeyReference) {
    Objects.requireNonNull(privateKeyReference);

    final KeyPair keyPair;
    if (KeyType.ED25519 == privateKeyReference.keyType()) {
      final Seed seed = this.generateEd25519XrplSeed(privateKeyReference.keyIdentifier());
      keyPair = seed.deriveKeyPair();
    } else if (KeyType.SECP256K1 == privateKeyReference.keyType()) {
      final Seed seed = this.generateSecp256k1Seed(privateKeyReference.keyIdentifier());
      keyPair =  seed.deriveKeyPair();
    } else {
      throw new IllegalArgumentException("Invalid VersionType: " + privateKeyReference.keyType());
    }

    return new BcSingleKeyTransactionSigner(keyPair.privateKey(), commonBcSignatureService);
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
      passphraseBytes = Hashing.hmacSha512(
          serverSecretBytes.value()) // <-- This is equivalent to the `passphraseBytes` in the xrpl.org docs.
        .hashBytes(accountIdentifier.getBytes()).asBytes();
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
      passphraseBytes = Hashing.hmacSha512(
          serverSecretBytes.value()) // <-- This is equivalent to the `passphraseBytes` in the xrpl.org docs.
        .hashBytes(accountIdentifier.getBytes()).asBytes();
      return Seed.secp256k1SeedFromPassphrase(Passphrase.of(passphraseBytes));
    } finally {
      // Zero-out all bytes in the both arrays so secret material exists in-memory for as little time as possible.
      serverSecretBytes.destroy();
      Arrays.fill(passphraseBytes, (byte) 0);
    }
  }

  /**
   * Helper method to return an instance of {@link BcSingleKeyTransactionSigner} or else throw an exception.
   *
   * @param privateKeyReference The {@link PrivateKeyReference} of the key to return.
   *
   * @return A {@link BcSingleKeyTransactionSigner}.
   */
  private BcSingleKeyTransactionSigner getTransactionSigner(final PrivateKeyReference privateKeyReference) {
    Objects.requireNonNull(privateKeyReference);

    // Try to load from the loading cache...
    return this.transactionSignerCache.get(privateKeyReference);
  }

  /**
   * <p>A transaction signer that uses BouncyCastle internally with a single private key.</p>
   *
   * <p>WARNING: This implementation utilizes in-memory private-key material. Consider using an alternative
   * implementation that relies upon {@link PrivateKeyReference} instead for improved security.</p>
   */
  private static class BcSingleKeyTransactionSigner {

    private final PrivateKey privateKey;
    private final BcSignatureService bcSignatureService;

    public BcSingleKeyTransactionSigner(final PrivateKey privateKey, final BcSignatureService bcSignatureService) {
      this.privateKey = Objects.requireNonNull(privateKey);
      this.bcSignatureService = Objects.requireNonNull(bcSignatureService);
    }

    public final <T extends Transaction> SingleSignedTransaction<T> sign(final T transaction) {
      return bcSignatureService.sign(this.privateKey, transaction);
    }

    public Signature sign(final UnsignedClaim unsignedClaim) {
      Objects.requireNonNull(unsignedClaim);
      return bcSignatureService.sign(this.privateKey, unsignedClaim);
    }

    public <T extends Transaction> Signature multiSign(final T transaction) {
      return bcSignatureService.multiSign(this.privateKey, transaction);
    }

    public PublicKey getPublicKey() {
      return BcKeyUtils.toPublicKey(this.privateKey);
    }
  }

}
