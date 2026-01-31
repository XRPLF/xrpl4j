package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java;

import static org.xrpl.xrpl4j.crypto.mpt.RandomnessUtils.generateRandomScalar;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.BulletproofVectors;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Core bulletproof operations ported from C implementation. Provides helper functions for Inner Product Arguments (IPA)
 * and vector operations.
 */
@SuppressWarnings( {
  "checkstyle:ParameterName",
  "checkstyle:LocalVariableName",
  "checkstyle:VariableDeclarationUsageDistance"
})
public class BulletproofOperations {

  /**
   * Number of bits for range proofs (64-bit values).
   */
  public static final int N_BITS = 64;

  private final Secp256k1Operations secp256k1;
  private final SecureRandom random;

  /**
   * Constructs a new BulletproofOperations instance.
   */
  public BulletproofOperations() {
    this.secp256k1 = new Secp256k1Operations();
    this.random = new SecureRandom();
  }

  // TODO: Add a constructor that accepts SecureRandom.

  /**
   * Constructs a new BulletproofOperations instance with a custom Secp256k1Operations.
   *
   * @param secp256k1 The secp256k1 operations instance.
   */
  public BulletproofOperations(final Secp256k1Operations secp256k1) {
    this.secp256k1 = Objects.requireNonNull(secp256k1);
    this.random = new SecureRandom();
  }

  // ============================================================================
  // Scalar Vector Operations
  // ============================================================================

  /**
   * Computes the modular sum of a vector of 32-byte scalars. Port of: secp256k1_bulletproof_sum_scalar_vector
   *
   * @param vector Array of 32-byte scalars.
   *
   * @return The sum of all scalars mod n.
   */
  public byte[] sumScalarVector(final byte[][] vector) {
    Objects.requireNonNull(vector, "vector must not be null");
    byte[] sum = secp256k1.scalarZero();
    for (byte[] scalar : vector) {
      sum = secp256k1.scalarAdd(sum, scalar);
    }
    return sum;
  }

  /**
   * Computes the modular sum of a vector of 32-byte scalars. Port of: secp256k1_bulletproof_sum_scalar_vector
   *
   * @param vector List of 32-byte scalars.
   *
   * @return The sum of all scalars mod n.
   */
  public byte[] sumScalarVector(final List<byte[]> vector) {
    Objects.requireNonNull(vector, "vector must not be null");
    byte[] sum = secp256k1.scalarZero();
    for (byte[] scalar : vector) {
      sum = secp256k1.scalarAdd(sum, scalar);
    }
    return sum;
  }

  /**
   * Computes the modular dot product (inner product) of two scalar vectors. c = sum(a[i] * b[i]) mod n Port of:
   * secp256k1_bulletproof_ipa_dot
   *
   * @param a First scalar vector (each element is 32 bytes).
   * @param b Second scalar vector (each element is 32 bytes).
   *
   * @return The inner product as a 32-byte scalar.
   */
  @SuppressWarnings("checkstyle:MethodName")
  public byte[] ipaDot(final byte[][] a, final byte[][] b) {
    Objects.requireNonNull(a, "a must not be null");
    Objects.requireNonNull(b, "b must not be null");
    if (a.length != b.length) {
      throw new IllegalArgumentException("Vectors must have same length");
    }

    byte[] acc = secp256k1.scalarZero();
    for (int i = 0; i < a.length; i++) {
      byte[] term = secp256k1.scalarMultiply(a[i], b[i]);
      acc = secp256k1.scalarAdd(acc, term);
    }
    return acc;
  }

  /**
   * Computes Multi-Scalar Multiplication (MSM): R = sum(scalars[i] * points[i]). Port of:
   * secp256k1_bulletproof_ipa_msm
   *
   * @param points  Array of EC points.
   * @param scalars Array of 32-byte scalars (same length as points).
   *
   * @return The resulting point R.
   */
  public ECPoint ipaMsm(final ECPoint[] points, final byte[][] scalars) {
    Objects.requireNonNull(points, "points must not be null");
    Objects.requireNonNull(scalars, "scalars must not be null");
    if (points.length != scalars.length) {
      throw new IllegalArgumentException("Points and scalars must have same length");
    }
    if (points.length == 0) {
      return secp256k1.getInfinity();
    }

    // Initialize with first term
    ECPoint acc = secp256k1.multiply(points[0], new BigInteger(1, scalars[0]));

    // Accumulate remaining terms
    for (int i = 1; i < points.length; i++) {
      ECPoint term = secp256k1.multiply(points[i], new BigInteger(1, scalars[i]));
      acc = secp256k1.add(acc, term);
    }
    return acc;
  }

