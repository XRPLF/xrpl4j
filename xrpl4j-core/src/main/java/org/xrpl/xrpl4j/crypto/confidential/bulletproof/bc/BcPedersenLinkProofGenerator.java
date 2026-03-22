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

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.SecureRandomBlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.PedersenLinkProofGenerator;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * BouncyCastle implementation of {@link PedersenLinkProofGenerator}.
 *
 * <p>Port of {@code secp256k1_elgamal_pedersen_link_prove} from proof_link.c.</p>
 */
@SuppressWarnings("checkstyle")
public class BcPedersenLinkProofGenerator implements PedersenLinkProofGenerator {

  private final BlindingFactorGenerator blindingFactorGenerator;

  /**
   * Creates a generator with the default random blinding factor generator.
   */
  public BcPedersenLinkProofGenerator() {
    this(new SecureRandomBlindingFactorGenerator());
  }

  /**
   * Creates a generator with a custom blinding factor generator (for testing).
   *
   * @param blindingFactorGenerator The generator to use for nonces.
   */
  public BcPedersenLinkProofGenerator(BlindingFactorGenerator blindingFactorGenerator) {
    this.blindingFactorGenerator = blindingFactorGenerator;
  }

  @Override
  public UnsignedByteArray generateProof(
    UnsignedByteArray c1,
    UnsignedByteArray c2,
    UnsignedByteArray pk,
    UnsignedByteArray pcm,
    UnsignedLong amount,
    UnsignedByteArray r,
    UnsignedByteArray rho,
    UnsignedByteArray contextId
  ) {
    // 0. Validate Witnesses - matches C: if (!secp256k1_ec_seckey_verify(ctx, r)) return 0;
    if (!Secp256k1Operations.isValidScalar(r.toByteArray())) {
      throw new IllegalArgumentException("r is not a valid scalar");
    }
    if (!Secp256k1Operations.isValidScalar(rho.toByteArray())) {
      throw new IllegalArgumentException("rho is not a valid scalar");
    }

    byte[] km = null, kr = null, krho = null;
    byte[] mScalar = new byte[32];
    byte[] sm = null, sr = null, srho = null;

    try {
      // 1. Generate Nonces
      km = blindingFactorGenerator.generate().value().toByteArray();
      kr = blindingFactorGenerator.generate().value().toByteArray();
      krho = blindingFactorGenerator.generate().value().toByteArray();

      BigInteger kmInt = new BigInteger(1, km);
      BigInteger krInt = new BigInteger(1, kr);
      BigInteger krhoInt = new BigInteger(1, krho);

      // Parse input points
      ECPoint pkPoint = Secp256k1Operations.deserialize(pk.toByteArray());
      ECPoint H = Secp256k1Operations.getH();

      // 2. Compute Commitments

      // T1 = kr * G
      ECPoint T1 = Secp256k1Operations.multiplyG(krInt);
      if (T1.isInfinity()) {
        throw new IllegalStateException("T1 is point at infinity");
      }

      // T2 = km * G + kr * Pk
      ECPoint kmG = Secp256k1Operations.multiplyG(kmInt);
      ECPoint krPk = Secp256k1Operations.multiply(pkPoint, krInt);
      ECPoint T2 = Secp256k1Operations.add(kmG, krPk);
      if (T2.isInfinity()) {
        throw new IllegalStateException("T2 is point at infinity");
      }

      // T3 = km * G + krho * H
      ECPoint krhoH = Secp256k1Operations.multiply(H, krhoInt);
      ECPoint T3 = Secp256k1Operations.add(kmG, krhoH);
      if (T3.isInfinity()) {
        throw new IllegalStateException("T3 is point at infinity");
      }

      // Serialize commitment points
      UnsignedByteArray T1Bytes = UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(T1));
      UnsignedByteArray T2Bytes = UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(T2));
      UnsignedByteArray T3Bytes = UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(T3));

      // 3. Challenge
      UnsignedByteArray e = ChallengeUtils.buildPedersenLinkChallenge(
        c1, c2, pk, pcm, T1Bytes, T2Bytes, T3Bytes, contextId
      );
      BigInteger eInt = new BigInteger(1, e.toByteArray());

      // 4. Responses
      // Convert amount to scalar
      long amountValue = amount.longValue();
      for (int i = 0; i < 8; i++) {
        mScalar[31 - i] = (byte) ((amountValue >> (i * 8)) & 0xFF);
      }
      BigInteger mInt = new BigInteger(1, mScalar);
      BigInteger rInt = new BigInteger(1, r.toByteArray());
      BigInteger rhoInt = new BigInteger(1, rho.toByteArray());

      BigInteger n = Secp256k1Operations.getCurveOrder();

      // sm = km + e * m (mod n)
      BigInteger smInt = kmInt.add(eInt.multiply(mInt)).mod(n);
      sm = Secp256k1Operations.toBytes32(smInt);
      if (!Secp256k1Operations.isValidScalar(sm)) {
        throw new IllegalStateException("sm is not a valid scalar");
      }

      // sr = kr + e * r (mod n)
      BigInteger srInt = krInt.add(eInt.multiply(rInt)).mod(n);
      sr = Secp256k1Operations.toBytes32(srInt);
      if (!Secp256k1Operations.isValidScalar(sr)) {
        throw new IllegalStateException("sr is not a valid scalar");
      }

      // srho = krho + e * rho (mod n)
      BigInteger srhoInt = krhoInt.add(eInt.multiply(rhoInt)).mod(n);
      srho = Secp256k1Operations.toBytes32(srhoInt);
      if (!Secp256k1Operations.isValidScalar(srho)) {
        throw new IllegalStateException("srho is not a valid scalar");
      }

      // 5. Serialize Proof (195 bytes)
      byte[] proof = new byte[PROOF_SIZE];
      int offset = 0;
      System.arraycopy(T1Bytes.toByteArray(), 0, proof, offset, 33);
      offset += 33;
      System.arraycopy(T2Bytes.toByteArray(), 0, proof, offset, 33);
      offset += 33;
      System.arraycopy(T3Bytes.toByteArray(), 0, proof, offset, 33);
      offset += 33;
      System.arraycopy(sm, 0, proof, offset, 32);
      offset += 32;
      System.arraycopy(sr, 0, proof, offset, 32);
      offset += 32;
      System.arraycopy(srho, 0, proof, offset, 32);

      return UnsignedByteArray.of(proof);

    } finally {
      // Securely clear secrets - matches C: OPENSSL_cleanse
      if (km != null) Arrays.fill(km, (byte) 0);
      if (kr != null) Arrays.fill(kr, (byte) 0);
      if (krho != null) Arrays.fill(krho, (byte) 0);
      Arrays.fill(mScalar, (byte) 0);
      if (sm != null) Arrays.fill(sm, (byte) 0);
      if (sr != null) Arrays.fill(sr, (byte) 0);
      if (srho != null) Arrays.fill(srho, (byte) 0);
    }
  }
}

