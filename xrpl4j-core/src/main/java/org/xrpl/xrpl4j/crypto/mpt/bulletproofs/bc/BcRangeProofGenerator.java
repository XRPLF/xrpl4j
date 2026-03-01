package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc;

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
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.RangeProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.context.LinkProofContext;
import org.xrpl.xrpl4j.crypto.mpt.tmp.BulletproofRangeProof;
import org.xrpl.xrpl4j.crypto.mpt.tmp.PedersenCommitment;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Java implementation of {@link RangeProofGenerator}.
 *
 * <p>This implementation generates Bulletproof range proofs compatible with
 * rippled's secp256k1_bulletproof_prove_agg function.</p>
 *
 * <p>Implements the Bulletproofs protocol (Bünz et al., 2018) for proving
 * that committed values lie within [0, 2^64) without revealing the values.</p>
 *
 * @see RangeProofGenerator
 */
public class BcRangeProofGenerator implements RangeProofGenerator {

  private static final int BP_VALUE_BITS = 64;
  private static final String NUMS_DOMAIN_SEPARATOR = "MPT_BULLETPROOF_V1_NUMS";
  private static final String CURVE_LABEL = "secp256k1";
  private static final String RANGE_DOMAIN = "MPT_BULLETPROOF_RANGE";
  private static final int SCALAR_SIZE = 32;
  private static final int COMPRESSED_POINT_SIZE = 33;

  private final SecureRandom secureRandom;

  // Cached generators
  private ECPoint cachedH;
  private ECPoint cachedU;

  /**
   * Constructs a new BcRangeProofGenerator.
   */
  public BcRangeProofGenerator() {
    this.secureRandom = new SecureRandom();
  }

  @Override
  public BulletproofRangeProof generateProof(
    final List<UnsignedLong> values,
    final List<BlindingFactor> blindingFactors,
    final LinkProofContext context
  ) {
    Objects.requireNonNull(values, "values must not be null");
    Objects.requireNonNull(blindingFactors, "blindingFactors must not be null");
    Objects.requireNonNull(context, "context must not be null");

    if (values.isEmpty()) {
      throw new IllegalArgumentException("values must not be empty");
    }
    if (values.size() != blindingFactors.size()) {
      throw new IllegalArgumentException("values and blindingFactors must have the same size");
    }

    // Convert to arrays for internal method
    long[] valuesArray = new long[values.size()];
    byte[][] blindingsArray = new byte[blindingFactors.size()][];

    for (int i = 0; i < values.size(); i++) {
      Objects.requireNonNull(values.get(i), "values[" + i + "] must not be null");
      Objects.requireNonNull(blindingFactors.get(i), "blindingFactors[" + i + "] must not be null");
      valuesArray[i] = values.get(i).longValue();
      blindingsArray[i] = blindingFactors.get(i).toBytes();
    }

    byte[] proof = generateBulletproofInternal(valuesArray, blindingsArray, context.toBytes());
    return BulletproofRangeProof.fromBytes(proof);
  }

  @Override
  public boolean verify(
    final BulletproofRangeProof proof,
    final List<PedersenCommitment> commitments,
    final LinkProofContext context
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(commitments, "commitments must not be null");
    Objects.requireNonNull(context, "context must not be null");

    if (commitments.isEmpty()) {
      throw new IllegalArgumentException("commitments must not be empty");
    }

    byte[][] commitmentsArray = new byte[commitments.size()][];
    for (int i = 0; i < commitments.size(); i++) {
      Objects.requireNonNull(commitments.get(i), "commitments[" + i + "] must not be null");
      commitmentsArray[i] = commitments.get(i).toCompressedBytes();
    }

    return verifyBulletproofInternal(proof.toBytes(), commitmentsArray, context.toBytes());
  }

