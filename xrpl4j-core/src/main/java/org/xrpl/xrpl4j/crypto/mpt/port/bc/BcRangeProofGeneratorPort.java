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

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.SecureRandomBlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.mpt.port.RangeProofGeneratorPort;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * BouncyCastle implementation of {@link RangeProofGeneratorPort}.
 *
 * <p>Port of {@code secp256k1_bulletproof_prove_agg} from bulletproof_aggregated.c.</p>
 */
public class BcRangeProofGeneratorPort implements RangeProofGeneratorPort {

  private static final int SCALAR_SIZE = 32;
  private static final int COMPRESSED_POINT_SIZE = 33;
  private static final String RANGE_DOMAIN = "MPT_BULLETPROOF_RANGE";

  private final BlindingFactorGenerator blindingFactorGenerator;

  /**
   * Constructs a new generator using {@link SecureRandomBlindingFactorGenerator}.
   */
  public BcRangeProofGeneratorPort() {
    this(new SecureRandomBlindingFactorGenerator());
  }

  /**
   * Constructs a new generator with the given blinding factor generator.
   *
   * @param blindingFactorGenerator The generator for random scalars.
   */
  public BcRangeProofGeneratorPort(BlindingFactorGenerator blindingFactorGenerator) {
    this.blindingFactorGenerator = Objects.requireNonNull(blindingFactorGenerator);
  }

  @Override
  public UnsignedByteArray generateProof(
    UnsignedLong[] values,
    UnsignedByteArray blindingsFlat,
    UnsignedByteArray pkBase,
    UnsignedByteArray contextId
  ) {
    Objects.requireNonNull(values, "values must not be null");
    Objects.requireNonNull(blindingsFlat, "blindingsFlat must not be null");
    Objects.requireNonNull(pkBase, "pkBase must not be null");

    final int m = values.length;

    // Convert UnsignedLong[] to long[]
    long[] valuesLong = new long[m];
    for (int i = 0; i < m; i++) {
      valuesLong[i] = values[i].longValue();
    }
    if (m == 0 || (m & (m - 1)) != 0) {
      throw new IllegalArgumentException("m must be a power of 2 (1 or 2)");
    }
    if (blindingsFlat.length() != m * SCALAR_SIZE) {
      throw new IllegalArgumentException("blindingsFlat must be " + (m * SCALAR_SIZE) + " bytes");
    }

    final int n = VALUE_BITS * m;
    final int rounds = ipaRounds(n);
    final int proofSize = 292 + 66 * rounds;

    // Parse pk_base
    ECPoint pkBasePoint = Secp256k1Operations.deserialize(pkBase.toByteArray());

    // Extract blindings
    byte[][] blindings = new byte[m][SCALAR_SIZE];
    for (int j = 0; j < m; j++) {
      System.arraycopy(blindingsFlat.toByteArray(), j * SCALAR_SIZE, blindings[j], 0, SCALAR_SIZE);
    }

    // Generate proof
    byte[] proof = generateProofInternal(valuesLong, blindings, pkBasePoint,
      contextId != null ? contextId.toByteArray() : null, n, rounds);

    // Clear sensitive data
    for (byte[] b : blindings) {
      Arrays.fill(b, (byte) 0);
    }

    return UnsignedByteArray.of(proof);
  }

