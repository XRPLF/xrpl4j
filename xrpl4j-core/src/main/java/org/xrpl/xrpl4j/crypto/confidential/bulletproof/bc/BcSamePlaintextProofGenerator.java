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
 * <p>Port of {@code secp256k1_mpt_prove_same_plaintext_multi} from proof_same_plaintext_multi.c.</p>
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
    final List<UnsignedByteArray> R,
    final List<UnsignedByteArray> S,
    final List<UnsignedByteArray> Pk,
    final List<UnsignedByteArray> rArray,
    final UnsignedByteArray contextId
  ) {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(R, "R must not be null");
    Objects.requireNonNull(S, "S must not be null");
    Objects.requireNonNull(Pk, "Pk must not be null");
    Objects.requireNonNull(rArray, "rArray must not be null");

    int n = R.size();
    Preconditions.checkArgument(n >= 2, "Must have at least 2 participants, but had %s", n);
    Preconditions.checkArgument(S.size() == n, "S size must match R size");
    Preconditions.checkArgument(Pk.size() == n, "Pk size must match R size");
    Preconditions.checkArgument(rArray.size() == n, "rArray size must match R size");

    // Validate input blinding factors (matches C's implicit validation via r_array usage)
    for (int i = 0; i < n; i++) {
      BigInteger ri = new BigInteger(1, rArray.get(i).toByteArray());
      if (!Secp256k1Operations.isValidPrivateKey(ri)) {
        throw new IllegalArgumentException("rArray[" + i + "] is not a valid scalar");
      }
    }

    // Intermediate values to clear
    byte[] km = null;
    byte[][] krFlat = new byte[n][];
    byte[] mScalar = null;
    byte[] sm = null;
    byte[] e = null;

    try {
      // 1. Generate Randomness & Commitments

      // km -> Tm = km * G
      // if (!secp256k1_ec_pubkey_create(ctx, &Tm, k_m)) goto cleanup;
      BlindingFactor nonceKm = blindingFactorGenerator.generate();
      km = nonceKm.toBytes();
      BigInteger kmInt = new BigInteger(1, km);
      ECPoint Tm = Secp256k1Operations.multiplyG(kmInt);
      if (Tm.isInfinity()) {
        throw new IllegalStateException("Tm is point at infinity");
      }
      byte[] TmBytes = Secp256k1Operations.serializeCompressed(Tm);

      List<UnsignedByteArray> TrGList = new ArrayList<>(n);
      List<UnsignedByteArray> TrPList = new ArrayList<>(n);

      for (int i = 0; i < n; i++) {
        // kri -> TrG = kri * G
        // if (!secp256k1_ec_pubkey_create(ctx, &TrG[i], kri)) goto cleanup;
        BlindingFactor nonceKri = blindingFactorGenerator.generate();
        krFlat[i] = nonceKri.toBytes();
        BigInteger kriInt = new BigInteger(1, krFlat[i]);
        ECPoint TrGi = Secp256k1Operations.multiplyG(kriInt);
        if (TrGi.isInfinity()) {
          throw new IllegalStateException("TrG[" + i + "] is point at infinity");
        }
        TrGList.add(UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(TrGi)));

        // TrP = kri * Pk_i
        // if (!secp256k1_ec_pubkey_tweak_mul(ctx, &TrP[i], kri)) goto cleanup;
        ECPoint PkPoint = Secp256k1Operations.deserialize(Pk.get(i).toByteArray());
        ECPoint TrPi = Secp256k1Operations.multiply(PkPoint, kriInt);
        if (TrPi.isInfinity()) {
          throw new IllegalStateException("TrP[" + i + "] is point at infinity");
        }
        TrPList.add(UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(TrPi)));
      }

      // 2. Compute Challenge
      UnsignedByteArray eUba = ChallengeUtils.buildSamePlaintextChallenge(
        R, S, Pk, UnsignedByteArray.of(TmBytes), TrGList, TrPList, contextId
      );
      e = eUba.toByteArray();
      BigInteger eInt = new BigInteger(1, e);

      // 3. Compute Responses

      // s_m = k_m + e * m (mod n)
      // if (!secp256k1_ec_seckey_tweak_mul(ctx, m_scalar, e)) goto cleanup;
      // if (!secp256k1_ec_seckey_tweak_add(ctx, s_m, m_scalar)) goto cleanup;
      mScalar = Secp256k1Operations.unsignedLongToScalar(amount);
      BigInteger mInt = new BigInteger(1, mScalar);
      BigInteger smInt = kmInt.add(eInt.multiply(mInt)).mod(Secp256k1Operations.getCurveOrder());
      if (!Secp256k1Operations.isValidPrivateKey(smInt)) {
        throw new IllegalStateException("s_m is not a valid scalar");
      }
      sm = Secp256k1Operations.toBytes32(smInt);

      // Serialize proof: Tm || TrG[0..N-1] || TrP[0..N-1] || sm || sr[0..N-1]
      int proofSize = SamePlaintextProofGenerator.proofSize(n);
      byte[] proof = new byte[proofSize];
      int offset = 0;

      // Tm
      System.arraycopy(TmBytes, 0, proof, offset, 33);
      offset += 33;

      // TrG[0..N-1]
      for (int i = 0; i < n; i++) {
        byte[] trgBytes = TrGList.get(i).toByteArray();
        System.arraycopy(trgBytes, 0, proof, offset, 33);
        offset += 33;
      }

      // TrP[0..N-1]
      for (int i = 0; i < n; i++) {
        byte[] trpBytes = TrPList.get(i).toByteArray();
        System.arraycopy(trpBytes, 0, proof, offset, 33);
        offset += 33;
      }

      // sm
      System.arraycopy(sm, 0, proof, offset, 32);
      offset += 32;

      // sr[0..N-1]: s_ri = k_ri + e * r_i (mod n)
      // if (!secp256k1_ec_seckey_tweak_mul(ctx, term, e)) goto cleanup;
      // if (!secp256k1_ec_seckey_tweak_add(ctx, s_ri, term)) goto cleanup;
      for (int i = 0; i < n; i++) {
        BigInteger kriInt = new BigInteger(1, krFlat[i]);
        BigInteger riInt = new BigInteger(1, rArray.get(i).toByteArray());
        BigInteger sriInt = kriInt.add(eInt.multiply(riInt)).mod(Secp256k1Operations.getCurveOrder());
        if (!Secp256k1Operations.isValidPrivateKey(sriInt)) {
          throw new IllegalStateException("s_r[" + i + "] is not a valid scalar");
        }
        byte[] sri = Secp256k1Operations.toBytes32(sriInt);
        System.arraycopy(sri, 0, proof, offset, 32);
        offset += 32;
        Arrays.fill(sri, (byte) 0);
      }

      return UnsignedByteArray.of(proof);

    } finally {
      // OPENSSL_cleanse - clear all intermediate values
      if (km != null) {
        Arrays.fill(km, (byte) 0);
      }
      for (int i = 0; i < n; i++) {
        if (krFlat[i] != null) {
          Arrays.fill(krFlat[i], (byte) 0);
        }
      }
      if (mScalar != null) {
        Arrays.fill(mScalar, (byte) 0);
      }
      if (sm != null) {
        Arrays.fill(sm, (byte) 0);
      }
      if (e != null) {
        Arrays.fill(e, (byte) 0);
      }
    }
  }
}

