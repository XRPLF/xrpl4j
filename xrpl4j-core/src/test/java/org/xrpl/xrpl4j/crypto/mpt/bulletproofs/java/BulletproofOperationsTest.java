package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.mpt.RandomnessUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.BulletproofVectors;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Unit tests for {@link BulletproofOperations}.
 */
class BulletproofOperationsTest {

  private static final SecureRandom RANDOM = new SecureRandom();

  private BulletproofOperations ops;

  @BeforeEach
  void setUp() {
    ops = new BulletproofOperations();
  }

  // ============================================================================
  // Scalar Vector Operations Tests
  // ============================================================================

  @Test
  void testSumScalarVector() {
    // Create a vector of known scalars
    byte[][] vector = new byte[4][32];
    vector[0] = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(100));
    vector[1] = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(200));
    vector[2] = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(300));
    vector[3] = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(400));

    byte[] sum = ops.sumScalarVector(vector);

    // Expected: 100 + 200 + 300 + 400 = 1000
    byte[] expected = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(1000));
    assertThat(sum).isEqualTo(expected);
  }

  @Test
  void testSumScalarVectorEmpty() {
    byte[][] vector = new byte[0][32];
    byte[] sum = ops.sumScalarVector(vector);
    assertThat(sum).isEqualTo(Secp256k1Operations.scalarZero());
  }

  @Test
  void testIpaDotSimple() {
    // <[1, 2, 3], [4, 5, 6]> = 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
    byte[][] a = new byte[3][32];
    a[0] = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(1));
    a[1] = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(2));
    a[2] = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(3));

    byte[][] b = new byte[3][32];
    b[0] = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(4));
    b[1] = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(5));
    b[2] = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(6));

    byte[] dot = ops.ipaDot(a, b);

    byte[] expected = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(32));
    assertThat(dot).isEqualTo(expected);
  }

  @Test
  void testIpaDotMismatchedLengths() {
    byte[][] a = new byte[3][32];
    byte[][] b = new byte[4][32];

    assertThatThrownBy(() -> ops.ipaDot(a, b))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("same length");
  }

  // ============================================================================
  // Multi-Scalar Multiplication (MSM) Tests
  // ============================================================================

  @Test
  void testIpaMsmSinglePoint() {
    // scalar * G should equal multiplyG(scalar)
    byte[] scalar = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(42));
    ECPoint[] points = new ECPoint[] {Secp256k1Operations.getG()};
    byte[][] scalars = new byte[][] {scalar};

    ECPoint result = ops.ipaMsm(points, scalars);
    ECPoint expected = Secp256k1Operations.multiplyG(BigInteger.valueOf(42));

    assertThat(Secp256k1Operations.pointsEqual(result, expected)).isTrue();
  }

  @Test
  void testIpaMsmMultiplePoints() {
    // 2*G + 3*G = 5*G
    ECPoint G = Secp256k1Operations.getG();
    ECPoint[] points = new ECPoint[] {G, G};
    byte[][] scalars = new byte[][] {
      Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(2)),
      Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(3))
    };

    ECPoint result = ops.ipaMsm(points, scalars);
    ECPoint expected = Secp256k1Operations.multiplyG(BigInteger.valueOf(5));

    assertThat(Secp256k1Operations.pointsEqual(result, expected)).isTrue();
  }

  @Test
  void testIpaMsmWithDifferentPoints() {
    // 2*G + 3*(2*G) = 2*G + 6*G = 8*G
    ECPoint G = Secp256k1Operations.getG();
    ECPoint twoG = Secp256k1Operations.multiplyG(BigInteger.valueOf(2));
    ECPoint[] points = new ECPoint[] {G, twoG};
    byte[][] scalars = new byte[][] {
      Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(2)),
      Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(3))
    };

    ECPoint result = ops.ipaMsm(points, scalars);
    ECPoint expected = Secp256k1Operations.multiplyG(BigInteger.valueOf(8));

    assertThat(Secp256k1Operations.pointsEqual(result, expected)).isTrue();
  }

  // ============================================================================
  // Point Scalar Multiplication Tests
  // ============================================================================

  @Test
  void testPointScalarMul() {
    ECPoint G = Secp256k1Operations.getG();
    byte[] scalar = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(7));

    ECPoint result = ops.pointScalarMul(G, scalar);
    ECPoint expected = Secp256k1Operations.multiplyG(BigInteger.valueOf(7));

    assertThat(Secp256k1Operations.pointsEqual(result, expected)).isTrue();
  }

  // ============================================================================
  // Challenge Derivation Tests
  // ============================================================================

  @Test
  void testIpaDeriveChallengeFromDotDeterministic() {
    byte[] commitInp = new byte[32];
    RANDOM.nextBytes(commitInp);
    byte[] dot = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(12345));

    byte[] challenge1 = ops.ipaDeriveChallengeFromDot(commitInp, dot);
    byte[] challenge2 = ops.ipaDeriveChallengeFromDot(commitInp, dot);

    // Same inputs should produce same challenge
    assertThat(challenge1).isEqualTo(challenge2);
    // Challenge should be 32 bytes
    assertThat(challenge1).hasSize(32);
    // Challenge should be a valid scalar
    assertThat(Secp256k1Operations.isValidScalar(challenge1)).isTrue();
  }

  @Test
  void testIpaDeriveChallengeFromDotDifferentInputs() {
    byte[] commitInp = new byte[32];
    RANDOM.nextBytes(commitInp);
    byte[] dot1 = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(12345));
    byte[] dot2 = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(54321));

    byte[] challenge1 = ops.ipaDeriveChallengeFromDot(commitInp, dot1);
    byte[] challenge2 = ops.ipaDeriveChallengeFromDot(commitInp, dot2);

    // Different inputs should produce different challenges
    assertThat(challenge1).isNotEqualTo(challenge2);
  }

  @Test
  void testIpaDeriveChallenge() {
    byte[] transcript = new byte[32];
    RANDOM.nextBytes(transcript);
    ECPoint L = Secp256k1Operations.multiplyG(BigInteger.valueOf(123));
    ECPoint R = Secp256k1Operations.multiplyG(BigInteger.valueOf(456));

    byte[] challenge = ops.ipaDeriveChallenge(transcript, L, R);

    assertThat(challenge).hasSize(32);
    assertThat(Secp256k1Operations.isValidScalar(challenge)).isTrue();
  }

  // ============================================================================
  // Vector Computation Tests
  // ============================================================================

  @Test
  void testComputeVectorsLength() {
    UnsignedLong value = UnsignedLong.valueOf(12345);
    BulletproofVectors vectors = ops.computeVectors(value);

    assertThat(vectors.al().size()).isEqualTo(BulletproofOperations.N_BITS);
    assertThat(vectors.ar().size()).isEqualTo(BulletproofOperations.N_BITS);
    assertThat(vectors.sl().size()).isEqualTo(BulletproofOperations.N_BITS);
    assertThat(vectors.sr().size()).isEqualTo(BulletproofOperations.N_BITS);
  }

  @Test
  void testComputeVectorsBitEncoding() {
    // Test with value 5 = binary 101
    UnsignedLong value = UnsignedLong.valueOf(5);
    BulletproofVectors vectors = ops.computeVectors(value);

    byte[] one = Secp256k1Operations.scalarOne();
    byte[] zero = Secp256k1Operations.scalarZero();
    byte[] minusOne = Secp256k1Operations.scalarMinusOne();

    // Bit 0 (LSB) = 1: al[0] = 1, ar[0] = 0
    assertThat(vectors.al().get(0)).isEqualTo(one);
    assertThat(vectors.ar().get(0)).isEqualTo(zero);

    // Bit 1 = 0: al[1] = 0, ar[1] = -1
    assertThat(vectors.al().get(1)).isEqualTo(zero);
    assertThat(vectors.ar().get(1)).isEqualTo(minusOne);

    // Bit 2 = 1: al[2] = 1, ar[2] = 0
    assertThat(vectors.al().get(2)).isEqualTo(one);
    assertThat(vectors.ar().get(2)).isEqualTo(zero);

    // Bit 3 = 0: al[3] = 0, ar[3] = -1
    assertThat(vectors.al().get(3)).isEqualTo(zero);
    assertThat(vectors.ar().get(3)).isEqualTo(minusOne);
  }

  @Test
  void testComputeVectorsRandomness() {
    UnsignedLong value = UnsignedLong.valueOf(100);
    BulletproofVectors vectors1 = ops.computeVectors(value);
    BulletproofVectors vectors2 = ops.computeVectors(value);

    // al and ar should be the same (deterministic encoding)
    assertThat(listsDeepEquals(vectors1.al(), vectors2.al())).isTrue();
    assertThat(listsDeepEquals(vectors1.ar(), vectors2.ar())).isTrue();

    // sl and sr should be different (random)
    assertThat(listsDeepEquals(vectors1.sl(), vectors2.sl())).isFalse();
    assertThat(listsDeepEquals(vectors1.sr(), vectors2.sr())).isFalse();
  }

  /**
   * Helper method to compare two lists of byte arrays for deep equality.
   */
  private boolean listsDeepEquals(java.util.List<byte[]> list1, java.util.List<byte[]> list2) {
    if (list1.size() != list2.size()) {
      return false;
    }
    for (int i = 0; i < list1.size(); i++) {
      if (!Arrays.equals(list1.get(i), list2.get(i))) {
        return false;
      }
    }
    return true;
  }

  // ============================================================================
  // Commitment Tests
  // ============================================================================

  @Test
  void testCreateCommitment() {
    UnsignedLong value = UnsignedLong.valueOf(1000);
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar();
    ECPoint pkBase = Secp256k1Operations.multiplyG(BigInteger.valueOf(7)); // Some point

    ECPoint commitment = ops.createCommitment(value, blindingFactor, pkBase);

    // Verify: C = value*G + blindingFactor*pkBase
    ECPoint vG = Secp256k1Operations.multiplyG(value.bigIntegerValue());
    ECPoint rPk = Secp256k1Operations.multiply(pkBase, new BigInteger(1, blindingFactor));
    ECPoint expected = Secp256k1Operations.add(vG, rPk);

    assertThat(Secp256k1Operations.pointsEqual(commitment, expected)).isTrue();
  }

  @Test
  void testCreateCommitmentZeroValueThrows() {
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar();
    ECPoint pkBase = Secp256k1Operations.multiplyG(BigInteger.valueOf(7));

    assertThatThrownBy(() -> ops.createCommitment(UnsignedLong.ZERO, blindingFactor, pkBase))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("non-zero");
  }

  @Test
  void testCommitAS() {
    UnsignedLong value = UnsignedLong.valueOf(500);
    BulletproofVectors vectors = ops.computeVectors(value);
    byte[] rho = RandomnessUtils.generateRandomScalar();
    byte[] rhoS = RandomnessUtils.generateRandomScalar();
    ECPoint pkBase = Secp256k1Operations.multiplyG(BigInteger.valueOf(11));

    ECPoint[] as = ops.commitAS(vectors, rho, rhoS, pkBase);

    assertThat(as).hasSize(2);
    assertThat(as[0]).isNotNull(); // A
    assertThat(as[1]).isNotNull(); // S
    assertThat(as[0].isInfinity()).isFalse();
    assertThat(as[1].isInfinity()).isFalse();
  }

  // ============================================================================
  // IPA Compress Step Tests
  // ============================================================================

  @Test
  void testIpaCompressStep() {
    int n = 4;
    int halfN = 2;

    // Create test vectors
    byte[][] a = new byte[n][32];
    byte[][] b = new byte[n][32];
    ECPoint[] G = new ECPoint[n];
    ECPoint[] H = new ECPoint[n];

    for (int i = 0; i < n; i++) {
      a[i] = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(i + 1));
      b[i] = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(i + 5));
      G[i] = Secp256k1Operations.multiplyG(BigInteger.valueOf(i + 10));
      H[i] = Secp256k1Operations.multiplyG(BigInteger.valueOf(i + 20));
    }

    byte[] x = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(3));
    byte[] xInv = Secp256k1Operations.scalarInverse(x);

    // Compress
    ops.ipaCompressStep(a, b, G, H, halfN, x, xInv);

    // After compression, first halfN elements should be updated
    // a'[0] = a[0]*x + a[2]*x_inv = 1*3 + 3*x_inv
    // We just verify the operation completes and produces valid results
    assertThat(a[0]).hasSize(32);
    assertThat(b[0]).hasSize(32);
    assertThat(G[0]).isNotNull();
    assertThat(H[0]).isNotNull();
  }

  // ============================================================================
  // IPA Compute L/R Tests
  // ============================================================================

  @Test
  void testIpaComputeLR() {
    int halfN = 2;

    byte[][] aL = new byte[][] {Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(1)),
      Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(2))};
    byte[][] aR = new byte[][] {Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(3)),
      Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(4))};
    byte[][] bL = new byte[][] {Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(5)),
      Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(6))};
    byte[][] bR = new byte[][] {Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(7)),
      Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(8))};

    ECPoint[] gL = new ECPoint[] {
      Secp256k1Operations.multiplyG(BigInteger.valueOf(10)),
      Secp256k1Operations.multiplyG(BigInteger.valueOf(11))
    };
    ECPoint[] gR = new ECPoint[] {
      Secp256k1Operations.multiplyG(BigInteger.valueOf(12)),
      Secp256k1Operations.multiplyG(BigInteger.valueOf(13))
    };
    ECPoint[] hL = new ECPoint[] {
      Secp256k1Operations.multiplyG(BigInteger.valueOf(14)),
      Secp256k1Operations.multiplyG(BigInteger.valueOf(15))
    };
    ECPoint[] hR = new ECPoint[] {
      Secp256k1Operations.multiplyG(BigInteger.valueOf(16)),
      Secp256k1Operations.multiplyG(BigInteger.valueOf(17))
    };

    ECPoint g = Secp256k1Operations.multiplyG(BigInteger.valueOf(100));
    byte[] ux = Secp256k1Operations.unsignedLongToScalar(UnsignedLong.valueOf(42));

    ECPoint[] lr = ops.ipaComputeLR(aL, aR, bL, bR, gL, gR, hL, hR, g, ux);

    assertThat(lr).hasSize(2);
    assertThat(lr[0]).isNotNull(); // L
    assertThat(lr[1]).isNotNull(); // R
    assertThat(lr[0].isInfinity()).isFalse();
    assertThat(lr[1].isInfinity()).isFalse();
  }

  // ============================================================================
  // Random Scalar Generation Tests
  // ============================================================================

  @Test
  void testGenerateRandomScalar() {
    byte[] scalar1 = RandomnessUtils.generateRandomScalar();
    byte[] scalar2 = RandomnessUtils.generateRandomScalar();

    assertThat(scalar1).hasSize(32);
    assertThat(scalar2).hasSize(32);
    assertThat(Secp256k1Operations.isValidScalar(scalar1)).isTrue();
    assertThat(Secp256k1Operations.isValidScalar(scalar2)).isTrue();
    assertThat(scalar1).isNotEqualTo(scalar2);
  }
}