  private byte[] generateProofInternal(
    long[] values,
    byte[][] blindings,
    ECPoint pkBase,
    byte[] contextId,
    int n,
    int rounds
  ) {
    final int m = values.length;

    // Generator vectors
    ECPoint[] gVec = Secp256k1Operations.getGeneratorVector("G", n);
    ECPoint[] hVec = Secp256k1Operations.getGeneratorVector("H", n);
    ECPoint u = Secp256k1Operations.getU();

    // Bit decomposition vectors
    byte[][] al = new byte[n][SCALAR_SIZE];
    byte[][] ar = new byte[n][SCALAR_SIZE];
    byte[][] sl = new byte[n][SCALAR_SIZE];
    byte[][] sr = new byte[n][SCALAR_SIZE];

    byte[] one = Secp256k1Operations.scalarOne();
    byte[] minusOne = Secp256k1Operations.scalarNegate(one);
    byte[] zero = Secp256k1Operations.scalarZero();

    // Bit decomposition for each value
    for (int j = 0; j < m; j++) {
      long v = values[j];
      for (int i = 0; i < VALUE_BITS; i++) {
        int k = j * VALUE_BITS + i;
        if (((v >> i) & 1) == 1) {
          al[k] = one.clone();
          ar[k] = zero.clone();
        } else {
          al[k] = zero.clone();
          ar[k] = minusOne.clone();
        }
        sl[k] = generateRandomScalar();
        sr[k] = generateRandomScalar();
        // Validate random scalars (matching C: if (!generate_random_scalar(...)) goto cleanup)
        if (!Secp256k1Operations.isValidScalar(sl[k]) || !Secp256k1Operations.isValidScalar(sr[k])) {
          throw new IllegalStateException("Failed to generate valid random scalar for sl/sr");
        }
      }
    }

    // Random blinding factors
    byte[] alpha = generateRandomScalar();
    byte[] rho = generateRandomScalar();
    // Validate random scalars (matching C: if (!generate_random_scalar(...)) goto cleanup)
    if (!Secp256k1Operations.isValidScalar(alpha) || !Secp256k1Operations.isValidScalar(rho)) {
      throw new IllegalStateException("Failed to generate valid random scalar for alpha/rho");
    }

    // Compute A = alpha*pkBase + <al,G> + <ar,H>
    ECPoint commitA = calculateCommitmentTerm(pkBase, alpha, al, ar, gVec, hVec);
    // Compute S = rho*pkBase + <sl,G> + <sr,H>
    ECPoint commitS = calculateCommitmentTerm(pkBase, rho, sl, sr, gVec, hVec);

    // Serialize A and S
    byte[] aSer = Secp256k1Operations.serializeCompressed(commitA);
    byte[] sSer = Secp256k1Operations.serializeCompressed(commitS);

    // Reconstruct value commitments for transcript
    byte[][] vCommitmentsSer = new byte[m][];
    for (int i = 0; i < m; i++) {
      ECPoint vCommit = createCommitment(values[i], blindings[i], pkBase);
      vCommitmentsSer[i] = Secp256k1Operations.serializeCompressed(vCommit);
    }

    // Fiat-Shamir: derive y and z
    byte[] y = deriveYChallenge(contextId, vCommitmentsSer, aSer, sSer);
    byte[] z = deriveZChallenge(contextId, vCommitmentsSer, aSer, sSer, y);
    byte[] zNeg = Secp256k1Operations.scalarNegate(z);

    // Compute y powers: y^0, y^1, ..., y^{n-1}
    byte[][] yPowers = computeScalarPowers(y, n);

    // Compute z^(j+2) for j = 0..m-1
    byte[][] zJ2 = computeZPowersJ2(z, m);

    // Compute l0, r0, r1 vectors
    byte[][] lVec = new byte[n][SCALAR_SIZE];
    byte[][] rVec = new byte[n][SCALAR_SIZE];
    byte[][] r1Vec = new byte[n][SCALAR_SIZE];

    for (int block = 0; block < m; block++) {
      byte[] zBlk = zJ2[block];
      for (int i = 0; i < VALUE_BITS; i++) {
        int k = block * VALUE_BITS + i;

        // 2^i as scalar
        byte[] twoI = computeTwoPower(i);

        // l0 = aL - z
        lVec[k] = Secp256k1Operations.scalarAdd(al[k], zNeg);

        // r0 = y^k * (aR + z) + z^(block+2) * 2^i
        byte[] tmp1 = Secp256k1Operations.scalarAdd(ar[k], z);
        byte[] r0 = Secp256k1Operations.scalarMultiply(tmp1, yPowers[k]);
        byte[] tmp2 = Secp256k1Operations.scalarMultiply(zBlk, twoI);
        rVec[k] = Secp256k1Operations.scalarAdd(r0, tmp2);

        // r1 = sR * y^k
        r1Vec[k] = Secp256k1Operations.scalarMultiply(sr[k], yPowers[k]);
      }
    }

    // t1 = <l0, r1> + <sl, r0>
    byte[] dot1 = ipaDot(lVec, r1Vec);
    byte[] dot2 = ipaDot(sl, rVec);
    byte[] t1 = Secp256k1Operations.scalarAdd(dot1, dot2);

    // t2 = <sl, r1>
    byte[] t2 = ipaDot(sl, r1Vec);

    // Random tau1, tau2
    byte[] tau1 = generateRandomScalar();
    byte[] tau2 = generateRandomScalar();
    // Validate random scalars (matching C: if (!generate_random_scalar(...)) goto cleanup)
    if (!Secp256k1Operations.isValidScalar(tau1) || !Secp256k1Operations.isValidScalar(tau2)) {
      throw new IllegalStateException("Failed to generate valid random scalar for tau1/tau2");
    }

    // Check t1 is not zero (matching C: if (memcmp(t1, zero, 32) == 0) goto cleanup)
    if (Secp256k1Operations.isScalarZero(t1)) {
      throw new IllegalStateException("t1 polynomial coefficient is zero");
    }

    // T1 = t1*G + tau1*pkBase
    ECPoint t1G = Secp256k1Operations.multiplyG(new BigInteger(1, t1));
    ECPoint tau1Base = Secp256k1Operations.multiply(pkBase, new BigInteger(1, tau1));
    ECPoint commitT1 = Secp256k1Operations.add(t1G, tau1Base);

    // Check t2 is not zero (matching C: if (memcmp(t2, zero, 32) == 0) goto cleanup)
    if (Secp256k1Operations.isScalarZero(t2)) {
      throw new IllegalStateException("t2 polynomial coefficient is zero");
    }

    // T2 = t2*G + tau2*pkBase
    ECPoint t2G = Secp256k1Operations.multiplyG(new BigInteger(1, t2));
    ECPoint tau2Base = Secp256k1Operations.multiply(pkBase, new BigInteger(1, tau2));
    ECPoint commitT2 = Secp256k1Operations.add(t2G, tau2Base);

    // Fiat-Shamir: derive x
    byte[] t1Ser = Secp256k1Operations.serializeCompressed(commitT1);
    byte[] t2Ser = Secp256k1Operations.serializeCompressed(commitT2);
    byte[] x = deriveXChallenge(contextId, aSer, sSer, y, z, t1Ser, t2Ser);

    // Check x is not zero (matching C: if (memcmp(x, zero, 32) == 0) goto cleanup)
    if (Secp256k1Operations.isScalarZero(x)) {
      throw new IllegalStateException("Challenge x is zero");
    }

    // Evaluate l(x), r(x)
    for (int k = 0; k < n; k++) {
      // l = l0 + sL*x
      byte[] tmp = Secp256k1Operations.scalarMultiply(sl[k], x);
      lVec[k] = Secp256k1Operations.scalarAdd(lVec[k], tmp);

      // r = r0 + r1*x
      tmp = Secp256k1Operations.scalarMultiply(r1Vec[k], x);
      rVec[k] = Secp256k1Operations.scalarAdd(rVec[k], tmp);
    }

    // t_hat = <l, r>
    byte[] tHat = ipaDot(lVec, rVec);

    // tau_x = tau2*x^2 + tau1*x + sum_j z^(j+2) * blinding_j
    byte[] xSq = Secp256k1Operations.scalarMultiply(x, x);
    byte[] tauX = Secp256k1Operations.scalarMultiply(tau2, xSq);
    byte[] tmp = Secp256k1Operations.scalarMultiply(tau1, x);
    tauX = Secp256k1Operations.scalarAdd(tauX, tmp);
    for (int j = 0; j < m; j++) {
      byte[] add = Secp256k1Operations.scalarMultiply(zJ2[j], blindings[j]);
      tauX = Secp256k1Operations.scalarAdd(tauX, add);
    }

    // mu = alpha + rho*x
    tmp = Secp256k1Operations.scalarMultiply(rho, x);
    byte[] mu = Secp256k1Operations.scalarAdd(alpha, tmp);

    // Build IPA transcript
    byte[] ipaTranscript = buildIpaTranscript(contextId, aSer, sSer, t1Ser, t2Ser, y, z, x, tHat);

    // Derive ux scalar
    byte[] uxScalar = deriveIpaBindingChallenge(ipaTranscript, tHat);

    // Normalize H: H'[k] = H[k] * y^{-k}
    byte[] yInv = Secp256k1Operations.scalarInverse(y);
    ECPoint[] hPrime = new ECPoint[n];
    byte[] yInvPow = one.clone();
    for (int k = 0; k < n; k++) {
      hPrime[k] = Secp256k1Operations.multiply(hVec[k], new BigInteger(1, yInvPow));
      yInvPow = Secp256k1Operations.scalarMultiply(yInvPow, yInv);
    }

    // Run IPA prover
    ECPoint[] lOut = new ECPoint[rounds];
    ECPoint[] rOut = new ECPoint[rounds];
    byte[] aFinal = new byte[SCALAR_SIZE];
    byte[] bFinal = new byte[SCALAR_SIZE];

    runIpaProver(u, gVec, hPrime, lVec, rVec, n, ipaTranscript, uxScalar,
      lOut, rOut, rounds, aFinal, bFinal);

    // Serialize proof
    byte[] proof = serializeProof(commitA, commitS, commitT1, commitT2, lOut, rOut,
      aFinal, bFinal, tHat, tauX, mu, rounds);

    // Clear sensitive data (matching C code cleanup)
    // Bit decomposition and blinding vectors
    clearArrays(al, ar, sl, sr, lVec, rVec, r1Vec);

    // Random blinding factors
    Arrays.fill(alpha, (byte) 0);
    Arrays.fill(rho, (byte) 0);
    Arrays.fill(tau1, (byte) 0);
    Arrays.fill(tau2, (byte) 0);

    // Polynomial coefficients
    Arrays.fill(t1, (byte) 0);
    Arrays.fill(t2, (byte) 0);

    // Proof scalars
    Arrays.fill(tHat, (byte) 0);
    Arrays.fill(tauX, (byte) 0);
    Arrays.fill(mu, (byte) 0);

    // Fiat-Shamir challenges
    Arrays.fill(y, (byte) 0);
    Arrays.fill(z, (byte) 0);
    Arrays.fill(x, (byte) 0);
    Arrays.fill(zNeg, (byte) 0);
    Arrays.fill(xSq, (byte) 0);

    // IPA related
    Arrays.fill(uxScalar, (byte) 0);
    Arrays.fill(ipaTranscript, (byte) 0);
    Arrays.fill(yInv, (byte) 0);
    Arrays.fill(yInvPow, (byte) 0);

    // Power vectors
    clearArrays(yPowers, zJ2);

    return proof;
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  private byte[] generateRandomScalar() {
    return blindingFactorGenerator.generate().toBytes();
  }

  private int ipaRounds(int n) {
    int r = 0;
    while (n > 1) {
      n >>= 1;
      r++;
    }
    return r;
  }

  private ECPoint createCommitment(long value, byte[] blinding, ECPoint pkBase) {
    ECPoint rH = Secp256k1Operations.multiply(pkBase, new BigInteger(1, blinding));
    if (value == 0) {
      return rH;
    }
    ECPoint vG = Secp256k1Operations.multiplyG(BigInteger.valueOf(value));
    return Secp256k1Operations.add(vG, rH);
  }

  private ECPoint calculateCommitmentTerm(ECPoint pkBase, byte[] baseScalar,
      byte[][] vecL, byte[][] vecR, ECPoint[] gVec, ECPoint[] hVec) {
    int n = vecL.length;

    // base_scalar * pkBase
    ECPoint result = Secp256k1Operations.multiply(pkBase, new BigInteger(1, baseScalar));

    // + <vecL, G>
    ECPoint msm1 = multiScalarMul(gVec, vecL);
    if (msm1 != null) {
      result = Secp256k1Operations.add(result, msm1);
    }

    // + <vecR, H>
    ECPoint msm2 = multiScalarMul(hVec, vecR);
    if (msm2 != null) {
      result = Secp256k1Operations.add(result, msm2);
    }

    return result;
  }

  private ECPoint multiScalarMul(ECPoint[] points, byte[][] scalars) {
    ECPoint result = null;

    for (int i = 0; i < points.length; i++) {
      if (Secp256k1Operations.isScalarZero(scalars[i])) {
        continue;
      }
      ECPoint term = Secp256k1Operations.multiply(points[i], new BigInteger(1, scalars[i]));
      if (result == null) {
        result = term;
      } else {
        result = Secp256k1Operations.add(result, term);
      }
    }
    return result;
  }

  private byte[] ipaDot(byte[][] a, byte[][] b) {
    byte[] acc = Secp256k1Operations.scalarZero();
    for (int i = 0; i < a.length; i++) {
      byte[] term = Secp256k1Operations.scalarMultiply(a[i], b[i]);
      acc = Secp256k1Operations.scalarAdd(acc, term);
    }
    return acc;
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

  private byte[][] computeZPowersJ2(byte[] z, int m) {
    byte[][] result = new byte[m][SCALAR_SIZE];
    byte[] zPow = Secp256k1Operations.scalarMultiply(z, z); // z^2
    for (int j = 0; j < m; j++) {
      result[j] = zPow.clone();
      zPow = Secp256k1Operations.scalarMultiply(zPow, z);
    }
    return result;
  }

  private byte[] computeTwoPower(int i) {
    byte[] result = new byte[SCALAR_SIZE];
    result[SCALAR_SIZE - 1 - (i / 8)] = (byte) (1 << (i % 8));
    return result;
  }

  private void clearArrays(byte[][]... arrays) {
    for (byte[][] arr : arrays) {
      for (byte[] b : arr) {
        Arrays.fill(b, (byte) 0);
      }
    }
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

  // ============================================================================
  // IPA Prover
  // ============================================================================

  private void runIpaProver(ECPoint u, ECPoint[] gVec, ECPoint[] hVec,
      byte[][] aVec, byte[][] bVec, int n, byte[] ipaTranscript, byte[] uxScalar,
      ECPoint[] lOut, ECPoint[] rOut, int maxRounds, byte[] aFinal, byte[] bFinal) {

    int curN = n;
    byte[] lastChallenge = ipaTranscript.clone();
    byte[] uScalar = null;
    byte[] uInv = null;

    // Make mutable copies
    ECPoint[] g = gVec.clone();
    ECPoint[] h = hVec.clone();
    byte[][] a = new byte[n][];
    byte[][] b = new byte[n][];
    for (int i = 0; i < n; i++) {
      a[i] = aVec[i].clone();
      b[i] = bVec[i].clone();
    }

    try {
      for (int round = 0; round < maxRounds; round++) {
        int halfN = curN / 2;

        // Compute L and R
        ECPoint lr = computeIpaLR(g, h, a, b, halfN, u, uxScalar, true);
        ECPoint rr = computeIpaLR(g, h, a, b, halfN, u, uxScalar, false);

        // Validate L and R are not null (matching C: if (!secp256k1_bulletproof_ipa_compute_LR(...)) goto cleanup)
        if (lr == null || rr == null) {
          throw new IllegalStateException("Failed to compute IPA L/R commitment at round " + round);
        }

        lOut[round] = lr;
        rOut[round] = rr;

        // Derive challenge
        uScalar = deriveIpaRoundChallenge(lastChallenge, lr, rr);

        // Validate challenge scalar (matching C: if (!secp256k1_ec_seckey_verify(ctx, u_inv)) goto cleanup)
        if (!Secp256k1Operations.isValidScalar(uScalar)) {
          throw new IllegalStateException("Invalid IPA round challenge at round " + round);
        }

        uInv = Secp256k1Operations.scalarInverse(uScalar);

        // Validate inverse (matching C: if (!secp256k1_ec_seckey_verify(ctx, u_inv)) goto cleanup)
        if (!Secp256k1Operations.isValidScalar(uInv)) {
          throw new IllegalStateException("Invalid IPA round challenge inverse at round " + round);
        }

        lastChallenge = uScalar.clone();

        // Fold vectors
        foldIpaVectors(g, h, a, b, halfN, uScalar, uInv);

        // Clear round-specific scalars
        Arrays.fill(uScalar, (byte) 0);
        Arrays.fill(uInv, (byte) 0);

        curN = halfN;
      }

      System.arraycopy(a[0], 0, aFinal, 0, SCALAR_SIZE);
      System.arraycopy(b[0], 0, bFinal, 0, SCALAR_SIZE);
    } finally {
      // Clear sensitive data (matching C code cleanup)
      if (uScalar != null) Arrays.fill(uScalar, (byte) 0);
      if (uInv != null) Arrays.fill(uInv, (byte) 0);
      Arrays.fill(lastChallenge, (byte) 0);

      // Clear mutable copies of a and b vectors
      for (int i = 0; i < n; i++) {
        if (a[i] != null) Arrays.fill(a[i], (byte) 0);
        if (b[i] != null) Arrays.fill(b[i], (byte) 0);
      }
    }
  }

  private ECPoint computeIpaLR(ECPoint[] g, ECPoint[] h, byte[][] a, byte[][] b,
      int halfN, ECPoint u, byte[] ux, boolean isL) {

    // For L: aL = a[0..halfN-1], aR = a[halfN..n-1]
    // For R: swap
    int aLStart = isL ? 0 : halfN;
    int aRStart = isL ? halfN : 0;
    int gLStart = isL ? halfN : 0;
    int gRStart = isL ? 0 : halfN;
    int hLStart = isL ? 0 : halfN;
    int hRStart = isL ? halfN : 0;
    int bLStart = isL ? halfN : 0;
    int bRStart = isL ? 0 : halfN;

    ECPoint result = null;

    // <aL, gR>
    for (int i = 0; i < halfN; i++) {
      if (!Secp256k1Operations.isScalarZero(a[aLStart + i])) {
        ECPoint term = Secp256k1Operations.multiply(g[gLStart + i], new BigInteger(1, a[aLStart + i]));
        result = (result == null) ? term : Secp256k1Operations.add(result, term);
      }
    }

    // <bR, hL>
    for (int i = 0; i < halfN; i++) {
      if (!Secp256k1Operations.isScalarZero(b[bRStart + i])) {
        ECPoint term = Secp256k1Operations.multiply(h[hLStart + i], new BigInteger(1, b[bRStart + i]));
        result = (result == null) ? term : Secp256k1Operations.add(result, term);
      }
    }

    // c * ux * U
    byte[] c = ipaDotRange(a, aLStart, b, bRStart, halfN);
    byte[] cUx = Secp256k1Operations.scalarMultiply(c, ux);
    if (!Secp256k1Operations.isScalarZero(cUx)) {
      ECPoint term = Secp256k1Operations.multiply(u, new BigInteger(1, cUx));
      result = (result == null) ? term : Secp256k1Operations.add(result, term);
    }

    return result;
  }

  private byte[] ipaDotRange(byte[][] a, int aStart, byte[][] b, int bStart, int len) {
    byte[] acc = Secp256k1Operations.scalarZero();
    for (int i = 0; i < len; i++) {
      byte[] term = Secp256k1Operations.scalarMultiply(a[aStart + i], b[bStart + i]);
      acc = Secp256k1Operations.scalarAdd(acc, term);
    }
    return acc;
  }

  private void foldIpaVectors(ECPoint[] g, ECPoint[] h, byte[][] a, byte[][] b,
      int halfN, byte[] x, byte[] xInv) {

    for (int i = 0; i < halfN; i++) {
      // a'[i] = aL*x + aR*x_inv
      byte[] t1 = Secp256k1Operations.scalarMultiply(a[i], x);
      byte[] t2 = Secp256k1Operations.scalarMultiply(a[i + halfN], xInv);
      a[i] = Secp256k1Operations.scalarAdd(t1, t2);

      // b'[i] = bL*x_inv + bR*x
      t1 = Secp256k1Operations.scalarMultiply(b[i], xInv);
      t2 = Secp256k1Operations.scalarMultiply(b[i + halfN], x);
      b[i] = Secp256k1Operations.scalarAdd(t1, t2);

      // G'[i] = GL*x_inv + GR*x
      ECPoint gL = Secp256k1Operations.multiply(g[i], new BigInteger(1, xInv));
      ECPoint gR = Secp256k1Operations.multiply(g[i + halfN], new BigInteger(1, x));
      g[i] = Secp256k1Operations.add(gL, gR);

      // H'[i] = HL*x + HR*x_inv
      ECPoint hL = Secp256k1Operations.multiply(h[i], new BigInteger(1, x));
      ECPoint hR = Secp256k1Operations.multiply(h[i + halfN], new BigInteger(1, xInv));
      h[i] = Secp256k1Operations.add(hL, hR);
    }
  }

  // ============================================================================
  // Proof Serialization
  // ============================================================================

  private byte[] serializeProof(ECPoint a, ECPoint s, ECPoint t1, ECPoint t2,
      ECPoint[] lVec, ECPoint[] rVec, byte[] aFinal, byte[] bFinal,
      byte[] tHat, byte[] tauX, byte[] mu, int rounds) {

    int proofSize = 4 * COMPRESSED_POINT_SIZE + 2 * rounds * COMPRESSED_POINT_SIZE + 5 * SCALAR_SIZE;
    byte[] proof = new byte[proofSize];
    int offset = 0;

    // A, S, T1, T2
    offset = appendPoint(proof, offset, a);
    offset = appendPoint(proof, offset, s);
    offset = appendPoint(proof, offset, t1);
    offset = appendPoint(proof, offset, t2);

    // L_vec
    for (int i = 0; i < rounds; i++) {
      offset = appendPoint(proof, offset, lVec[i]);
    }

    // R_vec
    for (int i = 0; i < rounds; i++) {
      offset = appendPoint(proof, offset, rVec[i]);
    }

    // a_final, b_final, t_hat, tau_x, mu
    System.arraycopy(aFinal, 0, proof, offset, SCALAR_SIZE);
    offset += SCALAR_SIZE;
    System.arraycopy(bFinal, 0, proof, offset, SCALAR_SIZE);
    offset += SCALAR_SIZE;
    System.arraycopy(tHat, 0, proof, offset, SCALAR_SIZE);
    offset += SCALAR_SIZE;
    System.arraycopy(tauX, 0, proof, offset, SCALAR_SIZE);
    offset += SCALAR_SIZE;
    System.arraycopy(mu, 0, proof, offset, SCALAR_SIZE);

    return proof;
  }

  private int appendPoint(byte[] buffer, int offset, ECPoint point) {
    byte[] ser = Secp256k1Operations.serializeCompressed(point);
    System.arraycopy(ser, 0, buffer, offset, COMPRESSED_POINT_SIZE);
    return offset + COMPRESSED_POINT_SIZE;
  }
}

