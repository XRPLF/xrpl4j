package org.xrpl.xrpl4j.crypto.mpt.port.bc;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.port.RangeProofVerifierPort;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * BouncyCastle implementation of {@link RangeProofVerifierPort}.
 *
 * <p>Port of {@code secp256k1_bulletproof_verify_agg} from bulletproof_aggregated.c.</p>
 */
public class BcRangeProofVerifierPort implements RangeProofVerifierPort {

  private static final int SCALAR_SIZE = 32;
  private static final int COMPRESSED_POINT_SIZE = 33;
  private static final int VALUE_BITS = 64;
  private static final String RANGE_DOMAIN = "MPT_BULLETPROOF_RANGE";

  @Override
  public boolean verifyProof(
    UnsignedByteArray[] gVec,
    UnsignedByteArray[] hVec,
    UnsignedByteArray proof,
    UnsignedByteArray[] commitmentVec,
    UnsignedByteArray pkBase,
    UnsignedByteArray contextId
  ) {
    try {
      // Convert UnsignedByteArray[] to ECPoint[]
      ECPoint[] gVecPoints = new ECPoint[gVec.length];
      ECPoint[] hVecPoints = new ECPoint[hVec.length];
      for (int i = 0; i < gVec.length; i++) {
        gVecPoints[i] = Secp256k1Operations.deserialize(gVec[i].toByteArray());
      }
      for (int i = 0; i < hVec.length; i++) {
        hVecPoints[i] = Secp256k1Operations.deserialize(hVec[i].toByteArray());
      }

      return verifyProofInternal(gVecPoints, hVecPoints, proof.toByteArray(), commitmentVec,
        pkBase.toByteArray(), contextId != null ? contextId.toByteArray() : null);
    } catch (Exception e) {
      return false;
    }
  }