  /**
   * Computes scalar * point. Port of: secp256k1_bulletproof_point_scalar_mul
   *
   * @param point  The EC point.
   * @param scalar The 32-byte scalar.
   *
   * @return The resulting point.
   */
  public ECPoint pointScalarMul(final ECPoint point, final byte[] scalar) {
    Objects.requireNonNull(point, "point must not be null");
    Objects.requireNonNull(scalar, "scalar must not be null");
    return secp256k1.multiply(point, new BigInteger(1, scalar));
  }

  /**
   * Computes the cross-term commitments L and R for IPA. L = MSM(G_R, a_L) + MSM(H_L, b_R) + (c_L * ux) * g R =
   * MSM(G_L, a_R) + MSM(H_R, b_L) + (c_R * ux) * g Port of: secp256k1_bulletproof_ipa_compute_LR
   *
   * @param aL Left half of scalar vector a.
   * @param aR Right half of scalar vector a.
   * @param bL Left half of scalar vector b.
   * @param bR Right half of scalar vector b.
   * @param gL Left half of generator vector G.
   * @param gR Right half of generator vector G.
   * @param hL Left half of generator vector H.
   * @param hR Right half of generator vector H.
   * @param g  The blinding generator point.
   * @param ux The challenge scalar.
   *
   * @return Array of two points: [L, R].
   */
  @SuppressWarnings("checkstyle:MethodName")
  public ECPoint[] ipaComputeLR(
    final byte[][] aL, final byte[][] aR,
    final byte[][] bL, final byte[][] bR,
    final ECPoint[] gL, final ECPoint[] gR,
    final ECPoint[] hL, final ECPoint[] hR,
    final ECPoint g, final byte[] ux
  ) {

    // TODO: Confirm not needed since Java has the array length of N.
    // int halfN = aL.length;

    // Compute cross-term scalars: c_L = <a_L, b_R>, c_R = <a_R, b_L>
    byte[] cL = ipaDot(aL, bR);
    byte[] cR = ipaDot(aR, bL);

    // Compute L: L = MSM(G_R, a_L) + MSM(H_L, b_R) + (c_L * ux) * g
    ECPoint L = ipaMsm(gR, aL);
    ECPoint t1 = ipaMsm(hL, bR);
    L = secp256k1.add(L, t1);

    // Blinding term for L: c_L * ux * g
    byte[] cLux = secp256k1.scalarMultiply(cL, ux);
    ECPoint t2 = pointScalarMul(g, cLux);
    L = secp256k1.add(L, t2);

    // Compute R: R = MSM(G_L, a_R) + MSM(H_R, b_L) + (c_R * ux) * g
    ECPoint R = ipaMsm(gL, aR);
    t1 = ipaMsm(hR, bL);
    R = secp256k1.add(R, t1);

    // Blinding term for R: c_R * ux * g
    byte[] cRux = secp256k1.scalarMultiply(cR, ux);
    t2 = pointScalarMul(g, cRux);
    R = secp256k1.add(R, t2);

    return new ECPoint[] {L, R};
  }

