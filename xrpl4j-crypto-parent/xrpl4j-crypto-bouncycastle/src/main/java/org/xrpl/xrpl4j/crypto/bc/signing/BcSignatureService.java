package org.xrpl.xrpl4j.crypto.bc.signing;

import com.google.common.base.Preconditions;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.bc.BcAddressUtils;
import org.xrpl.xrpl4j.crypto.bc.keys.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.core.AddressUtils;
import org.xrpl.xrpl4j.crypto.core.HashingUtils;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.signing.AbstractSignatureService;
import org.xrpl.xrpl4j.crypto.core.signing.DelegatedSignatureService;
import org.xrpl.xrpl4j.crypto.core.signing.Signature;
import org.xrpl.xrpl4j.crypto.core.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.core.signing.SignatureUtils;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;

/**
 * <p>A {@link SignatureService} that uses BouncyCastle internally.</p>
 *
 * <p>WARNING: This implementation requires in-memory private-key material. Consider using an implementation of {@link
 * DelegatedSignatureService} for improved security.</p>
 *
 * @see "https://www.bouncycastle.org/java.html"
 * @see "https://www.bouncycastle.org/fips-java/BCFipsIn100.pdf"
 */
public class BcSignatureService extends AbstractSignatureService implements SignatureService {

  private final Ed25519Signer ed25519Signer;
  private final ECDSASigner ecdsaSigner;

