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
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.PlaintextEqualityProofVerifier;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * BouncyCastle implementation of {@link PlaintextEqualityProofVerifier}.
 *
 * <p>Port of {@code secp256k1_equality_plaintext_verify} from proof_equality_plaintext.c.</p>
 */
@SuppressWarnings("checkstyle")
public class BcPlaintextEqualityProofVerifier implements PlaintextEqualityProofVerifier {

  private static final int PROOF_LENGTH = 98;

  @Override
  public boolean verifyProof(
    final UnsignedByteArray proof,
    final UnsignedByteArray c1,
    final UnsignedByteArray c2,
    final UnsignedByteArray pk,
    final UnsignedLong amount,
    final UnsignedByteArray contextId
  ) {
    // Validate proof length
    if (proof.length() != PROOF_LENGTH) {
      return false;
    }

    byte[] proofBytes = proof.toByteArray();

    // 1. Deserialize proof: T1 (33) || T2 (33) || s (32)
    byte[] t1Bytes = Arrays.copyOfRange(proofBytes, 0, 33);
    byte[] t2Bytes = Arrays.copyOfRange(proofBytes, 33, 66);
    byte[] s = Arrays.copyOfRange(proofBytes, 66, 98);

    ECPoint T1;
    ECPoint T2;
    ECPoint c1Point;
    ECPoint c2Point;
    ECPoint pkPoint;

    try {
      T1 = Secp256k1Operations.deserialize(t1Bytes);
      T2 = Secp256k1Operations.deserialize(t2Bytes);
      c1Point = Secp256k1Operations.deserialize(c1.toByteArray());
      c2Point = Secp256k1Operations.deserialize(c2.toByteArray());
      pkPoint = Secp256k1Operations.deserialize(pk.toByteArray());
    } catch (Exception e) {
      return false;
    }

    // Validate points are not at infinity
    if (T1.isInfinity() || T2.isInfinity() || c1Point.isInfinity() || c2Point.isInfinity() || pkPoint.isInfinity()) {
      return false;
    }

    // Validate s is a valid scalar
    if (!Secp256k1Operations.isValidScalar(s)) {
      return false;
    }
    BigInteger sInt = new BigInteger(1, s);

    // 2. Compute mG if amount > 0
    UnsignedByteArray mG = null;
    ECPoint mGPoint = null;
    if (amount.longValue() > 0) {
      byte[] mScalar = Secp256k1Operations.unsignedLongToScalar(amount);
      BigInteger mInt = new BigInteger(1, mScalar);
      mGPoint = Secp256k1Operations.multiplyG(mInt);
      mG = UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(mGPoint));
    }

    // 3. Recompute challenge e
    UnsignedByteArray e = ChallengeUtils.buildPlaintextEqualityChallenge(
      c1, c2, pk, mG, UnsignedByteArray.of(t1Bytes), UnsignedByteArray.of(t2Bytes), contextId
    );
    BigInteger eInt = new BigInteger(1, e.toByteArray());

    // 4. Verify Eq 1: s * G == T1 + e * C1
    ECPoint lhs1 = Secp256k1Operations.multiplyG(sInt);
    ECPoint eC1 = Secp256k1Operations.multiply(c1Point, eInt);
    ECPoint rhs1 = Secp256k1Operations.add(T1, eC1);
    if (!Secp256k1Operations.pointsEqual(lhs1, rhs1)) {
      return false;
    }

    // 5. Verify Eq 2: s * Pk == T2 + e * (C2 - mG)
    ECPoint lhs2 = Secp256k1Operations.multiply(pkPoint, sInt);
    ECPoint Y = c2Point;
    if (mGPoint != null) {
      ECPoint negMG = mGPoint.negate();
      Y = Secp256k1Operations.add(c2Point, negMG);
    }
    ECPoint eY = Secp256k1Operations.multiply(Y, eInt);
    ECPoint rhs2 = Secp256k1Operations.add(T2, eY);

    return Secp256k1Operations.pointsEqual(lhs2, rhs2);
  }
}

