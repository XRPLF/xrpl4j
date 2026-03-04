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
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.SecureRandomBlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.PlaintextEqualityProofGenerator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * BouncyCastle implementation of {@link PlaintextEqualityProofGenerator}.
 *
 * <p>Port of {@code secp256k1_equality_plaintext_prove} from proof_equality_plaintext.c.</p>
 */
public class BcPlaintextEqualityProofGenerator implements PlaintextEqualityProofGenerator {

  private final BlindingFactorGenerator blindingFactorGenerator;

  /**
   * Constructs a new instance using {@link SecureRandomBlindingFactorGenerator}.
   */
  public BcPlaintextEqualityProofGenerator() {
    this(new SecureRandomBlindingFactorGenerator());
  }

  /**
   * Constructs a new instance with the specified blinding factor generator.
   *
   * @param blindingFactorGenerator The generator for random nonces.
   */
  public BcPlaintextEqualityProofGenerator(final BlindingFactorGenerator blindingFactorGenerator) {
    this.blindingFactorGenerator = Objects.requireNonNull(
      blindingFactorGenerator, "blindingFactorGenerator must not be null"
    );
  }

  @Override
  public UnsignedByteArray generateProof(
    final UnsignedByteArray c1,
    final UnsignedByteArray c2,
    final UnsignedByteArray pk,
    final UnsignedLong amount,
    final UnsignedByteArray r,
    final UnsignedByteArray contextId
  ) {
    byte[] t = null;
    byte[] s = null;
    byte[] term = null;

    try {
      // Validate inputs - C returns early if points are invalid
      ECPoint c1Point = Secp256k1Operations.deserialize(c1.toByteArray());
      ECPoint c2Point = Secp256k1Operations.deserialize(c2.toByteArray());
      ECPoint pkPoint = Secp256k1Operations.deserialize(pk.toByteArray());

      if (c1Point.isInfinity() || c2Point.isInfinity() || pkPoint.isInfinity()) {
        throw new IllegalArgumentException("Invalid point: point at infinity");
      }

      // Validate r is a valid scalar
      BigInteger rInt = new BigInteger(1, r.toByteArray());
      if (!Secp256k1Operations.isValidPrivateKey(rInt)) {
        throw new IllegalArgumentException("Invalid secret key r");
      }

      // 1. Generate random nonce t
      BlindingFactor nonce = blindingFactorGenerator.generate();
      t = nonce.toBytes();
      BigInteger tInt = new BigInteger(1, t);

      // 2. Compute commitments T1 = t * G, T2 = t * Pk
      ECPoint T1 = Secp256k1Operations.multiplyG(tInt);
      ECPoint T2 = Secp256k1Operations.multiply(pkPoint, tInt);

      if (T1.isInfinity() || T2.isInfinity()) {
        throw new IllegalStateException("Commitment point is at infinity");
      }

      byte[] T1Bytes = Secp256k1Operations.serializeCompressed(T1);
      byte[] T2Bytes = Secp256k1Operations.serializeCompressed(T2);

      // 3. Compute mG if amount > 0
      UnsignedByteArray mG = null;
      if (amount.longValue() > 0) {
        byte[] mScalar = Secp256k1Operations.unsignedLongToScalar(amount);
        BigInteger mInt = new BigInteger(1, mScalar);
        ECPoint mGPoint = Secp256k1Operations.multiplyG(mInt);
        mG = UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(mGPoint));
      }

      // 4. Compute challenge e
      UnsignedByteArray e = ChallengeUtils.buildPlaintextEqualityChallenge(
        c1, c2, pk, mG, UnsignedByteArray.of(T1Bytes), UnsignedByteArray.of(T2Bytes), contextId
      );
      BigInteger eInt = new BigInteger(1, e.toByteArray());

      // 5. Compute s = t + e * r (mod n)
      term = r.toByteArray();
      BigInteger termInt = rInt.multiply(eInt).mod(Secp256k1Operations.getCurveOrder());
      BigInteger sInt = tInt.add(termInt).mod(Secp256k1Operations.getCurveOrder());
      s = Secp256k1Operations.toBytes32(sInt);

      // 6. Serialize proof: T1 (33) || T2 (33) || s (32) = 98 bytes
      byte[] proof = new byte[PROOF_LENGTH];
      System.arraycopy(T1Bytes, 0, proof, 0, 33);
      System.arraycopy(T2Bytes, 0, proof, 33, 33);
      System.arraycopy(s, 0, proof, 66, 32);

      return UnsignedByteArray.of(proof);

    } finally {
      // OPENSSL_cleanse equivalent
      if (t != null) {
        Arrays.fill(t, (byte) 0);
      }
      if (s != null) {
        Arrays.fill(s, (byte) 0);
      }
      if (term != null) {
        Arrays.fill(term, (byte) 0);
      }
    }
  }
}

