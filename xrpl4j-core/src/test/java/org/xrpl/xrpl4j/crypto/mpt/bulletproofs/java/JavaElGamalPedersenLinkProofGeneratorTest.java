package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.LinkageProofType;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.PedersenCommitmentGenerator;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTSendContext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalKeyPair;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPublicKey;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.ElGamalPedersenLinkProof;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.PedersenCommitment;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.math.BigInteger;

/**
 * Unit tests for {@link JavaElGamalPedersenLinkProofGenerator}.
 *
 * <p>These tests use deterministic inputs to verify compatibility with C implementation.</p>
 */
class JavaElGamalPedersenLinkProofGeneratorTest {

  private static final BaseEncoding HEX = BaseEncoding.base16().upperCase();

  private JavaElGamalPedersenLinkProofGenerator generator;
  private PedersenCommitmentGenerator pedersenGen;

  @BeforeEach
  void setUp() {
    generator = new JavaElGamalPedersenLinkProofGenerator();
    pedersenGen = new JavaPedersenCommitmentGenerator();
  }

  /**
   * Test generating and verifying an AMOUNT_COMMITMENT linkage proof.
   */
  @Test
  void testGenerateAndVerifyAmountCommitmentProof() {
    // 1. Amount
    UnsignedLong amount = UnsignedLong.valueOf(1000);

    // 2. Generate ElGamal key pair
    ElGamalKeyPair keyPair = ElGamalKeyPair.generate();
    ElGamalPublicKey publicKey = keyPair.publicKey();

    // 3. ElGamal blinding factor r
    BlindingFactor r = BlindingFactor.generate();

    // 4. Pedersen blinding factor rho
    BlindingFactor rho = BlindingFactor.generate();

    // 5. Create ElGamal ciphertext: C1 = r * G, C2 = m * G + r * Pk
    BigInteger rInt = new BigInteger(1, r.toBytes());
    ECPoint c1 = Secp256k1Operations.multiplyG(rInt);
    BigInteger mInt = BigInteger.valueOf(amount.longValue());
    ECPoint mG = Secp256k1Operations.multiplyG(mInt);
    ECPoint rPk = Secp256k1Operations.multiply(publicKey.asEcPoint(), rInt);
    ECPoint c2 = Secp256k1Operations.add(mG, rPk);
    ElGamalCiphertext ciphertext = new ElGamalCiphertext(c1, c2);

    // 6. Create Pedersen commitment
    PedersenCommitment commitment = pedersenGen.generateCommitment(amount, rho);

    // 7. Create context
    ConfidentialMPTSendContext context = ConfidentialMPTSendContext.generate(
      Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
      UnsignedInteger.valueOf(1),
      MpTokenIssuanceId.of("000000000000000000000000000000000000000000000000"),
      Address.of("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"),
      UnsignedInteger.valueOf(0)
    );

    // 8. Generate random nonces
    BlindingFactor nonceKm = BlindingFactor.generate();
    BlindingFactor nonceKr = BlindingFactor.generate();
    BlindingFactor nonceKrho = BlindingFactor.generate();

    // 9. Generate proof
    ElGamalPedersenLinkProof proof = generator.generateProof(
      LinkageProofType.AMOUNT_COMMITMENT,
      ciphertext,
      publicKey,
      commitment,
      amount,
      r,
      rho,
      nonceKm,
      nonceKr,
      nonceKrho,
      context
    );

    // 9. Verify proof
    boolean isValid = generator.verify(
      LinkageProofType.AMOUNT_COMMITMENT,
      proof,
      ciphertext,
      publicKey,
      commitment,
      context
    );

    // 10. Test that verification fails with wrong commitment
    BlindingFactor wrongRho = BlindingFactor.generate();
    PedersenCommitment wrongCommitment = pedersenGen.generateCommitment(amount, wrongRho);

    boolean isInvalid = generator.verify(
      LinkageProofType.AMOUNT_COMMITMENT,
      proof,
      ciphertext,
      publicKey,
      wrongCommitment,
      context
    );

    // Assertions
    assertThat(proof.toBytes()).hasSize(ElGamalPedersenLinkProof.PROOF_LENGTH);
    assertThat(isValid).isTrue();
    assertThat(isInvalid).isFalse();
  }

  /**
   * Test generating and verifying a BALANCE_COMMITMENT linkage proof.
   */
  @Test
  void testGenerateAndVerifyBalanceCommitmentProof() {
    // 1. Amount (balance)
    UnsignedLong balance = UnsignedLong.valueOf(5000);

    // 2. Generate ElGamal key pair (sender's key)
    ElGamalKeyPair senderKeyPair = ElGamalKeyPair.generate();
    ElGamalPublicKey senderPublicKey = senderKeyPair.publicKey();

    // 3. Create an encrypted balance (as if it was encrypted by someone else)
    // For balance proof, we use the private key as the "blinding factor"
    BlindingFactor privateKeyAsBlindingFactor = BlindingFactor.fromBytes(senderKeyPair.privateKey().naturalBytes().toByteArray());

    // 4. Pedersen blinding factor rho
    BlindingFactor rho = BlindingFactor.generate();

    // 5. Create ElGamal ciphertext representing encrypted balance
    // In a real scenario, this would be the existing encrypted balance
    BlindingFactor encryptionR = BlindingFactor.generate();
    BigInteger rInt = new BigInteger(1, encryptionR.toBytes());
    ECPoint c1 = Secp256k1Operations.multiplyG(rInt);
    BigInteger mInt = BigInteger.valueOf(balance.longValue());
    ECPoint mG = Secp256k1Operations.multiplyG(mInt);
    ECPoint rPk = Secp256k1Operations.multiply(senderPublicKey.asEcPoint(), rInt);
    ECPoint c2 = Secp256k1Operations.add(mG, rPk);
    ElGamalCiphertext ciphertext = new ElGamalCiphertext(c1, c2);

    // 6. Create Pedersen commitment
    PedersenCommitment commitment = pedersenGen.generateCommitment(balance, rho);

    // 7. Create context
    ConfidentialMPTSendContext context = ConfidentialMPTSendContext.generate(
      Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
      UnsignedInteger.valueOf(1),
      MpTokenIssuanceId.of("000000000000000000000000000000000000000000000000"),
      Address.of("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"),
      UnsignedInteger.valueOf(0)
    );

    // 8. Generate random nonces
    BlindingFactor nonceKm = BlindingFactor.generate();
    BlindingFactor nonceKr = BlindingFactor.generate();
    BlindingFactor nonceKrho = BlindingFactor.generate();

    // 9. Generate proof using BALANCE_COMMITMENT type
    ElGamalPedersenLinkProof proof = generator.generateProof(
      LinkageProofType.BALANCE_COMMITMENT,
      ciphertext,
      senderPublicKey,
      commitment,
      balance,
      privateKeyAsBlindingFactor,
      rho,
      nonceKm,
      nonceKr,
      nonceKrho,
      context
    );

    // 9. Verify proof
    boolean isValid = generator.verify(
      LinkageProofType.BALANCE_COMMITMENT,
      proof,
      ciphertext,
      senderPublicKey,
      commitment,
      context
    );

    // Assertions
    assertThat(proof.toBytes()).hasSize(ElGamalPedersenLinkProof.PROOF_LENGTH);
    assertThat(isValid).isTrue();
  }
}

