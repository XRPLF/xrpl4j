package org.xrpl.xrpl4j.crypto.signing;

import com.google.common.io.BaseEncoding;
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
import org.xrpl.xrpl4j.crypto.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.KeyStoreType;
import org.xrpl.xrpl4j.crypto.PrivateKey;
import org.xrpl.xrpl4j.crypto.PublicKey;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.keypairs.EcDsaSignature;
import org.xrpl.xrpl4j.keypairs.HashUtils;
import org.xrpl.xrpl4j.keypairs.KeyPairService;
import org.xrpl.xrpl4j.keypairs.Secp256k1;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.math.BigInteger;
import java.security.KeyStore;
import java.util.Objects;

/**
 * <p>A {@link SignatureService} that holds a single private key, in-memory, using BouncyCastle as the underlying
 * crypto implementation.</p>
 *
 * <p>WARNING: This implementation _might_ be appropriate for Android use, but should likely not be used in a
 * server-side context. In general, prefer an implementation that offers a higher level of security.</p>
 */
public class SingleKeySignatureService extends AbstractSignatureService implements SignatureService {

  private static final KeyStoreType KEY_STORE_TYPE = KeyStoreType.fromKeystoreTypeId("in-memory-single-key");

  private final Ed25519Signer ed25519Signer;
  private final ECDSASigner ecdsaSigner;
  private final PrivateKey privateKey;

  /**
   * Required-args Constructor for use in development mode.
   *
   * @param privateKey A {@link KeyStore} to load all private keys from.
   */
  public SingleKeySignatureService(final PrivateKey privateKey) {
    this(
      new SignatureUtils(ObjectMapperFactory.create(), new XrplBinaryCodec()),
      new Ed25519Signer(),
      new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest())),
      DefaultKeyPairService.getInstance(),
      privateKey
    );
  }

  /**
   * Required-args Constructor.
   *
   * @param signatureUtils An {@link SignatureUtils}.
   * @param ed25519Signer  An {@link Ed25519Signer}.
   * @param ecdsaSigner    An {@link ECDSASigner}.
   * @param keyPairService A {@link KeyPairService}.
   * @param privateKey     A {@link PrivateKey} for obtain keys from.
   */
  public SingleKeySignatureService(
    final SignatureUtils signatureUtils,
    final Ed25519Signer ed25519Signer,
    final ECDSASigner ecdsaSigner,
    final KeyPairService keyPairService,
    final PrivateKey privateKey
    ) {
    super(KEY_STORE_TYPE, signatureUtils, keyPairService);
    this.ed25519Signer = Objects.requireNonNull(ed25519Signer);
    this.ecdsaSigner = Objects.requireNonNull(ecdsaSigner);
    this.privateKey = Objects.requireNonNull(privateKey);
  }

  @Override
  public PublicKey getPublicKey(final KeyMetadata privateKeyMetadata) {
    Objects.requireNonNull(privateKeyMetadata);
    return BcKeyUtils.toPublicKey(this.privateKey);
  }

  @Override
  protected synchronized Signature edDsaSign(
    final KeyMetadata privateKeyMetadata, final UnsignedByteArray signableTransactionBytes
  ) {
    Objects.requireNonNull(privateKeyMetadata);
    Objects.requireNonNull(signableTransactionBytes);

    Ed25519PrivateKeyParameters privateKeyParameters = new Ed25519PrivateKeyParameters(
      BaseEncoding.base16().decode(privateKey.base16Encoded().substring(2)), // Remove ED prefix byte
      0
    );

    ed25519Signer.reset();
    ed25519Signer.init(true, privateKeyParameters);
    ed25519Signer.update(signableTransactionBytes.toByteArray(), 0, signableTransactionBytes.getUnsignedBytes().size());

    final UnsignedByteArray sigBytes = UnsignedByteArray.of(ed25519Signer.generateSignature());
    return Signature.builder()
      .value(sigBytes)
      .build();
  }

  @Override
  protected synchronized boolean edDsaVerify(
    final KeyMetadata keyMetadata,
    final SignedTransaction transactionWithSignature,
    final UnsignedByteArray signableTransactionBytes
  ) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(transactionWithSignature);
    Objects.requireNonNull(signableTransactionBytes);

    final PublicKey publicKey = this.getPublicKey(keyMetadata);
    final Ed25519PublicKeyParameters bcPublicKey = BcKeyUtils.toEd25519PublicKeyParameters(publicKey);

    ed25519Signer.reset();
    ed25519Signer.init(false, bcPublicKey);
    ed25519Signer.update(signableTransactionBytes.toByteArray(), 0, signableTransactionBytes.getUnsignedBytes().size());

    return ed25519Signer.verifySignature(
      transactionWithSignature.signature().value().toByteArray()
    );
  }

  @SuppressWarnings("checkstyle:LocalVariableName")
  @Override
  protected synchronized Signature ecDsaSign(
    final KeyMetadata keyMetadata, final UnsignedByteArray signableTransactionBytes
  ) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(signableTransactionBytes);

    final UnsignedByteArray messageHash = HashUtils.sha512Half(signableTransactionBytes);

    final BigInteger privateKeyInt = new BigInteger(privateKey.base16Encoded(), 16);
    final ECPrivateKeyParameters parameters = new ECPrivateKeyParameters(privateKeyInt, Secp256k1.ecDomainParameters);

    ecdsaSigner.init(true, parameters);
    final BigInteger[] signatures = ecdsaSigner.generateSignature(messageHash.toByteArray());
    final BigInteger r = signatures[0];
    BigInteger s = signatures[1];
    final BigInteger otherS = Secp256k1.ecDomainParameters.getN().subtract(s);
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
  protected synchronized boolean ecDsaVerify(
    final KeyMetadata keyMetadata,
    final SignedTransaction transactionWithSignature,
    final UnsignedByteArray signableTransactionBytes
  ) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(transactionWithSignature);
    Objects.requireNonNull(signableTransactionBytes);

    final PublicKey publicKey = this.getPublicKey(keyMetadata);
    final ECPublicKeyParameters bcPublicKey = BcKeyUtils.toEcPublicKeyParameters(publicKey);

    UnsignedByteArray messageHash = HashUtils.sha512Half(signableTransactionBytes);
    EcDsaSignature sig = EcDsaSignature.fromDer(transactionWithSignature.signature().value().toByteArray());
    if (sig == null) {
      return false;
    }

    ecdsaSigner.init(false, bcPublicKey);
    return ecdsaSigner.verifySignature(messageHash.toByteArray(), sig.r(), sig.s());
  }
}
