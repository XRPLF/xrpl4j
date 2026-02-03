package org.xrpl.xrpl4j.crypto.mpt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Unit tests for {@link SchnorrProofOfKnowledge}.
 *
 * <p>These tests mirror the C tests in test_pok_sk.c to ensure compatibility.</p>
 */
class SchnorrProofOfKnowledgeTest {

  private Secp256k1Operations secp256k1;
  private SecureRandom secureRandom;
  private SchnorrProofOfKnowledge schnorrPok;

  @BeforeEach
  void setUp() {
    secp256k1 = new Secp256k1Operations();
    secureRandom = new SecureRandom();
    schnorrPok = new SchnorrProofOfKnowledge(secp256k1, secureRandom);
  }

  @Test
  void generateAndVerifyValidProof() {
    // Setup: Generate keypair and random context
    byte[] sk = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    BigInteger skInt = new BigInteger(1, sk);
    ECPoint pk = secp256k1.multiplyG(skInt);
    byte[] contextId = new byte[32];
    secureRandom.nextBytes(contextId);

    // Generate proof
    byte[] proof = schnorrPok.generate(sk, pk, contextId);

    // Verify proof length is 65 bytes (33 + 32)
    assertThat(proof).hasSize(65);

    // Verify proof is valid
    boolean isValid = schnorrPok.verify(proof, pk, contextId);
    assertThat(isValid).isTrue();

    System.out.println("SUCCESS: Valid PoK verified.");
  }

  @Test
  void verifyFailsWithInvalidContext() {
    // Setup: Generate keypair and random context
    byte[] sk = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    BigInteger skInt = new BigInteger(1, sk);
    ECPoint pk = secp256k1.multiplyG(skInt);
    byte[] contextId = new byte[32];
    secureRandom.nextBytes(contextId);

    // Generate proof with original context
    byte[] proof = schnorrPok.generate(sk, pk, contextId);

    // Create wrong context by flipping first byte
    byte[] wrongContext = contextId.clone();
    wrongContext[0] ^= (byte) 0xFF;

    // Verify fails with wrong context
    boolean isValid = schnorrPok.verify(proof, pk, wrongContext);
    assertThat(isValid).isFalse();

    System.out.println("SUCCESS: Invalid context correctly rejected.");
  }

  @Test
  void verifyFailsWithCorruptedProofScalar() {
    // Setup: Generate keypair and random context
    byte[] sk = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    BigInteger skInt = new BigInteger(1, sk);
    ECPoint pk = secp256k1.multiplyG(skInt);
    byte[] contextId = new byte[32];
    secureRandom.nextBytes(contextId);

    // Generate proof
    byte[] proof = schnorrPok.generate(sk, pk, contextId);

    // Corrupt the scalar s (last byte of proof, index 64)
    proof[64] ^= (byte) 0xFF;

    // Verify fails with corrupted proof
    boolean isValid = schnorrPok.verify(proof, pk, contextId);
    assertThat(isValid).isFalse();

    System.out.println("SUCCESS: Corrupted proof correctly rejected.");
  }

  @Test
  void verifyFailsWithCorruptedProofPoint() {
    // Setup: Generate keypair and random context
    byte[] sk = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    BigInteger skInt = new BigInteger(1, sk);
    ECPoint pk = secp256k1.multiplyG(skInt);
    byte[] contextId = new byte[32];
    secureRandom.nextBytes(contextId);

    // Generate proof
    byte[] proof = schnorrPok.generate(sk, pk, contextId);

    // Corrupt the point T (first byte of proof)
    proof[0] ^= (byte) 0xFF;

    // Verify fails with corrupted proof
    boolean isValid = schnorrPok.verify(proof, pk, contextId);
    assertThat(isValid).isFalse();

    System.out.println("SUCCESS: Corrupted point correctly rejected.");
  }

  @Test
  void verifyFailsWithWrongPublicKey() {
    // Setup: Generate two keypairs
    byte[] sk1 = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    BigInteger sk1Int = new BigInteger(1, sk1);
    ECPoint pk1 = secp256k1.multiplyG(sk1Int);

    byte[] sk2 = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    BigInteger sk2Int = new BigInteger(1, sk2);
    ECPoint pk2 = secp256k1.multiplyG(sk2Int);

    byte[] contextId = new byte[32];
    secureRandom.nextBytes(contextId);

    // Generate proof for pk1
    byte[] proof = schnorrPok.generate(sk1, pk1, contextId);

    // Verify fails with pk2
    boolean isValid = schnorrPok.verify(proof, pk2, contextId);
    assertThat(isValid).isFalse();

    System.out.println("SUCCESS: Wrong public key correctly rejected.");
  }

  @Test
  void verifyFailsWithWrongProofLength() {
    byte[] sk = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    BigInteger skInt = new BigInteger(1, sk);
    ECPoint pk = secp256k1.multiplyG(skInt);
    byte[] contextId = new byte[32];

    // Wrong proof length
    byte[] shortProof = new byte[64];
    assertThat(schnorrPok.verify(shortProof, pk, contextId)).isFalse();

    byte[] longProof = new byte[66];
    assertThat(schnorrPok.verify(longProof, pk, contextId)).isFalse();
  }

  @Test
  void generateThrowsWithInvalidInputs() {
    byte[] sk = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    BigInteger skInt = new BigInteger(1, sk);
    ECPoint pk = secp256k1.multiplyG(skInt);
    byte[] contextId = new byte[32];

    // Wrong private key length
    assertThatThrownBy(() -> schnorrPok.generate(new byte[31], pk, contextId))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("privateKey must be 32 bytes");

    // Wrong context length
    assertThatThrownBy(() -> schnorrPok.generate(sk, pk, new byte[31]))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("contextId must be 32 bytes");
  }
}