  /**
   * Internal method to generate a bulletproof.
   * Implements the full Bulletproof protocol.
   */
  private byte[] generateBulletproofInternal(
    final long[] values,
    final byte[][] blindings,
    final byte[] contextId
  ) {
    final int m = values.length;
    final int n = BP_VALUE_BITS * m;
    final int rounds = ipaRounds(n);

    // Validate m is power of 2
    if (m == 0 || (m & (m - 1)) != 0) {
      throw new IllegalArgumentException("Number of values must be a power of 2");
    }

    // Get H generator (pk_base)
    ECPoint pkBase = getHGenerator();

    // Generate generator vectors G and H
    ECPoint[] gVec = generateGeneratorVector("G", n);
    ECPoint[] hVec = generateGeneratorVector("H", n);
    ECPoint u = getUGenerator();

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
      for (int i = 0; i < BP_VALUE_BITS; i++) {
        int k = j * BP_VALUE_BITS + i;
        if (((v >> i) & 1) == 1) {
          al[k] = one.clone();
          ar[k] = zero.clone();
        } else {
          al[k] = zero.clone();
          ar[k] = minusOne.clone();
        }
        sl[k] = generateRandomScalar();
        sr[k] = generateRandomScalar();
      }
    }

    // Random blinding factors
    byte[] alpha = generateRandomScalar();
    byte[] rho = generateRandomScalar();

    // Compute A = alpha*pkBase + <al,G> + <ar,H>
    ECPoint commitA = calculateCommitmentTerm(pkBase, alpha, al, ar, gVec, hVec);
    // Compute S = rho*pkBase + <sl,G> + <sr,H>
    ECPoint commitS = calculateCommitmentTerm(pkBase, rho, sl, sr, gVec, hVec);

    // Fiat-Shamir: derive y and z
    byte[] aSer = Secp256k1Operations.serializeCompressed(commitA);
    byte[] sSer = Secp256k1Operations.serializeCompressed(commitS);

    // Reconstruct value commitments for transcript
    byte[][] vCommitmentsSer = new byte[m][];
    for (int i = 0; i < m; i++) {
      ECPoint vCommit = createCommitment(values[i], blindings[i], pkBase);
      vCommitmentsSer[i] = Secp256k1Operations.serializeCompressed(vCommit);
    }

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
      for (int i = 0; i < BP_VALUE_BITS; i++) {
        int k = block * BP_VALUE_BITS + i;

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

    // T1 = t1*G + tau1*pkBase
    ECPoint t1G = Secp256k1Operations.multiplyG(new BigInteger(1, t1));
    ECPoint tau1Base = Secp256k1Operations.multiply(pkBase, new BigInteger(1, tau1));
    ECPoint commitT1 = Secp256k1Operations.add(t1G, tau1Base);

    // T2 = t2*G + tau2*pkBase
    ECPoint t2G = Secp256k1Operations.multiplyG(new BigInteger(1, t2));
    ECPoint tau2Base = Secp256k1Operations.multiply(pkBase, new BigInteger(1, tau2));
    ECPoint commitT2 = Secp256k1Operations.add(t2G, tau2Base);

    // Fiat-Shamir: derive x
    byte[] t1Ser = Secp256k1Operations.serializeCompressed(commitT1);
    byte[] t2Ser = Secp256k1Operations.serializeCompressed(commitT2);
    byte[] x = deriveXChallenge(contextId, aSer, sSer, y, z, t1Ser, t2Ser);

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
    return serializeProof(commitA, commitS, commitT1, commitT2, lOut, rOut,
      aFinal, bFinal, tHat, tauX, mu, rounds);
  }

