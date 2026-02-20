package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.mpt.RandomnessUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.Bulletproof;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.IpaProof;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Unit tests for {@link BulletproofProver}.
 */
class BulletproofProverTest {

  private Secp256k1Operations secp256k1;
  private BulletproofOperations bulletproofOperations;
  private BulletproofProver prover;

  @BeforeEach
  void setUp() {
    this.secp256k1 = new Secp256k1Operations();
    this.bulletproofOperations = new BulletproofOperations(secp256k1);
    this.prover = new BulletproofProver(bulletproofOperations, secp256k1);
  }

  // ============================================================================
  // Constructor Tests
  // ============================================================================

  @Test
  void testConstructorWithNulls() {
    assertThrows(NullPointerException.class, () -> new BulletproofProver(null, secp256k1));
    assertThrows(NullPointerException.class, () -> new BulletproofProver(bulletproofOperations, null));
  }

  // ============================================================================
  // IPA Prover Tests
  // ============================================================================

  @Test
  void testRunIpaProverWithPowerOfTwoVectors() {
    int n = 4; // Power of two
    ECPoint pkBase = secp256k1.multiplyG(BigInteger.valueOf(7));

    // Create generator vectors
    ECPoint[] gVec = new ECPoint[n];
    ECPoint[] hVec = new ECPoint[n];
    for (int i = 0; i < n; i++) {
      gVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 10));
      hVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 100));
    }

    // Create scalar vectors
    byte[][] aVec = new byte[n][32];
    byte[][] bVec = new byte[n][32];
    for (int i = 0; i < n; i++) {
      aVec[i] = secp256k1.unsignedLongToScalar(UnsignedLong.valueOf(i + 1));
      bVec[i] = secp256k1.unsignedLongToScalar(UnsignedLong.valueOf(i + 5));
    }

    // Create transcript input
    byte[] commitInp = new byte[32];
    commitInp[0] = 0x42;

    IpaProof proof = prover.runIpaProver(pkBase, gVec, hVec, aVec, bVec, commitInp);

    // Verify proof structure
    assertThat(proof).isNotNull();
    assertThat(proof.lPoints()).hasSize(2); // log2(4) = 2 rounds
    assertThat(proof.rPoints()).hasSize(2);
    assertThat(proof.aFinal()).hasSize(32);
    assertThat(proof.bFinal()).hasSize(32);
    assertThat(proof.dotProduct()).hasSize(32);

    // Verify L and R points are valid (not infinity)
    for (int i = 0; i < 2; i++) {
      assertThat(proof.lPoints().get(i)).isNotNull();
      assertThat(proof.rPoints().get(i)).isNotNull();
      assertThat(proof.lPoints().get(i).isInfinity()).isFalse();
      assertThat(proof.rPoints().get(i).isInfinity()).isFalse();
    }
  }

  @Test
  void testRunIpaProverWithLargerVectors() {
    int n = 8; // Power of two
    ECPoint pkBase = secp256k1.multiplyG(BigInteger.valueOf(11));

    ECPoint[] gVec = new ECPoint[n];
    ECPoint[] hVec = new ECPoint[n];
    byte[][] aVec = new byte[n][32];
    byte[][] bVec = new byte[n][32];

    for (int i = 0; i < n; i++) {
      gVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 20));
      hVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 200));
      aVec[i] = secp256k1.unsignedLongToScalar(UnsignedLong.valueOf(i + 1));
      bVec[i] = secp256k1.unsignedLongToScalar(UnsignedLong.valueOf(n - i));
    }

    byte[] commitInp = new byte[32];
    commitInp[31] = 0x01;

    IpaProof proof = prover.runIpaProver(pkBase, gVec, hVec, aVec, bVec, commitInp);

    // log2(8) = 3 rounds
    assertThat(proof.lPoints()).hasSize(3);
    assertThat(proof.rPoints()).hasSize(3);
  }

  @Test
  void testRunIpaProverNonPowerOfTwoThrows() {
    int n = 5; // Not a power of two
    ECPoint pkBase = secp256k1.multiplyG(BigInteger.valueOf(7));

    ECPoint[] gVec = new ECPoint[n];
    ECPoint[] hVec = new ECPoint[n];
    byte[][] aVec = new byte[n][32];
    byte[][] bVec = new byte[n][32];

    for (int i = 0; i < n; i++) {
      gVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 10));
      hVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 100));
      aVec[i] = secp256k1.unsignedLongToScalar(UnsignedLong.valueOf(i + 1));
      bVec[i] = secp256k1.unsignedLongToScalar(UnsignedLong.valueOf(i + 5));
    }

    byte[] commitInp = new byte[32];

    assertThatThrownBy(() -> prover.runIpaProver(pkBase, gVec, hVec, aVec, bVec, commitInp))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("power of two");
  }

  @Test
  void testRunIpaProverEmptyVectorsThrows() {
    ECPoint pkBase = secp256k1.multiplyG(BigInteger.valueOf(7));
    ECPoint[] gVec = new ECPoint[0];
    ECPoint[] hVec = new ECPoint[0];
    byte[][] aVec = new byte[0][32];
    byte[][] bVec = new byte[0][32];
    byte[] commitInp = new byte[32];

    assertThatThrownBy(() -> prover.runIpaProver(pkBase, gVec, hVec, aVec, bVec, commitInp))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void testRunIpaProverDotProductCorrect() {
    int n = 2;
    ECPoint pkBase = secp256k1.multiplyG(BigInteger.valueOf(7));

    ECPoint[] gVec = new ECPoint[n];
    ECPoint[] hVec = new ECPoint[n];
    for (int i = 0; i < n; i++) {
      gVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 10));
      hVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 100));
    }

    // a = [2, 3], b = [4, 5]
    // dot = 2*4 + 3*5 = 8 + 15 = 23
    byte[][] aVec = new byte[][] {
      secp256k1.unsignedLongToScalar(UnsignedLong.valueOf(2)),
      secp256k1.unsignedLongToScalar(UnsignedLong.valueOf(3))
    };
    byte[][] bVec = new byte[][] {
      secp256k1.unsignedLongToScalar(UnsignedLong.valueOf(4)),
      secp256k1.unsignedLongToScalar(UnsignedLong.valueOf(5))
    };

    byte[] commitInp = new byte[32];

    IpaProof proof = prover.runIpaProver(pkBase, gVec, hVec, aVec, bVec, commitInp);

    byte[] expectedDot = secp256k1.unsignedLongToScalar(UnsignedLong.valueOf(23));
    assertThat(proof.dotProduct()).isEqualTo(expectedDot);
  }

  // ============================================================================
  // Prove Function Tests
  // ============================================================================

  @Test
  void testProveGeneratesValidBulletproof() {
    UnsignedLong value = UnsignedLong.valueOf(12345);
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    ECPoint pkBase = secp256k1.multiplyG(BigInteger.valueOf(7));

    // Create generator vectors (must be N_BITS = 64 elements)
    int n = BulletproofOperations.N_BITS;
    ECPoint[] gVec = new ECPoint[n];
    ECPoint[] hVec = new ECPoint[n];
    for (int i = 0; i < n; i++) {
      gVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 1000));
      hVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 2000));
    }

    Bulletproof proof = prover.prove(value, blindingFactor, pkBase, gVec, hVec);

    // Verify proof structure
    assertThat(proof).isNotNull();
    assertThat(proof.vCommitment()).isNotNull();
    assertThat(proof.vCommitment().isInfinity()).isFalse();
    assertThat(proof.aCommitment()).isNotNull();
    assertThat(proof.aCommitment().isInfinity()).isFalse();
    assertThat(proof.sCommitment()).isNotNull();
    assertThat(proof.sCommitment().isInfinity()).isFalse();
    assertThat(proof.ipaProof()).isNotNull();

    // log2(64) = 6 rounds
    assertThat(proof.ipaProof().lPoints()).hasSize(6);
    assertThat(proof.ipaProof().rPoints()).hasSize(6);
  }

  @Test
  void testProveZeroValueThrows() {
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    ECPoint pkBase = secp256k1.multiplyG(BigInteger.valueOf(7));

    int n = BulletproofOperations.N_BITS;
    ECPoint[] gVec = new ECPoint[n];
    ECPoint[] hVec = new ECPoint[n];
    for (int i = 0; i < n; i++) {
      gVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 1000));
      hVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 2000));
    }

    assertThatThrownBy(() -> prover.prove(UnsignedLong.ZERO, blindingFactor, pkBase, gVec, hVec))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("non-zero");
  }

  @Test
  void testProveCommitmentMatchesExpected() {
    UnsignedLong value = UnsignedLong.valueOf(1000);
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    ECPoint pkBase = secp256k1.multiplyG(BigInteger.valueOf(7));

    int n = BulletproofOperations.N_BITS;
    ECPoint[] gVec = new ECPoint[n];
    ECPoint[] hVec = new ECPoint[n];
    for (int i = 0; i < n; i++) {
      gVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 1000));
      hVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 2000));
    }

    Bulletproof proof = prover.prove(value, blindingFactor, pkBase, gVec, hVec);

    // Manually compute expected commitment: V = value*G + blindingFactor*pkBase
    ECPoint expectedV = bulletproofOperations.createCommitment(value, blindingFactor, pkBase);

    assertThat(secp256k1.pointsEqual(proof.vCommitment(), expectedV)).isTrue();
  }

  @Test
  void testProveDifferentValuesProduceDifferentProofs() {
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    ECPoint pkBase = secp256k1.multiplyG(BigInteger.valueOf(7));

    int n = BulletproofOperations.N_BITS;
    ECPoint[] gVec1 = new ECPoint[n];
    ECPoint[] hVec1 = new ECPoint[n];
    ECPoint[] gVec2 = new ECPoint[n];
    ECPoint[] hVec2 = new ECPoint[n];
    for (int i = 0; i < n; i++) {
      gVec1[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 1000));
      hVec1[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 2000));
      gVec2[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 1000));
      hVec2[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 2000));
    }

    Bulletproof proof1 = prover.prove(UnsignedLong.valueOf(100), blindingFactor, pkBase, gVec1, hVec1);
    Bulletproof proof2 = prover.prove(UnsignedLong.valueOf(200), blindingFactor, pkBase, gVec2, hVec2);

    // Different values should produce different commitments
    assertThat(secp256k1.pointsEqual(proof1.vCommitment(), proof2.vCommitment())).isFalse();
  }

  @Test
  void testProveMaxValue() {
    UnsignedLong value = UnsignedLong.MAX_VALUE;
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    ECPoint pkBase = secp256k1.multiplyG(BigInteger.valueOf(7));

    int n = BulletproofOperations.N_BITS;
    ECPoint[] gVec = new ECPoint[n];
    ECPoint[] hVec = new ECPoint[n];
    for (int i = 0; i < n; i++) {
      gVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 1000));
      hVec[i] = secp256k1.multiplyG(BigInteger.valueOf(i + 2000));
    }

    Bulletproof proof = prover.prove(value, blindingFactor, pkBase, gVec, hVec);

    assertThat(proof).isNotNull();
    assertThat(proof.vCommitment().isInfinity()).isFalse();
  }
}