  /**
   * Executes one IPA compression step (vector update). Computes new compressed vectors (a', b', G', H') in-place. Port
   * of: secp256k1_bulletproof_ipa_compress_step
   *
   * @param a     Scalar vector a (will be compressed in first half).
   * @param b     Scalar vector b (will be compressed in first half).
   * @param G     Generator vector G (will be compressed in first half).
   * @param H     Generator vector H (will be compressed in first half).
   * @param halfN The length of the new compressed vectors (n/2).
   * @param x     The challenge scalar x.
   * @param xInv  The inverse challenge scalar x^(-1).
   */
  public void ipaCompressStep(
    final byte[][] a, final byte[][] b,
    final ECPoint[] G, final ECPoint[] H,
    final int halfN, final byte[] x, final byte[] xInv) {

    for (int i = 0; i < halfN; i++) {
      // a'[i] = a[i] * x + a[i + halfN] * x_inv
      byte[] t1 = secp256k1.scalarMultiply(a[i], x);
      byte[] t2 = secp256k1.scalarMultiply(a[i + halfN], xInv);
      a[i] = secp256k1.scalarAdd(t1, t2);

      // b'[i] = b[i] * x_inv + b[i + halfN] * x
      t1 = secp256k1.scalarMultiply(b[i], xInv);
      t2 = secp256k1.scalarMultiply(b[i + halfN], x);
      b[i] = secp256k1.scalarAdd(t1, t2);

      // G'[i] = G[i] * x_inv + G[i + halfN] * x
      ECPoint gTerm = pointScalarMul(G[i], xInv);
      ECPoint hTerm = pointScalarMul(G[i + halfN], x);
      G[i] = secp256k1.add(gTerm, hTerm);

      // H'[i] = H[i] * x + H[i + halfN] * x_inv
      gTerm = pointScalarMul(H[i], x);
      hTerm = pointScalarMul(H[i + halfN], xInv);
      H[i] = secp256k1.add(gTerm, hTerm);
    }
  }

  /**
   * Derives a challenge scalar from a hash of inputs. ux = SHA256(commitInp || dot) mod n Port of:
   * secp256k1_bulletproof_ipa_derive_challenge
   *
   * @param commitInp 32-byte commitment input.
   * @param dot       32-byte scalar (inner product).
   *
   * @return The derived challenge scalar (32 bytes).
   */
  public byte[] ipaDeriveChallengeFromDot(final byte[] commitInp, final byte[] dot) {
    Objects.requireNonNull(commitInp, "commitInp must not be null");
    Objects.requireNonNull(dot, "dot must not be null");

    byte[] hashInput = new byte[64];
    System.arraycopy(commitInp, 0, hashInput, 0, 32);
    System.arraycopy(dot, 0, hashInput, 32, 32);

    // TODO: Use Sha512Half?
    byte[] hash = HashingUtils.sha256(hashInput).toByteArray();

    // Ensure the scalar is valid (non-zero and < curve order)
    BigInteger scalar = new BigInteger(1, hash);
    scalar = scalar.mod(secp256k1.getCurveOrder());
    if (scalar.equals(BigInteger.ZERO)) {
      // Extremely unlikely, but handle it
      scalar = BigInteger.ONE;
    }
    return secp256k1.toBytes32(scalar);
  }

  /**
   * Derives a challenge scalar from L and R points.
   *
   * @param transcript Current transcript state (32 bytes).
   * @param L          The L commitment point.
   * @param R          The R commitment point.
   *
   * @return The derived challenge scalar (32 bytes).
   */
  public byte[] ipaDeriveChallenge(final byte[] transcript, final ECPoint L, final ECPoint R) {
    byte[] lBytes = secp256k1.serializeCompressed(L);
    byte[] rBytes = secp256k1.serializeCompressed(R);

    // Hash: transcript || L || R
    byte[] hashInput = new byte[32 + 33 + 33];
    System.arraycopy(transcript, 0, hashInput, 0, 32);
    System.arraycopy(lBytes, 0, hashInput, 32, 33);
    System.arraycopy(rBytes, 0, hashInput, 65, 33);

    byte[] hash = HashingUtils.sha256(hashInput).toByteArray();

    BigInteger scalar = new BigInteger(1, hash);
    scalar = scalar.mod(secp256k1.getCurveOrder());
    if (scalar.equals(BigInteger.ZERO)) {
      scalar = BigInteger.ONE;
    }
    return secp256k1.toBytes32(scalar);
  }

  // ============================================================================
  // Vector Encoding and Commitment Functions
  // ============================================================================

