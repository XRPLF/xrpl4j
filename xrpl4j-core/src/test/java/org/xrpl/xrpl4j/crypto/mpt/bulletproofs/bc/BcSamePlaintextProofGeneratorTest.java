package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SamePlaintextProofVerifier;
import org.xrpl.xrpl4j.crypto.mpt.models.SamePlaintextMultiProof;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SamePlaintextProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.models.SamePlaintextParticipant;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTContextUtil;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTSendContext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link BcSamePlaintextProofGenerator}.
 *
 * <p>This test uses deterministic inputs for comparison with C implementation.</p>
 */
class BcSamePlaintextProofGeneratorTest {

  private SamePlaintextProofGenerator proofGenerator;
  private SamePlaintextProofVerifier proofVerifier;

  @BeforeEach
  void setUp() {
    proofGenerator = new BcSamePlaintextProofGenerator();
    proofVerifier = new BcSamePlaintextProofVerifier();
  }

  /**
   * Test proof generation and verification with N=3 participants (sender, destination, issuer).
   *
   * <p>Nonces are generated internally by the proof generator, matching the C implementation.</p>
   */
  @Test
  void testProofGenerationAndVerification() {
    // 1. Amount
    UnsignedLong amount = UnsignedLong.valueOf(500);

    // 2. Generate public keys from passphrases (sender, destination, issuer)
    Seed senderSeed = Seed.elGamalSecp256k1SeedFromPassphrase(Passphrase.of("test_sender"));
    KeyPair senderKeypair = senderSeed.deriveKeyPair();
    PublicKey senderPublicKey = senderKeypair.publicKey();
    ECPoint senderPk = Secp256k1Operations.toEcPoint(senderPublicKey);

    Seed destSeed = Seed.elGamalSecp256k1SeedFromPassphrase(Passphrase.of("test_dest"));
    KeyPair destKeypair = destSeed.deriveKeyPair();
    PublicKey destPublicKey = destKeypair.publicKey();
    ECPoint destPk = Secp256k1Operations.toEcPoint(destPublicKey);

    Seed issuerSeed = Seed.elGamalSecp256k1SeedFromPassphrase(Passphrase.of("test_issuer"));
    KeyPair issuerKeypair = issuerSeed.deriveKeyPair();
    PublicKey issuerPublicKey = issuerKeypair.publicKey();
    ECPoint issuerPk = Secp256k1Operations.toEcPoint(issuerPublicKey);

    // 3. Generate blinding factors
    BlindingFactor r1 = BlindingFactor.generate();
    BlindingFactor r2 = BlindingFactor.generate();
    BlindingFactor r3 = BlindingFactor.generate();

    // 4. Create ciphertexts: R = r * G, S = m * G + r * Pk
    BigInteger mInt = BigInteger.valueOf(amount.longValue());
    ECPoint mG = Secp256k1Operations.multiplyG(mInt);

    BigInteger r1Int = new BigInteger(1, r1.toBytes());
    ECPoint R1 = Secp256k1Operations.multiplyG(r1Int);
    ECPoint S1 = Secp256k1Operations.add(mG, Secp256k1Operations.multiply(senderPk, r1Int));
    ElGamalCiphertext senderCiphertext = new ElGamalCiphertext(R1, S1);

    BigInteger r2Int = new BigInteger(1, r2.toBytes());
    ECPoint R2 = Secp256k1Operations.multiplyG(r2Int);
    ECPoint S2 = Secp256k1Operations.add(mG, Secp256k1Operations.multiply(destPk, r2Int));
    ElGamalCiphertext destCiphertext = new ElGamalCiphertext(R2, S2);

    BigInteger r3Int = new BigInteger(1, r3.toBytes());
    ECPoint R3 = Secp256k1Operations.multiplyG(r3Int);
    ECPoint S3 = Secp256k1Operations.add(mG, Secp256k1Operations.multiply(issuerPk, r3Int));
    ElGamalCiphertext issuerCiphertext = new ElGamalCiphertext(R3, S3);

    // 5. Create participants for proof generation (with blinding factors)
    SamePlaintextParticipant sender = SamePlaintextParticipant.forProofGeneration(
      senderCiphertext, senderPublicKey, r1
    );
    SamePlaintextParticipant destination = SamePlaintextParticipant.forProofGeneration(
      destCiphertext, destPublicKey, r2
    );
    SamePlaintextParticipant issuer = SamePlaintextParticipant.forProofGeneration(
      issuerCiphertext, issuerPublicKey, r3
    );

    // 6. Create context
    ConfidentialMPTSendContext context = ConfidentialMPTContextUtil.generateSendContext(
      Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
      UnsignedInteger.valueOf(1),
      MpTokenIssuanceId.of("000000000000000000000000000000000000000000000000"),
      Address.of("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"),
      UnsignedInteger.valueOf(1)
    );

    // 7. Generate proof (nonces are generated internally) using list of participants
    List<SamePlaintextParticipant> participantsForProof = Arrays.asList(sender, destination, issuer);
    SamePlaintextMultiProof proof = proofGenerator.generateProof(amount, participantsForProof, context);

    // 8. Print proof info
    System.out.println("\n========== SAME PLAINTEXT MULTI PROOF TEST VALUES (N=3) ==========");
    System.out.println("Proof size: " + proof.toBytes().length + " bytes");
    System.out.println("Proof hex: " + proof.hexValue());
    System.out.println();

    // 9. Verify proof using verification participants
    SamePlaintextParticipant senderVerify = SamePlaintextParticipant.forVerification(
      senderCiphertext, senderPublicKey
    );
    SamePlaintextParticipant destVerify = SamePlaintextParticipant.forVerification(
      destCiphertext, destPublicKey
    );
    SamePlaintextParticipant issuerVerify = SamePlaintextParticipant.forVerification(
      issuerCiphertext, issuerPublicKey
    );

    List<SamePlaintextParticipant> participantsForVerify = Arrays.asList(senderVerify, destVerify, issuerVerify);
    boolean valid = proofVerifier.verify(proof, participantsForVerify, context);
    System.out.println("Proof valid: " + valid);
    System.out.println("================================================================\n");

    // 10. Assertions
    int expectedSize = SamePlaintextProofGenerator.proofSize(3);
    assertThat(proof.toBytes()).hasSize(expectedSize);
    assertThat(proof.participantCount()).isEqualTo(3);
    assertThat(valid).isTrue();
  }
}

