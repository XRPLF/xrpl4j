package org.xrpl.xrpl4j.crypto.signing.bc;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PrivateKeyReference;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.signing.AbstractSignatureService;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SignatureUtils;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.math.BigInteger;
import java.util.Objects;

/**
 * <p>A {@link SignatureService} that uses BouncyCastle internally to sign with an in-memory instance of
 * {@link PrivateKey}.</p>
 *
 * <p>WARNING: This implementation utilizes in-memory private-key material. Consider instead using a
 * {@link SignatureService} that uses instance of {@link PrivateKeyReference} for improved security.</p>
 *
 * @see "https://www.bouncycastle.org/java.html"
 * @see "https://www.bouncycastle.org/fips-java/BCFipsIn100.pdf"
 */
public class BcSignatureService extends AbstractSignatureService<PrivateKey> implements SignatureService<PrivateKey> {

  private final Ed25519Signer ed25519Signer;
  private final ECDSASigner ecdsaSigner;

  /**
   * Required-args Constructor for use in development mode.
   */
  public BcSignatureService() {
    this(
      new SignatureUtils(ObjectMapperFactory.create(), XrplBinaryCodec.getInstance()),
      new Ed25519Signer(),
      new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()))
    );
  }

  /**
   * Required-args Constructor.
   *
   * @param signatureUtils An {@link SignatureUtils}.
   * @param ed25519Signer  An {@link Ed25519Signer}.
   * @param ecdsaSigner    An {@link ECDSASigner}.
   */
  public BcSignatureService(
    final SignatureUtils signatureUtils,
    final Ed25519Signer ed25519Signer,
    final ECDSASigner ecdsaSigner
  ) {
    super(signatureUtils);
    this.ed25519Signer = Objects.requireNonNull(ed25519Signer);
    this.ecdsaSigner = Objects.requireNonNull(ecdsaSigner);
  }

  @Override
  protected synchronized Signature edDsaSign(
    final PrivateKey privateKey, final UnsignedByteArray signableTransactionBytes
  ) {
    Objects.requireNonNull(privateKey);
    Objects.requireNonNull(signableTransactionBytes);

    final byte[] privateKeyBytes = new byte[32];
    try {
      // Remove ED prefix byte (if it's there)
      System.arraycopy(privateKey.value().toByteArray(), 1, privateKeyBytes, 0, 32);
      Ed25519PrivateKeyParameters privateKeyParameters = new Ed25519PrivateKeyParameters(
        privateKeyBytes, 0
      );

      ed25519Signer.reset();
      ed25519Signer.init(true, privateKeyParameters);
      ed25519Signer.update(
        signableTransactionBytes.toByteArray(), 0, signableTransactionBytes.getUnsignedBytes().size()
      );

      final UnsignedByteArray sigBytes = UnsignedByteArray.of(ed25519Signer.generateSignature());
      return Signature.builder()
        .value(sigBytes)
        .build();
    } finally {
      // Clear out the copied array, which was only used for signing.
      for (int i = 0; i < 32; i++) {
        privateKeyBytes[i] = (byte) 0;
      }
    }
  }

  @SuppressWarnings("checkstyle:LocalVariableName")
  @Override
  protected synchronized Signature ecDsaSign(final PrivateKey privateKey, final UnsignedByteArray transactionBytes) {
    Objects.requireNonNull(privateKey);
    Objects.requireNonNull(transactionBytes);

    final UnsignedByteArray messageHash = HashingUtils.sha512Half(transactionBytes);

    final BigInteger privateKeyInt = new BigInteger(privateKey.value().toByteArray());
    final ECPrivateKeyParameters parameters = new ECPrivateKeyParameters(privateKeyInt, Secp256k1.EC_DOMAIN_PARAMETERS);

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
  protected synchronized boolean edDsaVerify(
    final PublicKey publicKey, final UnsignedByteArray transactionBytes, final Signature signature
  ) {
    Objects.requireNonNull(publicKey);
    Objects.requireNonNull(transactionBytes);
    Objects.requireNonNull(signature);

    final Ed25519PublicKeyParameters bcPublicKey = BcKeyUtils.toEd25519PublicKeyParameters(publicKey);

    ed25519Signer.reset();
    ed25519Signer.init(false, bcPublicKey);
    ed25519Signer.update(transactionBytes.toByteArray(), 0, transactionBytes.length());

    return ed25519Signer.verifySignature(signature.value().toByteArray());
  }


  @Override
  protected synchronized boolean ecDsaVerify(
    final PublicKey publicKey, final UnsignedByteArray transactionBytes, final Signature signature
  ) {
    Objects.requireNonNull(publicKey);
    Objects.requireNonNull(transactionBytes);
    Objects.requireNonNull(signature);

    final ECPublicKeyParameters bcPublicKey = BcKeyUtils.toEcPublicKeyParameters(publicKey);
    UnsignedByteArray messageHash = HashingUtils.sha512Half(transactionBytes);
    EcDsaSignature sig = EcDsaSignature.fromDer(signature.value().toByteArray());
    if (sig == null) {
      return false;
    }

    ecdsaSigner.init(false, bcPublicKey);
    return ecdsaSigner.verifySignature(messageHash.toByteArray(), sig.r(), sig.s());
  }

  @Override
  public PublicKey derivePublicKey(final PrivateKey privateKey) {
    Objects.requireNonNull(privateKey);
    return BcKeyUtils.toPublicKey(privateKey);
  }

}