  /**
   * Computes the four required scalar vectors for bulletproofs. Port of: secp256k1_bulletproof_compute_vectors
   *
   * @param value The 64-bit unsigned value to encode.
   *
   * @return A BulletproofVectors object containing al, ar, sl, sr vectors.
   */
  public BulletproofVectors computeVectors(final UnsignedLong value) {
    Objects.requireNonNull(value, "value must not be null");
    List<byte[]> al = new ArrayList<>(N_BITS);
    List<byte[]> ar = new ArrayList<>(N_BITS);
    List<byte[]> sl = new ArrayList<>(N_BITS);
    List<byte[]> sr = new ArrayList<>(N_BITS);

    byte[] oneScalar = secp256k1.scalarOne();
    byte[] zeroScalar = secp256k1.scalarZero();
    byte[] minusOneScalar = secp256k1.scalarMinusOne();

    long longValue = value.longValue();
    // Encode value into a_l and a_r
    for (int i = 0; i < N_BITS; i++) {
      int bit = (int) ((longValue >> i) & 1);
      if (bit == 1) {
        al.add(Arrays.copyOf(oneScalar, 32));
        ar.add(Arrays.copyOf(zeroScalar, 32));
      } else {
        al.add(Arrays.copyOf(zeroScalar, 32));
        ar.add(Arrays.copyOf(minusOneScalar, 32));
      }
    }

    // Generate random auxiliary scalars s_l and s_r
    for (int i = 0; i < N_BITS; i++) {
      sl.add(generateRandomScalar(this.getSecureRandom(), secp256k1));
      sr.add(generateRandomScalar(this.getSecureRandom(), secp256k1));
    }

    return BulletproofVectors.of(al, ar, sl, sr);
  }

  /**
   * Computes the initial Commitment Points A and S. A = (al_sum + rho) * G + ar_sum * Pk_base S = (sl_sum + rho_s) * G
   * + sr_sum * Pk_base Port of: secp256k1_bulletproof_commit_AS
   *
   * @param vectors The bulletproof vectors (al, ar, sl, sr).
   * @param rho     Random blinding scalar for A.
   * @param rhoS    Random blinding scalar for S.
   * @param pkBase  The dynamic generator point (recipient's public key).
   *
   * @return Array of two points: [A, S].
   */
  public ECPoint[] commitAS(
    final BulletproofVectors vectors,
    final byte[] rho, final byte[] rhoS,
    final ECPoint pkBase
  ) {

    // Sum vectors
    byte[] alSum = sumScalarVector(vectors.al());
    byte[] arSum = sumScalarVector(vectors.ar());
    byte[] slSum = sumScalarVector(vectors.sl());
    byte[] srSum = sumScalarVector(vectors.sr());

    // Compute A = (al_sum + rho) * G + ar_sum * Pk_base
    byte[] gScalarA = secp256k1.scalarAdd(alSum, rho);
    ECPoint gTermA = secp256k1.multiplyG(new BigInteger(1, gScalarA));
    ECPoint hTermA = pointScalarMul(pkBase, arSum);
    ECPoint A = secp256k1.add(gTermA, hTermA);

    // Compute S = (sl_sum + rho_s) * G + sr_sum * Pk_base
    byte[] gScalarS = secp256k1.scalarAdd(slSum, rhoS);
    ECPoint gTermS = secp256k1.multiplyG(new BigInteger(1, gScalarS));
    ECPoint hTermS = pointScalarMul(pkBase, srSum);
    ECPoint S = secp256k1.add(gTermS, hTermS);

    return new ECPoint[] {A, S};
  }

  /**
   * Computes the Pedersen Commitment: C = value*G + blindingFactor*Pk_base. Port of:
   * secp256k1_bulletproof_create_commitment
   *
   * @param value          The unsigned value to commit to.
   * @param blindingFactor The blinding factor (32 bytes).
   * @param pkBase         The dynamic generator point.
   *
   * @return The commitment point C.
   */
  public ECPoint createCommitment(final UnsignedLong value, final byte[] blindingFactor, final ECPoint pkBase) {
    Objects.requireNonNull(value, "value must not be null");
    if (value.equals(UnsignedLong.ZERO)) {
      throw new IllegalArgumentException("Commitment must be to a non-zero value");
    }

    // v * G
    ECPoint vG = secp256k1.multiplyG(value.bigIntegerValue());

    // r * Pk_base
    ECPoint rPk = pointScalarMul(pkBase, blindingFactor);

    // C = v*G + r*Pk_base
    return secp256k1.add(vG, rPk);
  }

  /**
   * Gets the secure random instance used by this operations class.
   *
   * @return The secure random instance.
   */
  public SecureRandom getSecureRandom() {
    return random;
  }

}
