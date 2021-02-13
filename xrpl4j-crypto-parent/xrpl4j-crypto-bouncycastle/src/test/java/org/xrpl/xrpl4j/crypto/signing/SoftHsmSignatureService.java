package org.xrpl.xrpl4j.crypto.signing;

import static org.xrpl.xrpl4j.crypto.SecureRandomUtils.secureRandom;
import static org.xrpl.xrpl4j.keypairs.Secp256k1.ecDomainParameters;

import com.google.common.annotations.VisibleForTesting;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.KeyStoreType;
import org.xrpl.xrpl4j.crypto.PublicKey;
import org.xrpl.xrpl4j.keypairs.EcDsaSignature;
import org.xrpl.xrpl4j.keypairs.HashUtils;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Objects;

/**
 * <p>A test implementation of {@link SignatureService} to demonstrate support for externally held private keys, such
 * as private keys stored in a Hardware Security Module (HSM) (if support for such a system is ever built, we might
 * consider something like PKCS#11 with JCA in order to support external signing operations). For now, in order to
 * simulate keys that live on an external device, this implementation uses BouncyCastle keys for testing purposes. The
 * main intent here is merely to validate the the signature interfaces work properly for both in-memory and externally
 * held private keys, and this implementation simulates a sealed enclave where the private-key material is not
 * accessible from the outside.</p>
 *
 * <p>This implementation has two keys, one generated using the secp256k1 curve (for ECDSA), and one generated using
 * the Ed25519 curve (for EdDSA).</p>
 */
public class SoftHsmSignatureService implements SignatureService {

