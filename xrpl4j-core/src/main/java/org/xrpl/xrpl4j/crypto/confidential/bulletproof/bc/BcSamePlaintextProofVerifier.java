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
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SamePlaintextProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SamePlaintextProofVerifier;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * BouncyCastle implementation of {@link SamePlaintextProofVerifier}.
 *
 * <p>Port of {@code secp256k1_mpt_verify_equality_shared_r} from proof_same_plaintext_multi_shared_r.c.</p>
 */
@SuppressWarnings("checkstyle")
public class BcSamePlaintextProofVerifier implements SamePlaintextProofVerifier {

  /**
   * Constructs a new instance.
   */
  public BcSamePlaintextProofVerifier() {
  }

  @Override
  public boolean verifyProof(
    final UnsignedByteArray proof,
    final UnsignedByteArray c1,
    final List<UnsignedByteArray> c2List,
    final List<UnsignedByteArray> pkList,
    final UnsignedByteArray contextId
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(c1, "c1 must not be null");
    Objects.requireNonNull(c2List, "c2List must not be null");
    Objects.requireNonNull(pkList, "pkList must not be null");

    int n = c2List.size();
    Preconditions.checkArgument(n >= 2, "Must have at least 2 participants, but had %s", n);
    Preconditions.checkArgument(pkList.size() == n, "pkList size must match c2List size");

    int expectedSize = SamePlaintextProofGenerator.proofSize(n);
    if (proof.length() != expectedSize) {
      return false;
    }

    byte[] proofBytes = proof.toByteArray();
    int offset = 0;

    try {
      // 1. Deserialize: Tr (33 bytes) || Tm[0..N-1] (N*33 bytes) || sm (32 bytes) || sr (32 bytes)

      // Tr
      byte[] TrBytes = Arrays.copyOfRange(proofBytes, offset, offset + 33);
      offset += 33;
      ECPoint Tr = Secp256k1Operations.deserialize(TrBytes);

      // Tm[0..N-1]
      List<UnsignedByteArray> TmList = new ArrayList<>(n);
      ECPoint[] Tm = new ECPoint[n];
      for (int i = 0; i < n; i++) {
        byte[] tmBytes = Arrays.copyOfRange(proofBytes, offset, offset + 33);
        offset += 33;
        Tm[i] = Secp256k1Operations.deserialize(tmBytes);
        TmList.add(UnsignedByteArray.of(tmBytes));
      }

      // sm
      byte[] sm = Arrays.copyOfRange(proofBytes, offset, offset + 32);
      offset += 32;
      BigInteger smInt = new BigInteger(1, sm);
      if (!Secp256k1Operations.isValidPrivateKey(smInt)) {
        return false;
      }

      // sr
      byte[] sr = Arrays.copyOfRange(proofBytes, offset, offset + 32);
      offset += 32;
      BigInteger srInt = new BigInteger(1, sr);
      if (!Secp256k1Operations.isValidPrivateKey(srInt)) {
        return false;
      }

      // 2. Recompute challenge
      UnsignedByteArray eUba = ChallengeUtils.buildEqualitySharedRChallenge(
        c1, c2List, pkList, UnsignedByteArray.of(TrBytes), TmList, contextId
      );
      byte[] e = eUba.toByteArray();
      BigInteger eInt = new BigInteger(1, e);

      // 3. Verify equation 1: sr * G == Tr + e * C1
      ECPoint C1Point = Secp256k1Operations.deserialize(c1.toByteArray());
      ECPoint lhs1 = Secp256k1Operations.multiplyG(srInt);
      ECPoint eC1 = Secp256k1Operations.multiply(C1Point, eInt);
      ECPoint rhs1 = Secp256k1Operations.add(Tr, eC1);
      if (!lhs1.equals(rhs1)) {
        return false;
      }

      // Precompute sm * G (shared across all i)
      ECPoint SmG = Secp256k1Operations.multiplyG(smInt);

      // 4. For each i: sm * G + sr * Pk_i == Tm_i + e * C2_i
      for (int i = 0; i < n; i++) {
        ECPoint Pki = Secp256k1Operations.deserialize(pkList.get(i).toByteArray());
        ECPoint C2i = Secp256k1Operations.deserialize(c2List.get(i).toByteArray());

        ECPoint srPki = Secp256k1Operations.multiply(Pki, srInt);
        ECPoint lhs2 = Secp256k1Operations.add(SmG, srPki);

        ECPoint eC2i = Secp256k1Operations.multiply(C2i, eInt);
        ECPoint rhs2 = Secp256k1Operations.add(Tm[i], eC2i);

        if (!lhs2.equals(rhs2)) {
          return false;
        }
      }

      // Verify we consumed exactly the expected bytes
      return offset == proofBytes.length;

    } catch (Exception ex) {
      return false;
    }
  }
}
