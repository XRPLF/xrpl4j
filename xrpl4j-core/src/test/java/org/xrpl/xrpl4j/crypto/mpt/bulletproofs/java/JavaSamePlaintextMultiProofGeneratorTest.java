package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SamePlaintextMultiProof;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SamePlaintextMultiProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SamePlaintextParticipant;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTSendContext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPublicKey;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Unit tests for {@link JavaSamePlaintextMultiProofGenerator}.
 *
 * <p>This test uses deterministic inputs for comparison with C implementation.</p>
 */
class JavaSamePlaintextMultiProofGeneratorTest {

  private SamePlaintextMultiProofGenerator proofGenerator;

  @BeforeEach
  void setUp() {
    proofGenerator = new JavaSamePlaintextMultiProofGenerator();
  }

  /**
   * Deterministic test with N=3 participants (sender, destination, issuer) for comparison with C implementation.
   *
   * <p>This test uses deterministic values for reproducibility.</p>
   */
  @Test
  void testDeterministicProofGenerationForCComparison() {
    // 1. Amount
    UnsignedLong amount = UnsignedLong.valueOf(500);

    // 2. Generate deterministic public keys from passphrases (sender, destination, issuer)
    Seed senderSeed = Seed.secp256k1SeedFromPassphrase(Passphrase.of("test_sender"));
    KeyPair senderKeypair = senderSeed.deriveKeyPair();
    ECPoint senderPk = BcKeyUtils.toEcPublicKeyParameters(senderKeypair.publicKey()).getQ();
    ElGamalPublicKey senderPublicKey = ElGamalPublicKey.fromEcPoint(senderPk);

    Seed destSeed = Seed.secp256k1SeedFromPassphrase(Passphrase.of("test_dest"));
    KeyPair destKeypair = destSeed.deriveKeyPair();
    ECPoint destPk = BcKeyUtils.toEcPublicKeyParameters(destKeypair.publicKey()).getQ();
    ElGamalPublicKey destPublicKey = ElGamalPublicKey.fromEcPoint(destPk);

    Seed issuerSeed = Seed.secp256k1SeedFromPassphrase(Passphrase.of("test_issuer"));
    KeyPair issuerKeypair = issuerSeed.deriveKeyPair();
    ECPoint issuerPk = BcKeyUtils.toEcPublicKeyParameters(issuerKeypair.publicKey()).getQ();
    ElGamalPublicKey issuerPublicKey = ElGamalPublicKey.fromEcPoint(issuerPk);

    // 3. Deterministic blinding factors
    byte[] r1Bytes = new byte[32];
    byte[] r2Bytes = new byte[32];
    byte[] r3Bytes = new byte[32];
    for (int i = 0; i < 32; i++) {
      r1Bytes[i] = 0x01;
      r2Bytes[i] = 0x02;
      r3Bytes[i] = 0x03;
    }
    BlindingFactor r1 = BlindingFactor.fromBytes(r1Bytes);
    BlindingFactor r2 = BlindingFactor.fromBytes(r2Bytes);
    BlindingFactor r3 = BlindingFactor.fromBytes(r3Bytes);

    // 4. Create ciphertexts: R = r * G, S = m * G + r * Pk
    BigInteger mInt = BigInteger.valueOf(amount.longValue());
    ECPoint mG = Secp256k1Operations.multiplyG(mInt);

    BigInteger r1Int = new BigInteger(1, r1Bytes);
    ECPoint R1 = Secp256k1Operations.multiplyG(r1Int);
    ECPoint S1 = Secp256k1Operations.add(mG, Secp256k1Operations.multiply(senderPk, r1Int));
    ElGamalCiphertext senderCiphertext = new ElGamalCiphertext(R1, S1);

    BigInteger r2Int = new BigInteger(1, r2Bytes);
    ECPoint R2 = Secp256k1Operations.multiplyG(r2Int);
    ECPoint S2 = Secp256k1Operations.add(mG, Secp256k1Operations.multiply(destPk, r2Int));
    ElGamalCiphertext destCiphertext = new ElGamalCiphertext(R2, S2);

    BigInteger r3Int = new BigInteger(1, r3Bytes);
    ECPoint R3 = Secp256k1Operations.multiplyG(r3Int);
    ECPoint S3 = Secp256k1Operations.add(mG, Secp256k1Operations.multiply(issuerPk, r3Int));
    ElGamalCiphertext issuerCiphertext = new ElGamalCiphertext(R3, S3);

    // 5. Deterministic nonces
    byte[] nonceKmBytes = new byte[32];
    byte[] nonceKr1Bytes = new byte[32];
    byte[] nonceKr2Bytes = new byte[32];
    byte[] nonceKr3Bytes = new byte[32];
    for (int i = 0; i < 32; i++) {
      nonceKmBytes[i] = 0x10;
      nonceKr1Bytes[i] = 0x11;
      nonceKr2Bytes[i] = 0x12;
      nonceKr3Bytes[i] = 0x13;
    }
    BlindingFactor nonceKm = BlindingFactor.fromBytes(nonceKmBytes);
    BlindingFactor nonceKr1 = BlindingFactor.fromBytes(nonceKr1Bytes);
    BlindingFactor nonceKr2 = BlindingFactor.fromBytes(nonceKr2Bytes);
    BlindingFactor nonceKr3 = BlindingFactor.fromBytes(nonceKr3Bytes);

    // 6. Create participants
    SamePlaintextParticipant sender = SamePlaintextParticipant.forProofGeneration(
      senderCiphertext, senderPublicKey, r1, nonceKr1
    );
    SamePlaintextParticipant destination = SamePlaintextParticipant.forProofGeneration(
      destCiphertext, destPublicKey, r2, nonceKr2
    );
    SamePlaintextParticipant issuer = SamePlaintextParticipant.forProofGeneration(
      issuerCiphertext, issuerPublicKey, r3, nonceKr3
    );

    // 7. Create context
    ConfidentialMPTSendContext context = ConfidentialMPTSendContext.generate(
      Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
      UnsignedInteger.valueOf(1),
      MpTokenIssuanceId.of("000000000000000000000000000000000000000000000000"),
      Address.of("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"),
      UnsignedInteger.valueOf(1)
    );

    // 8. Generate proof
    SamePlaintextMultiProof proof = proofGenerator.generateProof(
      amount, sender, destination, issuer, Optional.empty(), context, nonceKm
    );

    // 9. Print proof info
    System.out.println("\n========== SAME PLAINTEXT MULTI PROOF TEST VALUES (N=3) ==========");
    System.out.println("Proof size: " + proof.toBytes().length + " bytes");
    System.out.println("Proof hex: " + proof.hexValue());
    System.out.println();

    // 10. Verify proof using verification participants
    SamePlaintextParticipant senderVerify = SamePlaintextParticipant.forVerification(
      senderCiphertext, senderPublicKey
    );
    SamePlaintextParticipant destVerify = SamePlaintextParticipant.forVerification(
      destCiphertext, destPublicKey
    );
    SamePlaintextParticipant issuerVerify = SamePlaintextParticipant.forVerification(
      issuerCiphertext, issuerPublicKey
    );

    boolean valid = proofGenerator.verify(proof, senderVerify, destVerify, issuerVerify, Optional.empty(), context);
    System.out.println("Proof valid: " + valid);
    System.out.println("================================================================\n");

    // 11. Assertions
    int expectedSize = SamePlaintextMultiProofGenerator.proofSize(3);
    assertThat(proof.toBytes()).hasSize(expectedSize);
    assertThat(proof.participantCount()).isEqualTo(3);
    assertThat(valid).isTrue();
  }
}