  /**
   * Internal method to verify a bulletproof.
   */
  private boolean verifyBulletproofInternal(
    final byte[] proof,
    final byte[][] commitments,
    final byte[] contextId
  ) {
    // For now, we trust the prover - verification can be added later
    // The rippled server will verify the proof
    throw new UnsupportedOperationException(
      "Bulletproof verification not yet implemented in Java. " +
      "The proof will be verified by rippled."
    );
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

  private byte[] generateRandomScalar() {
    byte[] scalar = new byte[SCALAR_SIZE];
    do {
      secureRandom.nextBytes(scalar);
    } while (!Secp256k1Operations.isValidScalar(scalar));
    return scalar;
  }

  private ECPoint getHGenerator() {
    if (cachedH == null) {
      cachedH = hashToPointNums("H".getBytes(StandardCharsets.UTF_8), 0);
    }
    return cachedH;
  }

  private ECPoint getUGenerator() {
    if (cachedU == null) {
      cachedU = hashToPointNums("BP_U".getBytes(StandardCharsets.UTF_8), 0);
    }
    return cachedU;
  }

  private ECPoint[] generateGeneratorVector(String label, int n) {
    ECPoint[] vec = new ECPoint[n];
    byte[] labelBytes = label.getBytes(StandardCharsets.UTF_8);
    for (int i = 0; i < n; i++) {
      vec[i] = hashToPointNums(labelBytes, i);
    }
    return vec;
  }

  private ECPoint hashToPointNums(byte[] label, int index) {
    byte[] domainBytes = NUMS_DOMAIN_SEPARATOR.getBytes(StandardCharsets.UTF_8);
    byte[] curveBytes = CURVE_LABEL.getBytes(StandardCharsets.UTF_8);
    byte[] indexBe = intToBigEndian(index);

    for (int ctr = 0; ctr < Integer.MAX_VALUE; ctr++) {
      byte[] ctrBe = intToBigEndian(ctr);

      // Build hash input
      byte[] hashInput = new byte[domainBytes.length + curveBytes.length +
        label.length + 4 + 4];
      int offset = 0;
      System.arraycopy(domainBytes, 0, hashInput, offset, domainBytes.length);
      offset += domainBytes.length;
      System.arraycopy(curveBytes, 0, hashInput, offset, curveBytes.length);
      offset += curveBytes.length;
      System.arraycopy(label, 0, hashInput, offset, label.length);
      offset += label.length;
      System.arraycopy(indexBe, 0, hashInput, offset, 4);
      offset += 4;
      System.arraycopy(ctrBe, 0, hashInput, offset, 4);

      byte[] hash = HashingUtils.sha256(hashInput).toByteArray();

      // Construct compressed point: 0x02 || hash
      byte[] compressed = new byte[COMPRESSED_POINT_SIZE];
      compressed[0] = 0x02;
      System.arraycopy(hash, 0, compressed, 1, SCALAR_SIZE);

      try {
        ECPoint point = Secp256k1Operations.deserialize(compressed);
        if (point != null && !point.isInfinity()) {
          return point;
        }
      } catch (Exception e) {
        // Invalid point, continue
      }
    }
    throw new IllegalStateException("Failed to derive NUMS point");
  }

  private byte[] intToBigEndian(int value) {
    return new byte[]{
      (byte) (value >> 24),
      (byte) (value >> 16),
      (byte) (value >> 8),
      (byte) value
    };
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
    byte[] zero = Secp256k1Operations.scalarZero();

    for (int i = 0; i < points.length; i++) {
      if (Arrays.equals(scalars[i], zero)) {
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
    byte[][] zJ2 = new byte[m][SCALAR_SIZE];
    for (int j = 0; j < m; j++) {
      zJ2[j] = scalarPow(z, j + 2);
    }
    return zJ2;
  }

  private byte[] scalarPow(byte[] base, int exp) {
    byte[] result = Secp256k1Operations.scalarOne();
    for (int i = 0; i < exp; i++) {
      result = Secp256k1Operations.scalarMultiply(result, base);
    }
    return result;
  }

  private byte[] computeTwoPower(int i) {
    byte[] result = new byte[SCALAR_SIZE];
    // 2^i in big-endian
    int bytePos = SCALAR_SIZE - 1 - (i / 8);
    int bitPos = i % 8;
    result[bytePos] = (byte) (1 << bitPos);
    return result;
  }

  private byte[] deriveYChallenge(byte[] contextId, byte[][] vCommitments,
      byte[] aSer, byte[] sSer) {
    byte[] domain = RANGE_DOMAIN.getBytes(StandardCharsets.UTF_8);
    int totalLen = domain.length + (contextId != null ? SCALAR_SIZE : 0) +
      vCommitments.length * COMPRESSED_POINT_SIZE + 2 * COMPRESSED_POINT_SIZE;
    byte[] input = new byte[totalLen];
    int offset = 0;

    System.arraycopy(domain, 0, input, offset, domain.length);
    offset += domain.length;
    if (contextId != null) {
      System.arraycopy(contextId, 0, input, offset, SCALAR_SIZE);
      offset += SCALAR_SIZE;
    }
    for (byte[] vSer : vCommitments) {
      System.arraycopy(vSer, 0, input, offset, COMPRESSED_POINT_SIZE);
      offset += COMPRESSED_POINT_SIZE;
    }
    System.arraycopy(aSer, 0, input, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;
    System.arraycopy(sSer, 0, input, offset, COMPRESSED_POINT_SIZE);

    byte[] hash = HashingUtils.sha256(input).toByteArray();
    return reduceToScalar(hash);
  }

  private byte[] deriveZChallenge(byte[] contextId, byte[][] vCommitments,
      byte[] aSer, byte[] sSer, byte[] y) {
    byte[] domain = RANGE_DOMAIN.getBytes(StandardCharsets.UTF_8);
    int totalLen = domain.length + (contextId != null ? SCALAR_SIZE : 0) +
      vCommitments.length * COMPRESSED_POINT_SIZE + 2 * COMPRESSED_POINT_SIZE + SCALAR_SIZE;
    byte[] input = new byte[totalLen];
    int offset = 0;

    System.arraycopy(domain, 0, input, offset, domain.length);
    offset += domain.length;
    if (contextId != null) {
      System.arraycopy(contextId, 0, input, offset, SCALAR_SIZE);
      offset += SCALAR_SIZE;
    }
    for (byte[] vSer : vCommitments) {
      System.arraycopy(vSer, 0, input, offset, COMPRESSED_POINT_SIZE);
      offset += COMPRESSED_POINT_SIZE;
    }
    System.arraycopy(aSer, 0, input, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;
    System.arraycopy(sSer, 0, input, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;
    System.arraycopy(y, 0, input, offset, SCALAR_SIZE);

    byte[] hash = HashingUtils.sha256(input).toByteArray();
    return reduceToScalar(hash);
  }

  private byte[] deriveXChallenge(byte[] contextId, byte[] aSer, byte[] sSer,
      byte[] y, byte[] z, byte[] t1Ser, byte[] t2Ser) {
    int totalLen = (contextId != null ? SCALAR_SIZE : 0) +
      4 * COMPRESSED_POINT_SIZE + 2 * SCALAR_SIZE;
    byte[] input = new byte[totalLen];
    int offset = 0;

    if (contextId != null) {
      System.arraycopy(contextId, 0, input, offset, SCALAR_SIZE);
      offset += SCALAR_SIZE;
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
    return reduceToScalar(hash);
  }

  private byte[] buildIpaTranscript(byte[] contextId, byte[] aSer, byte[] sSer,
      byte[] t1Ser, byte[] t2Ser, byte[] y, byte[] z, byte[] x, byte[] tHat) {
    int totalLen = (contextId != null ? SCALAR_SIZE : 0) +
      4 * COMPRESSED_POINT_SIZE + 4 * SCALAR_SIZE;
    byte[] input = new byte[totalLen];
    int offset = 0;

    if (contextId != null) {
      System.arraycopy(contextId, 0, input, offset, SCALAR_SIZE);
      offset += SCALAR_SIZE;
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

  private byte[] deriveIpaBindingChallenge(byte[] transcript, byte[] dot) {
    byte[] input = new byte[64];
    System.arraycopy(transcript, 0, input, 0, SCALAR_SIZE);
    System.arraycopy(dot, 0, input, SCALAR_SIZE, SCALAR_SIZE);
    byte[] hash = HashingUtils.sha256(input).toByteArray();
    return reduceToScalar(hash);
  }

  private byte[] deriveIpaRoundChallenge(byte[] lastChallenge, ECPoint l, ECPoint r) {
    byte[] lSer = Secp256k1Operations.serializeCompressed(l);
    byte[] rSer = Secp256k1Operations.serializeCompressed(r);

    byte[] input = new byte[SCALAR_SIZE + 2 * COMPRESSED_POINT_SIZE];
    System.arraycopy(lastChallenge, 0, input, 0, SCALAR_SIZE);
    System.arraycopy(lSer, 0, input, SCALAR_SIZE, COMPRESSED_POINT_SIZE);
    System.arraycopy(rSer, 0, input, SCALAR_SIZE + COMPRESSED_POINT_SIZE, COMPRESSED_POINT_SIZE);

    byte[] hash = HashingUtils.sha256(input).toByteArray();
    return reduceToScalar(hash);
  }

  private byte[] reduceToScalar(byte[] hash) {
    BigInteger hashInt = new BigInteger(1, hash);
    BigInteger reduced = hashInt.mod(Secp256k1Operations.getCurveOrder());
    return Secp256k1Operations.toBytes32(reduced);
  }

  private void runIpaProver(ECPoint u, ECPoint[] gVec, ECPoint[] hVec,
      byte[][] aVec, byte[][] bVec, int n, byte[] ipaTranscript, byte[] uxScalar,
      ECPoint[] lOut, ECPoint[] rOut, int maxRounds, byte[] aFinal, byte[] bFinal) {

    int curN = n;
    byte[] lastChallenge = ipaTranscript.clone();

    // Make mutable copies
    ECPoint[] g = gVec.clone();
    ECPoint[] h = hVec.clone();
    byte[][] a = new byte[n][];
    byte[][] b = new byte[n][];
    for (int i = 0; i < n; i++) {
      a[i] = aVec[i].clone();
      b[i] = bVec[i].clone();
    }

    for (int r = 0; r < maxRounds; r++) {
      int halfN = curN / 2;

      // Compute L and R
      ECPoint lr = computeLR(u, uxScalar, a, b, g, h, halfN, true);
      ECPoint rr = computeLR(u, uxScalar, a, b, g, h, halfN, false);

      lOut[r] = lr;
      rOut[r] = rr;

      // Derive round challenge
      byte[] uScalar = deriveIpaRoundChallenge(lastChallenge, lr, rr);
      byte[] uInv = Secp256k1Operations.scalarInverse(uScalar);

      lastChallenge = uScalar.clone();

      // Fold vectors
      foldVectors(a, b, g, h, halfN, uScalar, uInv);

      curN = halfN;
    }

    System.arraycopy(a[0], 0, aFinal, 0, SCALAR_SIZE);
    System.arraycopy(b[0], 0, bFinal, 0, SCALAR_SIZE);
  }

  private ECPoint computeLR(ECPoint u, byte[] ux, byte[][] a, byte[][] b,
      ECPoint[] g, ECPoint[] h, int halfN, boolean isL) {

    // For L: aL with gR, bR with hL
    // For R: aR with gL, bL with hR
    byte[][] aSlice = isL ? Arrays.copyOfRange(a, 0, halfN) : Arrays.copyOfRange(a, halfN, 2 * halfN);
    byte[][] bSlice = isL ? Arrays.copyOfRange(b, halfN, 2 * halfN) : Arrays.copyOfRange(b, 0, halfN);
    ECPoint[] gSlice = isL ? Arrays.copyOfRange(g, halfN, 2 * halfN) : Arrays.copyOfRange(g, 0, halfN);
    ECPoint[] hSlice = isL ? Arrays.copyOfRange(h, 0, halfN) : Arrays.copyOfRange(h, halfN, 2 * halfN);

    // c = <aSlice, bSlice>
    byte[] c = ipaDot(aSlice, bSlice);

    // result = <aSlice, gSlice> + <bSlice, hSlice> + c*ux*U
    ECPoint result = null;

    ECPoint msm1 = multiScalarMul(gSlice, aSlice);
    if (msm1 != null) {
      result = msm1;
    }

    ECPoint msm2 = multiScalarMul(hSlice, bSlice);
    if (msm2 != null) {
      result = result == null ? msm2 : Secp256k1Operations.add(result, msm2);
    }

    byte[] cux = Secp256k1Operations.scalarMultiply(c, ux);
    if (!Arrays.equals(cux, Secp256k1Operations.scalarZero())) {
      ECPoint uTerm = Secp256k1Operations.multiply(u, new BigInteger(1, cux));
      result = result == null ? uTerm : Secp256k1Operations.add(result, uTerm);
    }

    return result;
  }

  private void foldVectors(byte[][] a, byte[][] b, ECPoint[] g, ECPoint[] h,
      int halfN, byte[] x, byte[] xInv) {

    for (int i = 0; i < halfN; i++) {
      // a'[i] = aL*x + aR*xInv
      byte[] t1 = Secp256k1Operations.scalarMultiply(a[i], x);
      byte[] t2 = Secp256k1Operations.scalarMultiply(a[i + halfN], xInv);
      a[i] = Secp256k1Operations.scalarAdd(t1, t2);

      // b'[i] = bL*xInv + bR*x
      t1 = Secp256k1Operations.scalarMultiply(b[i], xInv);
      t2 = Secp256k1Operations.scalarMultiply(b[i + halfN], x);
      b[i] = Secp256k1Operations.scalarAdd(t1, t2);

      // G'[i] = GL*xInv + GR*x
      ECPoint gL = Secp256k1Operations.multiply(g[i], new BigInteger(1, xInv));
      ECPoint gR = Secp256k1Operations.multiply(g[i + halfN], new BigInteger(1, x));
      g[i] = Secp256k1Operations.add(gL, gR);

      // H'[i] = HL*x + HR*xInv
      ECPoint hL = Secp256k1Operations.multiply(h[i], new BigInteger(1, x));
      ECPoint hR = Secp256k1Operations.multiply(h[i + halfN], new BigInteger(1, xInv));
      h[i] = Secp256k1Operations.add(hL, hR);
    }
  }

  private byte[] serializeProof(ECPoint a, ECPoint s, ECPoint t1, ECPoint t2,
      ECPoint[] lVec, ECPoint[] rVec, byte[] aFinal, byte[] bFinal,
      byte[] tHat, byte[] tauX, byte[] mu, int rounds) {

    int proofSize = 4 * COMPRESSED_POINT_SIZE + 2 * rounds * COMPRESSED_POINT_SIZE + 5 * SCALAR_SIZE;
    byte[] proof = new byte[proofSize];
    int offset = 0;

    // A, S, T1, T2
    byte[] aSer = Secp256k1Operations.serializeCompressed(a);
    System.arraycopy(aSer, 0, proof, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;

    byte[] sSer = Secp256k1Operations.serializeCompressed(s);
    System.arraycopy(sSer, 0, proof, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;

    byte[] t1Ser = Secp256k1Operations.serializeCompressed(t1);
    System.arraycopy(t1Ser, 0, proof, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;

    byte[] t2Ser = Secp256k1Operations.serializeCompressed(t2);
    System.arraycopy(t2Ser, 0, proof, offset, COMPRESSED_POINT_SIZE);
    offset += COMPRESSED_POINT_SIZE;

    // L_vec
    for (int i = 0; i < rounds; i++) {
      byte[] lSer = Secp256k1Operations.serializeCompressed(lVec[i]);
      System.arraycopy(lSer, 0, proof, offset, COMPRESSED_POINT_SIZE);
      offset += COMPRESSED_POINT_SIZE;
    }

    // R_vec
    for (int i = 0; i < rounds; i++) {
      byte[] rSer = Secp256k1Operations.serializeCompressed(rVec[i]);
      System.arraycopy(rSer, 0, proof, offset, COMPRESSED_POINT_SIZE);
      offset += COMPRESSED_POINT_SIZE;
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
}