  private boolean verifyProofInternal(
    ECPoint[] gVec,
    ECPoint[] hVec,
    byte[] proof,
    UnsignedByteArray[] commitmentVec,
    byte[] pkBaseBytes,
    byte[] contextId
  ) {
    final int m = commitmentVec.length;
    if (m == 0 || (m & (m - 1)) != 0) {
      return false;
    }

    final int n = VALUE_BITS * m;
    final int rounds = ipaRounds(n);
    final int expectedLen = 292 + 66 * rounds;

    if (proof.length != expectedLen) {
      return false;
    }

    // Parse pk_base
    ECPoint pkBase = Secp256k1Operations.deserialize(pkBaseBytes);
    ECPoint u = Secp256k1Operations.getU();

    // Parse commitments
    ECPoint[] commitments = new ECPoint[m];
    for (int i = 0; i < m; i++) {
      commitments[i] = Secp256k1Operations.deserialize(commitmentVec[i].toByteArray());
    }

    // Unpack proof
    int offset = 0;
    ECPoint a = Secp256k1Operations.deserialize(Arrays.copyOfRange(proof, offset, offset + 33));
    offset += 33;
    ECPoint s = Secp256k1Operations.deserialize(Arrays.copyOfRange(proof, offset, offset + 33));
    offset += 33;
    ECPoint t1 = Secp256k1Operations.deserialize(Arrays.copyOfRange(proof, offset, offset + 33));
    offset += 33;
    ECPoint t2 = Secp256k1Operations.deserialize(Arrays.copyOfRange(proof, offset, offset + 33));
    offset += 33;

    ECPoint[] lVec = new ECPoint[rounds];
    ECPoint[] rVec = new ECPoint[rounds];
    for (int i = 0; i < rounds; i++) {
      lVec[i] = Secp256k1Operations.deserialize(Arrays.copyOfRange(proof, offset, offset + 33));
      offset += 33;
    }
    for (int i = 0; i < rounds; i++) {
      rVec[i] = Secp256k1Operations.deserialize(Arrays.copyOfRange(proof, offset, offset + 33));
      offset += 33;
    }

    byte[] aFinal = Arrays.copyOfRange(proof, offset, offset + 32);
    offset += 32;
    byte[] bFinal = Arrays.copyOfRange(proof, offset, offset + 32);
    offset += 32;
    byte[] tHat = Arrays.copyOfRange(proof, offset, offset + 32);
    offset += 32;
    byte[] tauX = Arrays.copyOfRange(proof, offset, offset + 32);
    offset += 32;
    byte[] mu = Arrays.copyOfRange(proof, offset, offset + 32);

    // Validate scalars
    if (!Secp256k1Operations.isValidScalar(aFinal) ||
        !Secp256k1Operations.isValidScalar(bFinal) ||
        !Secp256k1Operations.isValidScalar(tHat) ||
        !Secp256k1Operations.isValidScalar(tauX) ||
        !Secp256k1Operations.isValidScalar(mu)) {
      return false;
    }

    // Serialize points for Fiat-Shamir
    byte[] aSer = Secp256k1Operations.serializeCompressed(a);
    byte[] sSer = Secp256k1Operations.serializeCompressed(s);
    byte[] t1Ser = Secp256k1Operations.serializeCompressed(t1);
    byte[] t2Ser = Secp256k1Operations.serializeCompressed(t2);

    byte[][] commitmentsSer = new byte[m][];
    for (int i = 0; i < m; i++) {
      commitmentsSer[i] = Secp256k1Operations.serializeCompressed(commitments[i]);
    }

    // Derive y, z, x challenges
    byte[] y = deriveYChallenge(contextId, commitmentsSer, aSer, sSer);
    byte[] z = deriveZChallenge(contextId, commitmentsSer, aSer, sSer, y);
    byte[] x = deriveXChallenge(contextId, aSer, sSer, y, z, t1Ser, t2Ser);

    if (!Secp256k1Operations.isValidScalar(y) ||
        !Secp256k1Operations.isValidScalar(z) ||
        !Secp256k1Operations.isValidScalar(x)) {
      return false;
    }

    // Compute y powers and inverse powers
    byte[][] yPowers = computeScalarPowers(y, n);
    byte[] yInv = Secp256k1Operations.scalarInverse(y);
    byte[][] yInvPowers = computeScalarPowers(yInv, n);

    // z^2
    byte[] zSq = Secp256k1Operations.scalarMultiply(z, z);

    // Compute delta(y, z)
    byte[] delta = computeDelta(y, z, zSq, yPowers, m);

    // Step 3: Verify polynomial identity
    // LHS = t_hat*G + tau_x*pkBase
    ECPoint lhs = computeLhs(tHat, tauX, pkBase);

    // RHS = sum_j z^(j+2)*V_j + delta*G + x*T1 + x^2*T2
    ECPoint rhs = computeRhs(commitments, z, delta, x, t1, t2, m);

    if (!lhs.equals(rhs)) {
      return false;
    }

    // Step 4: Build P and verify IPA
    byte[] ipaTranscript = buildIpaTranscript(contextId, aSer, sSer, t1Ser, t2Ser, y, z, x, tHat);
    byte[] uxScalar = deriveIpaBindingChallenge(ipaTranscript, tHat);

    // Build P
    ECPoint p = buildP(a, s, x, z, gVec, hVec, yPowers, yInvPowers, m, u, uxScalar, tHat, mu, pkBase);

    // Build H' = y^{-k} * H_k
    ECPoint[] hPrime = new ECPoint[n];
    for (int k = 0; k < n; k++) {
      hPrime[k] = Secp256k1Operations.multiply(hVec[k], new BigInteger(1, yInvPowers[k]));
    }

    // Verify IPA
    return verifyIpa(gVec, hPrime, u, p, lVec, rVec, aFinal, bFinal, uxScalar, ipaTranscript, n);
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  private int ipaRounds(int n) {
    int r = 0;
    while (n > 1) {
      n >>= 1;
      r++;
    }
    return r;
  }

  private byte[][] computeScalarPowers(byte[] base, int n) {
    byte[][] powers = new byte[n][SCALAR_SIZE];
    byte[] current = Secp256k1Operations.scalarOne();
    for (int i = 0; i < n; i++) {
      powers[i] = current.clone();
      current = Secp256k1Operations.scalarMultiply(current, base);
    }
    return powers;
  }

  private byte[] computeDelta(byte[] y, byte[] z, byte[] zSq, byte[][] yPowers, int m) {
    byte[] delta = Secp256k1Operations.scalarZero();
    int n = VALUE_BITS * m;

    // sum_y_all = sum_{k=0}^{n-1} y^k
    byte[] sumYAll = Secp256k1Operations.scalarZero();
    for (int k = 0; k < n; k++) {
      sumYAll = Secp256k1Operations.scalarAdd(sumYAll, yPowers[k]);
    }

    // delta += (z - z^2) * sum_y_all
    byte[] zMinusZSq = Secp256k1Operations.scalarSub(z, zSq);
    byte[] term = Secp256k1Operations.scalarMultiply(zMinusZSq, sumYAll);
    delta = Secp256k1Operations.scalarAdd(delta, term);

    // Compute two_sum = sum_{i=0}^{63} 2^i
    byte[] twoSum = Secp256k1Operations.scalarZero();
    for (int i = 0; i < VALUE_BITS; i++) {
      byte[] twoI = computeTwoPower(i);
      twoSum = Secp256k1Operations.scalarAdd(twoSum, twoI);
    }

    // delta -= sum_{j=0}^{m-1} z^(j+3) * two_sum
    byte[] zPow = Secp256k1Operations.scalarMultiply(zSq, z); // z^3
    for (int j = 0; j < m; j++) {
      term = Secp256k1Operations.scalarMultiply(zPow, twoSum);
      delta = Secp256k1Operations.scalarSub(delta, term);
      zPow = Secp256k1Operations.scalarMultiply(zPow, z);
    }

    return delta;
  }

  private byte[] computeTwoPower(int i) {
    byte[] result = new byte[SCALAR_SIZE];
    result[SCALAR_SIZE - 1 - (i / 8)] = (byte) (1 << (i % 8));
    return result;
  }

  private ECPoint computeLhs(byte[] tHat, byte[] tauX, ECPoint pkBase) {
    ECPoint tHatG = Secp256k1Operations.multiplyG(new BigInteger(1, tHat));
    ECPoint tauXBase = Secp256k1Operations.multiply(pkBase, new BigInteger(1, tauX));
    return Secp256k1Operations.add(tHatG, tauXBase);
  }

  private ECPoint computeRhs(ECPoint[] commitments, byte[] z, byte[] delta, byte[] x,
      ECPoint t1, ECPoint t2, int m) {
    ECPoint result = null;

    // sum_j z^(j+2) * V_j
    byte[] zPow = Secp256k1Operations.scalarMultiply(z, z); // z^2
    for (int j = 0; j < m; j++) {
      if (!Secp256k1Operations.isScalarZero(zPow)) {
        ECPoint term = Secp256k1Operations.multiply(commitments[j], new BigInteger(1, zPow));
        result = (result == null) ? term : Secp256k1Operations.add(result, term);
      }
      zPow = Secp256k1Operations.scalarMultiply(zPow, z);
    }

    // + delta*G
    if (!Secp256k1Operations.isScalarZero(delta)) {
      ECPoint deltaG = Secp256k1Operations.multiplyG(new BigInteger(1, delta));
      result = (result == null) ? deltaG : Secp256k1Operations.add(result, deltaG);
    }

    // + x*T1
    if (!Secp256k1Operations.isScalarZero(x)) {
      ECPoint xT1 = Secp256k1Operations.multiply(t1, new BigInteger(1, x));
      result = (result == null) ? xT1 : Secp256k1Operations.add(result, xT1);
    }

    // + x^2*T2
    byte[] xSq = Secp256k1Operations.scalarMultiply(x, x);
    if (!Secp256k1Operations.isScalarZero(xSq)) {
      ECPoint xSqT2 = Secp256k1Operations.multiply(t2, new BigInteger(1, xSq));
      result = (result == null) ? xSqT2 : Secp256k1Operations.add(result, xSqT2);
    }

    return result;
  }

  private ECPoint buildP(ECPoint a, ECPoint s, byte[] x, byte[] z, ECPoint[] gVec, ECPoint[] hVec,
      byte[][] yPowers, byte[][] yInvPowers, int m, ECPoint u, byte[] uxScalar, byte[] tHat,
      byte[] mu, ECPoint pkBase) {
    int n = VALUE_BITS * m;

    // P = A
    ECPoint p = a;

    // P += x*S
    ECPoint xS = Secp256k1Operations.multiply(s, new BigInteger(1, x));
    p = Secp256k1Operations.add(p, xS);

    // P += sum_{k=0}^{n-1} [ (-z)*G_k + termH * (y^{-k}*H_k) ]
    byte[] negZ = Secp256k1Operations.scalarNegate(z);
    byte[] zSq = Secp256k1Operations.scalarMultiply(z, z);

    for (int j = 0; j < m; j++) {
      byte[] zJ2 = scalarPow(z, j + 2);

      for (int i = 0; i < VALUE_BITS; i++) {
        int k = j * VALUE_BITS + i;

        // (-z) * G_k
        if (!Secp256k1Operations.isScalarZero(negZ)) {
          ECPoint gi = Secp256k1Operations.multiply(gVec[k], new BigInteger(1, negZ));
          p = Secp256k1Operations.add(p, gi);
        }

        // termH = z*y^k + z^(j+2)*2^i
        byte[] twoI = computeTwoPower(i);
        byte[] termH = Secp256k1Operations.scalarMultiply(z, yPowers[k]);
        byte[] tmp = Secp256k1Operations.scalarMultiply(zJ2, twoI);
        termH = Secp256k1Operations.scalarAdd(termH, tmp);

        if (!Secp256k1Operations.isScalarZero(termH)) {
          // Hi = y^{-k} * H_k
          ECPoint hi = Secp256k1Operations.multiply(hVec[k], new BigInteger(1, yInvPowers[k]));
          // Hi = termH * Hi
          hi = Secp256k1Operations.multiply(hi, new BigInteger(1, termH));
          p = Secp256k1Operations.add(p, hi);
        }
      }
    }

    // P += (t_hat * ux) * U
    byte[] tHatUx = Secp256k1Operations.scalarMultiply(tHat, uxScalar);
    if (!Secp256k1Operations.isScalarZero(tHatUx)) {
      ECPoint q = Secp256k1Operations.multiply(u, new BigInteger(1, tHatUx));
      p = Secp256k1Operations.add(p, q);
    }

    // P -= mu*pkBase
    byte[] negMu = Secp256k1Operations.scalarNegate(mu);
    ECPoint muTerm = Secp256k1Operations.multiply(pkBase, new BigInteger(1, negMu));
    p = Secp256k1Operations.add(p, muTerm);

    return p;
  }

  private byte[] scalarPow(byte[] base, int exp) {
    byte[] result = Secp256k1Operations.scalarOne();
    for (int i = 0; i < exp; i++) {
      result = Secp256k1Operations.scalarMultiply(result, base);
    }
    return result;
  }

  private boolean verifyIpa(ECPoint[] gVec, ECPoint[] hPrime, ECPoint u, ECPoint p,
      ECPoint[] lVec, ECPoint[] rVec, byte[] aFinal, byte[] bFinal, byte[] uxScalar,
      byte[] ipaTranscript, int n) {

    int rounds = lVec.length;
    byte[] lastChallenge = ipaTranscript.clone();

    // Compute challenges for each round
    byte[][] challenges = new byte[rounds][];
    byte[][] challengeInvs = new byte[rounds][];
    for (int i = 0; i < rounds; i++) {
      challenges[i] = deriveIpaRoundChallenge(lastChallenge, lVec[i], rVec[i]);
      challengeInvs[i] = Secp256k1Operations.scalarInverse(challenges[i]);
      lastChallenge = challenges[i].clone();
    }

    // Compute final G and H
    ECPoint gFinal = computeFinalGenerator(gVec, challenges, challengeInvs, n, true);
    ECPoint hFinal = computeFinalGenerator(hPrime, challenges, challengeInvs, n, false);

    // Compute expected P
    // P' = P + sum_i (x_i^2 * L_i + x_i^{-2} * R_i)
    ECPoint pPrime = p;
    for (int i = 0; i < rounds; i++) {
      byte[] xSq = Secp256k1Operations.scalarMultiply(challenges[i], challenges[i]);
      byte[] xInvSq = Secp256k1Operations.scalarMultiply(challengeInvs[i], challengeInvs[i]);

      ECPoint lTerm = Secp256k1Operations.multiply(lVec[i], new BigInteger(1, xSq));
      ECPoint rTerm = Secp256k1Operations.multiply(rVec[i], new BigInteger(1, xInvSq));

      pPrime = Secp256k1Operations.add(pPrime, lTerm);
      pPrime = Secp256k1Operations.add(pPrime, rTerm);
    }

    // Expected: a*G_final + b*H_final + (a*b*ux)*U
    ECPoint expected = Secp256k1Operations.multiply(gFinal, new BigInteger(1, aFinal));
    ECPoint bH = Secp256k1Operations.multiply(hFinal, new BigInteger(1, bFinal));
    expected = Secp256k1Operations.add(expected, bH);

    byte[] ab = Secp256k1Operations.scalarMultiply(aFinal, bFinal);
    byte[] abUx = Secp256k1Operations.scalarMultiply(ab, uxScalar);
    if (!Secp256k1Operations.isScalarZero(abUx)) {
      ECPoint uTerm = Secp256k1Operations.multiply(u, new BigInteger(1, abUx));
      expected = Secp256k1Operations.add(expected, uTerm);
    }

    return pPrime.equals(expected);
  }

  private ECPoint computeFinalGenerator(ECPoint[] vec, byte[][] challenges, byte[][] challengeInvs,
      int n, boolean isG) {
    // For G: use x_inv for left, x for right
    // For H: use x for left, x_inv for right
    int rounds = challenges.length;

    // Compute scalar for each generator
    byte[][] scalars = new byte[n][SCALAR_SIZE];
    for (int i = 0; i < n; i++) {
      scalars[i] = Secp256k1Operations.scalarOne();
    }

    int halfN = n;
    for (int r = 0; r < rounds; r++) {
      halfN /= 2;
      byte[] xL = isG ? challengeInvs[r] : challenges[r];
      byte[] xR = isG ? challenges[r] : challengeInvs[r];

      for (int i = 0; i < n; i++) {
        int bit = (i >> (rounds - 1 - r)) & 1;
        byte[] factor = (bit == 0) ? xL : xR;
        scalars[i] = Secp256k1Operations.scalarMultiply(scalars[i], factor);
      }
    }

    // Compute multi-scalar multiplication
    ECPoint result = null;
    for (int i = 0; i < n; i++) {
      if (!Secp256k1Operations.isScalarZero(scalars[i])) {
        ECPoint term = Secp256k1Operations.multiply(vec[i], new BigInteger(1, scalars[i]));
        result = (result == null) ? term : Secp256k1Operations.add(result, term);
      }
    }
    return result;
  }

  // ============================================================================
  // Fiat-Shamir Challenge Derivation
  // ============================================================================

  private byte[] deriveYChallenge(byte[] contextId, byte[][] vCommitments, byte[] aSer, byte[] sSer) {
    int len = RANGE_DOMAIN.length() + (contextId != null ? 32 : 0) +
              vCommitments.length * COMPRESSED_POINT_SIZE + 2 * COMPRESSED_POINT_SIZE;
    byte[] input = new byte[len];
    int offset = 0;

    byte[] domain = RANGE_DOMAIN.getBytes();
    System.arraycopy(domain, 0, input, offset, domain.length);
    offset += domain.length;

    if (contextId != null) {
      System.arraycopy(contextId, 0, input, offset, 32);
      offset += 32;
    }

    for (byte[] c : vCommitments) {
      System.arraycopy(c, 0, input, offset, COMPRESSED_POINT_SIZE);
      offset += COMPRESSED_POINT_SIZE;
    }

    System.arraycopy(aSer, 0, input, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;
    System.arraycopy(sSer, 0, input, offset, COMPRESSED_POINT_SIZE);

    byte[] hash = HashingUtils.sha256(input).toByteArray();
    return Secp256k1Operations.reduceToScalar(hash);
  }

  private byte[] deriveZChallenge(byte[] contextId, byte[][] vCommitments, byte[] aSer, byte[] sSer, byte[] y) {
    int len = RANGE_DOMAIN.length() + (contextId != null ? 32 : 0) +
              vCommitments.length * COMPRESSED_POINT_SIZE + 2 * COMPRESSED_POINT_SIZE + SCALAR_SIZE;
    byte[] input = new byte[len];
    int offset = 0;

    byte[] domain = RANGE_DOMAIN.getBytes();
    System.arraycopy(domain, 0, input, offset, domain.length);
    offset += domain.length;

    if (contextId != null) {
      System.arraycopy(contextId, 0, input, offset, 32);
      offset += 32;
    }

    for (byte[] c : vCommitments) {
      System.arraycopy(c, 0, input, offset, COMPRESSED_POINT_SIZE);
      offset += COMPRESSED_POINT_SIZE;
    }

    System.arraycopy(aSer, 0, input, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;
    System.arraycopy(sSer, 0, input, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;
    System.arraycopy(y, 0, input, offset, SCALAR_SIZE);

    byte[] hash = HashingUtils.sha256(input).toByteArray();
    return Secp256k1Operations.reduceToScalar(hash);
  }

  private byte[] deriveXChallenge(byte[] contextId, byte[] aSer, byte[] sSer,
      byte[] y, byte[] z, byte[] t1Ser, byte[] t2Ser) {
    int len = (contextId != null ? 32 : 0) + 4 * COMPRESSED_POINT_SIZE + 2 * SCALAR_SIZE;
    byte[] input = new byte[len];
    int offset = 0;

    if (contextId != null) {
      System.arraycopy(contextId, 0, input, offset, 32);
      offset += 32;
    }

    System.arraycopy(aSer, 0, input, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;
    System.arraycopy(sSer, 0, input, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;
    System.arraycopy(y, 0, input, offset, SCALAR_SIZE);
    offset += SCALAR_SIZE;
    System.arraycopy(z, 0, input, offset, SCALAR_SIZE);
    offset += SCALAR_SIZE;
    System.arraycopy(t1Ser, 0, input, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;
    System.arraycopy(t2Ser, 0, input, offset, COMPRESSED_POINT_SIZE);

    byte[] hash = HashingUtils.sha256(input).toByteArray();
    return Secp256k1Operations.reduceToScalar(hash);
  }

  private byte[] buildIpaTranscript(byte[] contextId, byte[] aSer, byte[] sSer,
      byte[] t1Ser, byte[] t2Ser, byte[] y, byte[] z, byte[] x, byte[] tHat) {
    int len = (contextId != null ? 32 : 0) + 4 * COMPRESSED_POINT_SIZE + 4 * SCALAR_SIZE;
    byte[] input = new byte[len];
    int offset = 0;

    if (contextId != null) {
      System.arraycopy(contextId, 0, input, offset, 32);
      offset += 32;
    }

    System.arraycopy(aSer, 0, input, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;
    System.arraycopy(sSer, 0, input, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;
    System.arraycopy(t1Ser, 0, input, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;
    System.arraycopy(t2Ser, 0, input, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;
    System.arraycopy(y, 0, input, offset, SCALAR_SIZE);
    offset += SCALAR_SIZE;
    System.arraycopy(z, 0, input, offset, SCALAR_SIZE);
    offset += SCALAR_SIZE;
    System.arraycopy(x, 0, input, offset, SCALAR_SIZE);
    offset += SCALAR_SIZE;
    System.arraycopy(tHat, 0, input, offset, SCALAR_SIZE);

    return HashingUtils.sha256(input).toByteArray();
  }

  private byte[] deriveIpaBindingChallenge(byte[] transcript, byte[] tHat) {
    byte[] input = new byte[64];
    System.arraycopy(transcript, 0, input, 0, 32);
    System.arraycopy(tHat, 0, input, 32, 32);
    byte[] hash = HashingUtils.sha256(input).toByteArray();
    return Secp256k1Operations.reduceToScalar(hash);
  }

  private byte[] deriveIpaRoundChallenge(byte[] lastChallenge, ECPoint l, ECPoint r) {
    byte[] lSer = Secp256k1Operations.serializeCompressed(l);
    byte[] rSer = Secp256k1Operations.serializeCompressed(r);
    byte[] input = new byte[32 + 2 * COMPRESSED_POINT_SIZE];
    System.arraycopy(lastChallenge, 0, input, 0, 32);
    System.arraycopy(lSer, 0, input, 32, COMPRESSED_POINT_SIZE);
    System.arraycopy(rSer, 0, input, 32 + COMPRESSED_POINT_SIZE, COMPRESSED_POINT_SIZE);
    byte[] hash = HashingUtils.sha256(input).toByteArray();
    return Secp256k1Operations.reduceToScalar(hash);
  }
}