  /**
   * Required-args Constructor for use in development mode.
   */
  public BcSignatureService() {
    this(
      new SignatureUtils(ObjectMapperFactory.create(), new XrplBinaryCodec()),
      BcAddressUtils.getInstance(),
      new Ed25519Signer(),
      new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()))
    );
  }

  /**
   * Required-args Constructor.
   *
   * @param signatureUtils An {@link SignatureUtils}.
   * @param addressService An {@link AddressUtils}.
   * @param ed25519Signer  An {@link Ed25519Signer}.
   * @param ecdsaSigner    An {@link ECDSASigner}.
   */
  public BcSignatureService(
    final SignatureUtils signatureUtils,
    final AddressUtils addressService,
    final Ed25519Signer ed25519Signer,
    final ECDSASigner ecdsaSigner
  ) {
    super(signatureUtils, addressService);
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
      ed25519Signer.update(signableTransactionBytes.toByteArray(), 0,
        signableTransactionBytes.getUnsignedBytes().size());

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

  @SuppressWarnings("checkstyle:LocalVariableName")
  @Override
  public synchronized Signature ecDsaSign(final PrivateKey privateKey, final UnsignedByteArray transactionBytes) {
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
  protected PublicKey derivePublicKey(final PrivateKey privateKey) {
    Objects.requireNonNull(privateKey);
    return BcKeyUtils.toPublicKey(privateKey);
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

  /**
   * A container for EcDSA signature content.
   */
  @Value.Immutable
  @SuppressWarnings( {"LocalVariableName", "MethodName"})
  public interface EcDsaSignature {

    static ImmutableEcDsaSignature.Builder builder() {
      return ImmutableEcDsaSignature.builder();
    }

    /**
     * Create an {@link org.xrpl.xrpl4j.crypto.bc.signing.BcSignatureService.EcDsaSignature} from a DER
     * encoded byte array signature.
     *
     * @param bytes A DER encoded byte array containing a signature.
     * @return An {@link org.xrpl.xrpl4j.crypto.bc.signing.BcSignatureService.EcDsaSignature}.
     */
    static EcDsaSignature fromDer(byte[] bytes) {
      try {
        ASN1InputStream decoder = new ASN1InputStream(bytes);
        DLSequence seq = (DLSequence) decoder.readObject();
        ASN1Integer r;
        ASN1Integer s;
        try {
          r = (ASN1Integer) seq.getObjectAt(0);
          s = (ASN1Integer) seq.getObjectAt(1);
        } catch (ClassCastException e) {
          return null;
        } finally {
          decoder.close();
        }
        // OpenSSL deviates from the DER spec by interpreting these values as unsigned, though they should not be
        // Thus, we always use the positive versions. See: http://r6.ca/blog/20111119T211504Z.html
        return EcDsaSignature.builder()
          .r(r.getPositiveValue())
          .s(s.getPositiveValue())
          .build();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * The r component of this {@link org.xrpl.xrpl4j.crypto.bc.signing.BcSignatureService.EcDsaSignature}.
     *
     * @return A {@link BigInteger} denoting the r component of this signature.
     */
    BigInteger r();

    /**
     * The s component of this {@link org.xrpl.xrpl4j.crypto.bc.signing.BcSignatureService.EcDsaSignature}.
     *
     * @return A {@link BigInteger} denoting the r component of this signature.
     */
    BigInteger s();

    /**
     * Encode this {@link org.xrpl.xrpl4j.crypto.bc.signing.BcSignatureService.EcDsaSignature} to the ASN.1 DER format.
     *
     * @return An {@link UnsignedByteArray} containing the bytes of the encoded signature.
     */
    @Value.Derived
    default UnsignedByteArray der() {
      // Usually 70-72 bytes.
      ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
      try {
        DERSequenceGenerator seq = new DERSequenceGenerator(bos);
        seq.addObject(new ASN1Integer(r()));
        seq.addObject(new ASN1Integer(s()));
        seq.close();
        return UnsignedByteArray.of(bos.toByteArray());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Make sure signature is canonical to protect against signature morphing attacks.
     *
     * <p>Signature should be:
     * {@code <30> <len> [ <02> <lenR> <R> ] [ <02> <lenS> <S> ]} where {@code 6 <= len <= 70} {@code  1 <= lenR <= 33}
     * {@code 1 <= lenS <= 33}
     * </p>
     */
    @Value.Check
    default void isStrictlyCanonical() {
      int sigLen = der().length();

      Preconditions.checkArgument(sigLen >= 8 && sigLen <= 72);
      Preconditions.checkArgument((der().get(0).asInt() == 0x30) && (der().get(1).asInt() == (sigLen - 2)));

      // Find R and check its length
      int rPos = 4;
      int rLen = der().get(rPos - 1).asInt();

      Preconditions.checkArgument(rLen >= 1 && rLen <= 33 && (rLen + 7) <= sigLen, "r is the wrong length.");

      // Find S and check its length
      int sPos = rLen + 6;
      int sLen = der().get(sPos - 1).asInt();
      Preconditions.checkArgument(sLen >= 1 && sLen <= 33 && (rLen + sLen + 6) == sigLen, "s is the wrong length.");

      Preconditions.checkArgument(
        der().get(rPos - 2).asInt() == 0x02 && der().get(sPos - 2).asInt() == 0x02,
        "r or s have the wrong type."
      );

      Preconditions.checkArgument((der().get(rPos).asInt() & 0x80) == 0, "r cannot be negative.");

      Preconditions.checkArgument(der().get(rPos).asInt() != 0 || rLen != 1, "r cannot be 0.");

      Preconditions.checkArgument(der().get(rPos).asInt() != 0 || (der().get(rPos + 1).asInt() & 0x80) != 0,
        "r cannot be padded.");

      Preconditions.checkArgument((der().get(sPos).asInt() & 0x80) == 0, "s cannot be negative.");

      Preconditions.checkArgument(der().get(sPos).asInt() != 0 || sLen != 1, "s cannot be 0.");

      Preconditions.checkArgument(
        der().get(sPos).asInt() != 0 || (der().get(sPos + 1).asInt() & 0x80) != 0,
        "s cannot be padded");

      byte[] rBytes = new byte[rLen];
      byte[] sBytes = new byte[sLen];

      System.arraycopy(der().toByteArray(), rPos, rBytes, 0, rLen);
      System.arraycopy(der().toByteArray(), sPos, sBytes, 0, sLen);

      BigInteger r = new BigInteger(1, rBytes);
      BigInteger s = new BigInteger(1, sBytes);

      BigInteger order = Secp256k1.EC_DOMAIN_PARAMETERS.getN();

      Preconditions.checkArgument(r.compareTo(order) <= -1 && s.compareTo(order) <= -1, "r or s greater than modulus");
      Preconditions.checkArgument(order.subtract(s).compareTo(s) > -1);

    }
  }

}
