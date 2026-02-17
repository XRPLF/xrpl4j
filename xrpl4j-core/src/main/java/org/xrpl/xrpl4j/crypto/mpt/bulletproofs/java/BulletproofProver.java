package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java;


import static org.xrpl.xrpl4j.crypto.mpt.RandomnessUtils.generateRandomScalar;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.Bulletproof;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.BulletproofVectors;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.IpaProof;

import java.util.Arrays;
import java.util.Objects;

/**
 * Bulletproof prover implementation. Ports the IPA prover and main prove function from C.
 */
// TODO: Make an interface?
public class BulletproofProver {

  private final BulletproofOperations bulletproofOperations;

  /**
   * Constructs a new BulletproofProver with custom operations.
   *
   * @param bulletproofOperations The bulletproof operations instance.
   */
  public BulletproofProver(final BulletproofOperations bulletproofOperations) {
    this.bulletproofOperations = Objects.requireNonNull(bulletproofOperations);
  }

  /**
   * Runs the core recursive Inner Product Argument (IPA) Prover. Iteratively compresses scalar and generator vectors
   * down to final scalars, while recording the L/R proof points. Port of: secp256k1_bulletproof_run_ipa_prover
   *
   * @param g         The blinding generator point (Pk_recipient).
   * @param gVec      Generator vector G (will be modified in-place).
   * @param hVec      Generator vector H (will be modified in-place).
   * @param aVec      Scalar vector a (will be modified in-place).
   * @param bVec      Scalar vector b (will be modified in-place).
   * @param commitInp 32-byte initial commitment input for transcript.
   *
   * @return The IPA proof containing L/R points and final scalars.
   */
  public IpaProof runIpaProver(
    final ECPoint g,
    final ECPoint[] gVec,
    final ECPoint[] hVec,
    final byte[][] aVec,
    final byte[][] bVec,
    final byte[] commitInp
  ) {

    int n = aVec.length;

    // Validate n is power of two
    if (n == 0 || (n & (n - 1)) != 0) {
      throw new IllegalArgumentException("Vector length must be a power of two");
    }

    // Calculate number of rounds
    int rounds = 0;
    int temp = n;
    // TODO: Guard against infinite while loop
    while (temp > 1) {
      temp >>= 1;
      rounds++;
    }

    // Allocate output arrays
    ECPoint[] lOut = new ECPoint[rounds];
    ECPoint[] rOut = new ECPoint[rounds];

    // Compute initial dot product
    byte[] dotOut = bulletproofOperations.ipaDot(aVec, bVec);

    // Compute initial challenge ux = H(commit_inp || dot_out)
    byte[] xScalar = bulletproofOperations.ipaDeriveChallengeFromDot(commitInp, dotOut);

    int curN = n;

    // Recursive IPA loop
    for (int r = 0; r < rounds; r++) {
      int halfN = curN >> 1;

      // Split vectors into left and right halves
      byte[][] aL = Arrays.copyOfRange(aVec, 0, halfN);
      byte[][] aR = Arrays.copyOfRange(aVec, halfN, curN);
      byte[][] bL = Arrays.copyOfRange(bVec, 0, halfN);
      byte[][] bR = Arrays.copyOfRange(bVec, halfN, curN);
      ECPoint[] gL = Arrays.copyOfRange(gVec, 0, halfN);
      ECPoint[] gR = Arrays.copyOfRange(gVec, halfN, curN);
      ECPoint[] hL = Arrays.copyOfRange(hVec, 0, halfN);
      ECPoint[] hR = Arrays.copyOfRange(hVec, halfN, curN);

      // Compute L_r, R_r commitments
      ECPoint[] lr = bulletproofOperations.ipaComputeLR(aL, aR, bL, bR, gL, gR, hL, hR, g, xScalar);
      lOut[r] = lr[0];
      rOut[r] = lr[1];

      // Generate next round challenge from L, R
      xScalar = bulletproofOperations.ipaDeriveChallenge(commitInp, lr[0], lr[1]);
      byte[] xInv = Secp256k1Operations.scalarInverse(xScalar);

      // Compress vectors in-place
      bulletproofOperations.ipaCompressStep(aVec, bVec, gVec, hVec, halfN, xScalar, xInv);

      curN = halfN;
    }

    // Final result: first elements of compressed vectors
    byte[] aFinal = Arrays.copyOf(aVec[0], 32);
    byte[] bFinal = Arrays.copyOf(bVec[0], 32);

    return IpaProof.of(Lists.newArrayList(lOut), Lists.newArrayList(rOut), aFinal, bFinal, dotOut);
  }

  /**
   * Generates a bulletproof for a given value. Port of: secp256k1_bulletproof_prove (partial - structure only)
   *
   * @param value          The unsigned value to prove is in range [0, 2^64).
   * @param blindingFactor The blinding factor for the commitment.
   * @param pkBase         The dynamic H point (recipient's public key).
   * @param gVec           Generator vector G (length N_BITS).
   * @param hVec           Generator vector H (length N_BITS).
   *
   * @return The bulletproof containing commitment and IPA proof.
   */
  public Bulletproof prove(
    final UnsignedLong value,
    final byte[] blindingFactor,
    final ECPoint pkBase,
    final ECPoint[] gVec,
    final ECPoint[] hVec
  ) {
    Objects.requireNonNull(value, "value must not be null");
    if (value.equals(UnsignedLong.ZERO)) {
      throw new IllegalArgumentException("Range proofs are for non-zero values");
    }

    // 1. Create commitment: V = v*G + r*Pk_base
    ECPoint vCommitment = bulletproofOperations.createCommitment(value, blindingFactor, pkBase);

    // 2. Compute vectors (a_l, a_r, s_l, s_r)
    BulletproofVectors vectors = bulletproofOperations.computeVectors(value);

    // 3. Generate random blinding scalars
    byte[] rho = generateRandomScalar();
    byte[] rhoS = generateRandomScalar();

    // 4. Compute A and S commitments
    ECPoint[] as = bulletproofOperations.commitAS(vectors, rho, rhoS, pkBase);
    ECPoint A = as[0];
    ECPoint S = as[1];

    // 5. Create transcript input from commitment
    byte[] commitInp = Secp256k1Operations.serializeCompressed(vCommitment);
    // Pad or truncate to 32 bytes
    byte[] transcript = new byte[32];
    System.arraycopy(commitInp, 0, transcript, 0, Math.min(commitInp.length, 32));

    // 6. Run IPA prover
    // Note: For a complete implementation, we would need to:
    // - Compute challenges y, z from A, S
    // - Compute polynomial T(x) and commitments T1, T2
    // - Compute final l(x), r(x) vectors
    // For now, we run IPA on the base vectors as a structural demonstration
    // Convert lists to arrays since runIpaProver modifies them in-place
    byte[][] alArray = vectors.al().toArray(new byte[0][]);
    byte[][] arArray = vectors.ar().toArray(new byte[0][]);
    IpaProof ipaProof = runIpaProver(pkBase, gVec, hVec, alArray, arArray, transcript);

    return Bulletproof.of(vCommitment, A, S, ipaProof);
  }

  // TODO: Remove getter from core interface.

  /**
   * Gets the underlying BulletproofOperations instance.
   *
   * @return The operations instance.
   */
  public BulletproofOperations getOperations() {
    return bulletproofOperations;
  }
}
