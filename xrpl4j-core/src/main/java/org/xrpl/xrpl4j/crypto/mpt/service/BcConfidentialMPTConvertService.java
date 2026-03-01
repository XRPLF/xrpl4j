package org.xrpl.xrpl4j.crypto.mpt.service;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SecretKeyProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc.BcSecretKeyProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTConvertContext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalEncryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.bc.BcElGamalEncryptor;
import org.xrpl.xrpl4j.crypto.mpt.models.ConfidentialMPTConvertResult;
import org.xrpl.xrpl4j.crypto.mpt.models.SecretKeyProof;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.Objects;
import java.util.Optional;

/**
 * BouncyCastle implementation of {@link ConfidentialMPTConvertService}.
 *
 * <p>This implementation uses in-memory {@link PrivateKey} instances for proof generation.
 * It uses {@link BcElGamalEncryptor} for encryption and {@link BcSecretKeyProofGenerator}
 * for ZK proof generation.</p>
 */
public class BcConfidentialMPTConvertService implements ConfidentialMPTConvertService<PrivateKey> {

  private final ElGamalEncryptor encryptor;
  private final SecretKeyProofGenerator<PrivateKey> proofGenerator;

  /**
   * Creates a new instance with default BouncyCastle implementations.
   */
  public BcConfidentialMPTConvertService() {
    this.encryptor = new BcElGamalEncryptor();
    this.proofGenerator = new BcSecretKeyProofGenerator();
  }

  /**
   * Creates a new instance with custom implementations.
   *
   * @param encryptor      The ElGamal encryptor to use.
   * @param proofGenerator The secret key proof generator to use.
   */
  public BcConfidentialMPTConvertService(
    final ElGamalEncryptor encryptor,
    final SecretKeyProofGenerator<PrivateKey> proofGenerator
  ) {
    this.encryptor = Objects.requireNonNull(encryptor, "encryptor must not be null");
    this.proofGenerator = Objects.requireNonNull(proofGenerator, "proofGenerator must not be null");
  }

  @Override
  public ConfidentialMPTConvertResult generateConvertData(
    final PrivateKey holderElGamalPrivateKey,
    final PublicKey holderElGamalPublicKey,
    final PublicKey issuerElGamalPublicKey,
    final Optional<PublicKey> auditorElGamalPublicKey,
    final Address holderAddress,
    final UnsignedInteger accountSequence,
    final MpTokenIssuanceId mpTokenIssuanceId,
    final UnsignedLong amountToConvert
  ) {
    Objects.requireNonNull(holderElGamalPrivateKey, "holderElGamalPrivateKey must not be null");
    Objects.requireNonNull(holderElGamalPublicKey, "holderElGamalPublicKey must not be null");
    Objects.requireNonNull(issuerElGamalPublicKey, "issuerElGamalPublicKey must not be null");
    Objects.requireNonNull(auditorElGamalPublicKey, "auditorElGamalPublicKey must not be null");
    Objects.requireNonNull(holderAddress, "holderAddress must not be null");
    Objects.requireNonNull(accountSequence, "accountSequence must not be null");
    Objects.requireNonNull(mpTokenIssuanceId, "mpTokenIssuanceId must not be null");
    Objects.requireNonNull(amountToConvert, "amountToConvert must not be null");

    // 1. Generate random blinding factor (same for all encryptions in Convert)
    BlindingFactor blindingFactor = BlindingFactor.generate();

    // 2. Encrypt amount for holder
    ElGamalCiphertext holderEncryptedAmount = encryptor.encrypt(
      amountToConvert, holderElGamalPublicKey, blindingFactor
    );

    // 3. Encrypt amount for issuer (using same blinding factor)
    ElGamalCiphertext issuerEncryptedAmount = encryptor.encrypt(
      amountToConvert, issuerElGamalPublicKey, blindingFactor
    );

    // 4. Optionally encrypt amount for auditor (using same blinding factor)
    ElGamalCiphertext auditorEncryptedAmount = null;
    if (auditorElGamalPublicKey.isPresent()) {
      auditorEncryptedAmount = encryptor.encrypt(
        amountToConvert, auditorElGamalPublicKey.get(), blindingFactor
      );
    }

    // 5. Generate context for ZK proof
    ConfidentialMPTConvertContext context = ConfidentialMPTConvertContext.generate(
      holderAddress,
      accountSequence,
      mpTokenIssuanceId,
      amountToConvert
    );

    // 6. Generate Schnorr Proof of Knowledge
    SecretKeyProof zkProof = proofGenerator.generateProof(holderElGamalPrivateKey, context);

    // 7. Build and return result
    ConfidentialMPTConvertResult.Builder builder = ConfidentialMPTConvertResult.builder()
      .holderEncryptedAmount(holderEncryptedAmount)
      .issuerEncryptedAmount(issuerEncryptedAmount)
      .blindingFactor(blindingFactor)
      .zkProof(zkProof);

    if (auditorEncryptedAmount != null) {
      builder.auditorEncryptedAmount(auditorEncryptedAmount);
    }

    return builder.build();
  }

  @Override
  public PublicKey derivePublicKey(final PrivateKey privateKey) {
    Objects.requireNonNull(privateKey, "privateKey must not be null");

    // Derive public key: P = sk * G
    ECPoint publicKeyPoint = Secp256k1Operations.multiplyG(Secp256k1Operations.toScalar(privateKey));
    byte[] compressedBytes = Secp256k1Operations.serializeCompressed(publicKeyPoint);

    return PublicKey.fromBase16EncodedPublicKey(
      com.google.common.io.BaseEncoding.base16().encode(compressedBytes)
    );
  }
}