  static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      final BouncyCastleProvider bcProvider = new BouncyCastleProvider();
      if (Security.addProvider(bcProvider) == -1) {
        throw new RuntimeException("Could not configure BouncyCastle provider");
      }
    }
  }

  private static final String SECP256K1 = "secp256k1";
  private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName(SECP256K1);
  private static final ECDomainParameters EC_CURVE =
    new ECDomainParameters(
      CURVE_PARAMS.getCurve(),
      CURVE_PARAMS.getG(),
      CURVE_PARAMS.getN(),
      CURVE_PARAMS.getH()
    );
  private static final String BC = "BC";
  private static final String ECDSA = "ECDSA";

  public static final String KEY_ID_ED25519 = "ED25519";
  public static final String KEY_ID_SECP256K1 = "SECP256K1";

  private final SignatureUtils signatureUtils;
  private final Ed25519PrivateKeyParameters edPrivateKey;
  private final Ed25519PublicKeyParameters edPublicKey;
  private final ECPrivateKeyParameters ecPrivateKey;
  private final ECPublicKeyParameters ecPublicKey;

  private final Ed25519Signer ed25519Signer;
  private final ECDSASigner ecDsaSigner;

  /**
   * No-args Constructor.
   *
   * @param signatureUtils A {@link SignatureUtils}.
   */
  public SoftHsmSignatureService(final SignatureUtils signatureUtils) {
    this(signatureUtils, generateEdPrivateKeyParameters(), generateEcPrivateKeyParameters());
  }

  /**
   * Required-args Constructor, for testing purposes (generally this constructor should not be used so that we can
   * simulate new key material existing only inside of this class, in order to simulate an HSM).
   *
   * @param signatureUtils              A {@link SignatureUtils}.
   * @param ed25519PrivateKeyParameters A {@link Ed25519PrivateKeyParameters} generated from an Ed25519 curve.
   * @param ecPrivateKeyParameters      A {@link ECPrivateKeyParameters} generated from an secp256k1 curve.
   */
  @VisibleForTesting
  SoftHsmSignatureService(
    final SignatureUtils signatureUtils,
    final Ed25519PrivateKeyParameters ed25519PrivateKeyParameters,
    final ECPrivateKeyParameters ecPrivateKeyParameters
  ) {
    this.signatureUtils = Objects.requireNonNull(signatureUtils);

    // Ed25519
    this.edPrivateKey = Objects.requireNonNull(ed25519PrivateKeyParameters);
    this.edPublicKey = edPrivateKey.generatePublicKey();

    // secp256k1
    this.ecPrivateKey = Objects.requireNonNull(ecPrivateKeyParameters);
    final ECPoint q = CURVE_PARAMS.getG().multiply(this.ecPrivateKey.getD());
    this.ecPublicKey = new ECPublicKeyParameters(q, EC_CURVE);

    // Signers
    this.ed25519Signer = new Ed25519Signer();
    this.ecDsaSigner = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
  }

  @Override
  public KeyStoreType keyStoreType() {
    return KeyStoreType.fromKeystoreTypeId("SimulatedSoftHSM");
  }

  @Override
  public PublicKey getPublicKey(final KeyMetadata keyMetadata) {
    Objects.requireNonNull(keyMetadata);
    try {
      if (keyMetadata.keyIdentifier().equalsIgnoreCase(KEY_ID_ED25519)) {
        return BcKeyUtils.toPublicKey(this.edPublicKey);
      } else if (keyMetadata.keyIdentifier().equalsIgnoreCase(KEY_ID_SECP256K1)) {
        return BcKeyUtils.toPublicKey(this.ecPublicKey);
      } else {
        throw new IllegalArgumentException("Unable to sign using KeyMetadata: %s" + keyMetadata);
      }
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public SignedTransaction sign(
    final KeyMetadata keyMetadata, final Transaction transaction
  ) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(transaction);

    try {
      // Convert the transaction into a canonical format for signing.
      final UnsignedByteArray signableTransactionBytes =
        UnsignedByteArray.of(signatureUtils.toSignableBytes(transaction).toByteArray());

      final org.xrpl.xrpl4j.crypto.signing.Signature signature; // <-- Constructed below.
      if (keyMetadata.keyIdentifier().equalsIgnoreCase(KEY_ID_ED25519)) {
        signature = edDsaSign(signableTransactionBytes);
      } else if (keyMetadata.keyIdentifier().equalsIgnoreCase(KEY_ID_SECP256K1)) {
        signature = ecDsaSign(signableTransactionBytes);
      } else {
        throw new IllegalArgumentException("Unable to sign using KeyMetadata: %s" + keyMetadata);
      }

      // Add sig to original transaction and return
      return this.signatureUtils.addSignatureToTransaction(transaction, signature);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public boolean verify(final KeyMetadata keyMetadata, final SignedTransaction transactionWithSignature) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(transactionWithSignature);

    final byte[] signableTransactionBytes = signatureUtils
      .toSignableBytes(transactionWithSignature.unsignedTransaction()).toByteArray();
    final UnsignedByteArray signableTransactionUba = UnsignedByteArray.of(signableTransactionBytes);
    if (keyMetadata.keyIdentifier().equalsIgnoreCase(KEY_ID_ED25519)) {
      return edDsaVerify(transactionWithSignature, signableTransactionUba);
    } else if (keyMetadata.keyIdentifier().equalsIgnoreCase(KEY_ID_SECP256K1)) {
      return ecDsaVerify(transactionWithSignature, signableTransactionBytes);
    } else {
      throw new IllegalArgumentException("Unable to verify using KeyMetadata: %s" + keyMetadata);
    }
  }

  //////////////////
  // Private Helpers
  //////////////////

  /**
   * Generate a new {@link ECPrivateKeyParameters} using the secp256k1 curve.
   *
   * @return A {@link ECPrivateKeyParameters}.
   */
  @SuppressWarnings("checkstyle:LocalVariableName")
  @VisibleForTesting
  protected static KeyPair generateEcKeyPair() {
    try {
      ECGenParameterSpec ecSpec = new ECGenParameterSpec(SECP256K1);
      KeyPairGenerator g = KeyPairGenerator.getInstance(ECDSA, BC);
      g.initialize(ecSpec, new SecureRandom());
      return g.generateKeyPair();
    } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @VisibleForTesting
  protected static ECPrivateKeyParameters generateEcPrivateKeyParameters() {
    final PrivateKey privateKey = generateEcKeyPair().getPrivate();
    return new ECPrivateKeyParameters(((ECPrivateKey) privateKey).getS(), EC_CURVE);
  }

  @VisibleForTesting
  protected static Ed25519PrivateKeyParameters generateEdPrivateKeyParameters() {
    return new Ed25519PrivateKeyParameters(secureRandom());
  }

  /**
   * Helper method to sign using EdDSA.
   *
   * @param signableTransactionBytes An {@link UnsignedByteArray} with bytes to sign.
   *
   * @return A {@link Signature}.
   */
  private synchronized Signature edDsaSign(final UnsignedByteArray signableTransactionBytes) {
    Objects.requireNonNull(signableTransactionBytes);

    ed25519Signer.reset();
    ed25519Signer.init(true, this.edPrivateKey);
    ed25519Signer.update(signableTransactionBytes.toByteArray(), 0, signableTransactionBytes.toByteArray().length);
    final byte[] signatureBytes = ed25519Signer.generateSignature();
    return Signature.builder()
      .value(UnsignedByteArray.of(signatureBytes))
      .build();
  }

  /**
   * Verify a signature.
   *
   * @param transactionWithSignature A {@link SignedTransaction}.
   * @param signableTransactionUba   An {@link UnsignedByteArray}.
   *
   * @return {@code true} if the signature is valid; {@code false} otherwise.
   */
  private synchronized boolean edDsaVerify(
    final SignedTransaction transactionWithSignature,
    final UnsignedByteArray signableTransactionUba
  ) {
    ed25519Signer.reset();
    ed25519Signer.init(false, edPublicKey);
    ed25519Signer.update(signableTransactionUba.toByteArray(), 0, signableTransactionUba.getUnsignedBytes().size());
    return ed25519Signer.verifySignature(
      transactionWithSignature.signature().value().toByteArray()
    );
  }

  /**
   * Helper method to sign using EcDSA.
   *
   * @param signableTransactionBytes An {@link UnsignedByteArray} with bytes to sign.
   *
   * @return A {@link Signature}.
   */
  @SuppressWarnings("checkstyle:LocalVariableName")
  private synchronized Signature ecDsaSign(final UnsignedByteArray signableTransactionBytes) {
    Objects.requireNonNull(signableTransactionBytes);

    UnsignedByteArray messageHash = HashUtils.sha512Half(signableTransactionBytes);
    this.ecDsaSigner.init(true, this.ecPrivateKey);
    BigInteger[] signatures = this.ecDsaSigner.generateSignature(messageHash.toByteArray());
    BigInteger r = signatures[0];
    BigInteger s = signatures[1];
    BigInteger otherS = ecDomainParameters.getN().subtract(s);
    if (s.compareTo(otherS) > 0) {
      s = otherS;
    }

    EcDsaSignature ecDsaSignature = EcDsaSignature.builder()
      .r(r)
      .s(s)
      .build();

    return Signature.builder()
      .value(UnsignedByteArray.of(ecDsaSignature.der().toByteArray()))
      .build();
  }

  /**
   * Verify a signature.
   *
   * @param transactionWithSignature A {@link SignedTransaction}.
   * @param signableTransactionBytes An byte array.
   *
   * @return {@code true} if the signature is valid; {@code false} otherwise.
   */
  private synchronized boolean ecDsaVerify(
    final SignedTransaction transactionWithSignature,
    final byte[] signableTransactionBytes
  ) {
    Objects.requireNonNull(transactionWithSignature);
    Objects.requireNonNull(signableTransactionBytes);

    UnsignedByteArray messageHash = HashUtils.sha512Half(signableTransactionBytes);
    EcDsaSignature sig = EcDsaSignature.fromDer(transactionWithSignature.signature().value().toByteArray());
    if (sig == null) {
      return false;
    }

    ECDSASigner signer = new ECDSASigner();
    ECPoint publicKeyPoint = ecDomainParameters.getCurve().decodePoint(ecPublicKey.getQ().getEncoded(false));
    ECPublicKeyParameters params = new ECPublicKeyParameters(publicKeyPoint, ecDomainParameters);
    signer.init(false, params);
    return signer.verifySignature(messageHash.toByteArray(), sig.r(), sig.s());
  }
}
