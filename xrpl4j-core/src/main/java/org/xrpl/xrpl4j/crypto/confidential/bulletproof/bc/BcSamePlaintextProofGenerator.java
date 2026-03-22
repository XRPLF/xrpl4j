package org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc;

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

import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.SecureRandomBlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SamePlaintextProofGenerator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * BouncyCastle implementation of {@link SamePlaintextProofGenerator}.
 *
 * <p>Port of {@code secp256k1_mpt_prove_equality_shared_r} from proof_same_plaintext_multi_shared_r.c.</p>
 */
@SuppressWarnings("checkstyle")
public class BcSamePlaintextProofGenerator implements SamePlaintextProofGenerator {

  private final BlindingFactorGenerator blindingFactorGenerator;

  /**
   * Constructs a new instance using {@link SecureRandomBlindingFactorGenerator}.
   */
  public BcSamePlaintextProofGenerator() {
    this(new SecureRandomBlindingFactorGenerator());
  }

  /**
   * Constructs a new instance with the specified blinding factor generator.
   *
   * @param blindingFactorGenerator The generator for random nonces.
   */
  public BcSamePlaintextProofGenerator(final BlindingFactorGenerator blindingFactorGenerator) {
    this.blindingFactorGenerator = Objects.requireNonNull(
      blindingFactorGenerator, "blindingFactorGenerator must not be null"
    );
  }

  @Override
  public UnsignedByteArray generateProof(
    final UnsignedLong amount,
    final UnsignedByteArray sharedR,
    final UnsignedByteArray c1,
    final List<UnsignedByteArray> c2List,
    final List<UnsignedByteArray> pkList,
    final UnsignedByteArray contextId
  ) {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(sharedR, "sharedR must not be null");
    Objects.requireNonNull(c1, "c1 must not be null");
    Objects.requireNonNull(c2List, "c2List must not be null");
    Objects.requireNonNull(pkList, "pkList must not be null");

    int n = c2List.size();
    Preconditions.checkArgument(n >= 2, "Must have at least 2 participants, but had %s", n);
    Preconditions.checkArgument(pkList.size() == n, "pkList size must match c2List size");

    // Validate shared blinding factor
    BigInteger rInt = new BigInteger(1, sharedR.toByteArray());
    if (!Secp256k1Operations.isValidPrivateKey(rInt)) {
      throw new IllegalArgumentException("sharedR is not a valid scalar");
    }

    // Intermediate values to clear
    byte[] km = null;
    byte[] kr = null;
    byte[] mScalar = null;
    byte[] sm = null;
    byte[] sr = null;
    byte[] e = null;

    try {
      // 1. Generate nonces: km (for amount), kr (for shared randomness)
      BlindingFactor nonceKm = blindingFactorGenerator.generate();
      km = nonceKm.toBytes();
      BigInteger kmInt = new BigInteger(1, km);

      BlindingFactor nonceKr = blindingFactorGenerator.generate();
      kr = nonceKr.toBytes();
      BigInteger krInt = new BigInteger(1, kr);

      // 2. Compute Tr = kr * G
      ECPoint Tr = Secp256k1Operations.multiplyG(krInt);
      if (Tr.isInfinity()) {
        throw new IllegalStateException("Tr is point at infinity");
      }
      byte[] TrBytes = Secp256k1Operations.serializeCompressed(Tr);

      // 3. Precompute kmG = km * G
      ECPoint kmG = Secp256k1Operations.multiplyG(kmInt);
      if (kmG.isInfinity()) {
        throw new IllegalStateException("kmG is point at infinity");
      }

      // 4. For each i: Tm_i = kmG + kr * Pk_i
      List<UnsignedByteArray> TmList = new ArrayList<>(n);
      for (int i = 0; i < n; i++) {
        ECPoint PkPoint = Secp256k1Operations.deserialize(pkList.get(i).toByteArray());
        ECPoint krPki = Secp256k1Operations.multiply(PkPoint, krInt);
        ECPoint Tmi = Secp256k1Operations.add(kmG, krPki);
        if (Tmi.isInfinity()) {
          throw new IllegalStateException("Tm[" + i + "] is point at infinity");
        }
        TmList.add(UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(Tmi)));
      }

      // 5. Compute challenge e
      UnsignedByteArray eUba = ChallengeUtils.buildEqualitySharedRChallenge(
        c1, c2List, pkList, UnsignedByteArray.of(TrBytes), TmList, contextId
      );
      e = eUba.toByteArray();
      BigInteger eInt = new BigInteger(1, e);

      // 6. Compute responses: sm = km + e * m (mod n), sr = kr + e * r (mod n)
      mScalar = Secp256k1Operations.unsignedLongToScalar(amount);
      BigInteger mInt = new BigInteger(1, mScalar);
      BigInteger smInt = kmInt.add(eInt.multiply(mInt)).mod(Secp256k1Operations.getCurveOrder());
      if (!Secp256k1Operations.isValidPrivateKey(smInt)) {
        throw new IllegalStateException("s_m is not a valid scalar");
      }
      sm = Secp256k1Operations.toBytes32(smInt);

      BigInteger srInt = krInt.add(eInt.multiply(rInt)).mod(Secp256k1Operations.getCurveOrder());
      if (!Secp256k1Operations.isValidPrivateKey(srInt)) {
        throw new IllegalStateException("s_r is not a valid scalar");
      }
      sr = Secp256k1Operations.toBytes32(srInt);

      // 7. Serialize: Tr (33 bytes) || Tm[0..N-1] (N*33 bytes) || sm (32 bytes) || sr (32 bytes)
      int proofSize = SamePlaintextProofGenerator.proofSize(n);
      byte[] proof = new byte[proofSize];
      int offset = 0;

      // Tr
      System.arraycopy(TrBytes, 0, proof, offset, 33);
      offset += 33;

      // Tm[0..N-1]
      for (int i = 0; i < n; i++) {
        byte[] tmBytes = TmList.get(i).toByteArray();
        System.arraycopy(tmBytes, 0, proof, offset, 33);
        offset += 33;
      }

      // sm
      System.arraycopy(sm, 0, proof, offset, 32);
      offset += 32;

      // sr
      System.arraycopy(sr, 0, proof, offset, 32);

      return UnsignedByteArray.of(proof);

    } finally {
      // OPENSSL_cleanse - clear all intermediate values
      if (km != null) {
        Arrays.fill(km, (byte) 0);
      }
      if (kr != null) {
        Arrays.fill(kr, (byte) 0);
      }
      if (mScalar != null) {
        Arrays.fill(mScalar, (byte) 0);
      }
      if (sm != null) {
        Arrays.fill(sm, (byte) 0);
      }
      if (sr != null) {
        Arrays.fill(sr, (byte) 0);
      }
      if (e != null) {
        Arrays.fill(e, (byte) 0);
      }
    }
  }
}
