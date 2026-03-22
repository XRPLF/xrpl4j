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
 * <p>Port of {@code secp256k1_mpt_verify_same_plaintext_multi} from proof_same_plaintext_multi.c.</p>
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
    final List<UnsignedByteArray> R,
    final List<UnsignedByteArray> S,
    final List<UnsignedByteArray> Pk,
    final UnsignedByteArray contextId
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(R, "R must not be null");
    Objects.requireNonNull(S, "S must not be null");
    Objects.requireNonNull(Pk, "Pk must not be null");

    int n = R.size();
    Preconditions.checkArgument(n >= 2, "Must have at least 2 participants, but had %s", n);
    Preconditions.checkArgument(S.size() == n, "S size must match R size");
    Preconditions.checkArgument(Pk.size() == n, "Pk size must match R size");

    int expectedSize = SamePlaintextProofGenerator.proofSize(n);
    if (proof.length() != expectedSize) {
      return false;
    }

    byte[] proofBytes = proof.toByteArray();
    int offset = 0;

    try {
      // 1. Deserialize

      // Tm
      byte[] TmBytes = Arrays.copyOfRange(proofBytes, offset, offset + 33);
      offset += 33;
      ECPoint Tm = Secp256k1Operations.deserialize(TmBytes);

      // TrG[0..N-1]
      List<UnsignedByteArray> TrGList = new ArrayList<>(n);
      ECPoint[] TrG = new ECPoint[n];
      for (int i = 0; i < n; i++) {
        byte[] trgBytes = Arrays.copyOfRange(proofBytes, offset, offset + 33);
        offset += 33;
        TrG[i] = Secp256k1Operations.deserialize(trgBytes);
        TrGList.add(UnsignedByteArray.of(trgBytes));
      }

      // TrP[0..N-1]
      List<UnsignedByteArray> TrPList = new ArrayList<>(n);
      ECPoint[] TrP = new ECPoint[n];
      for (int i = 0; i < n; i++) {
        byte[] trpBytes = Arrays.copyOfRange(proofBytes, offset, offset + 33);
        offset += 33;
        TrP[i] = Secp256k1Operations.deserialize(trpBytes);
        TrPList.add(UnsignedByteArray.of(trpBytes));
      }

      // sm
      byte[] sm = Arrays.copyOfRange(proofBytes, offset, offset + 32);
      offset += 32;
      BigInteger smInt = new BigInteger(1, sm);
      if (!Secp256k1Operations.isValidPrivateKey(smInt)) {
        return false;
      }

      // 2. Recompute Challenge
      UnsignedByteArray eUba = ChallengeUtils.buildSamePlaintextChallenge(
        R, S, Pk, UnsignedByteArray.of(TmBytes), TrGList, TrPList, contextId
      );
      byte[] e = eUba.toByteArray();
      BigInteger eInt = new BigInteger(1, e);

      // 3. Verify Equations

      // Precompute s_m * G (Shared across all i)
      ECPoint SmG = Secp256k1Operations.multiplyG(smInt);

      for (int i = 0; i < n; i++) {
        // Read s_ri
        byte[] sri = Arrays.copyOfRange(proofBytes, offset, offset + 32);
        offset += 32;
        BigInteger sriInt = new BigInteger(1, sri);
        if (!Secp256k1Operations.isValidPrivateKey(sriInt)) {
          return false;
        }

        ECPoint Ri = Secp256k1Operations.deserialize(R.get(i).toByteArray());
        ECPoint Si = Secp256k1Operations.deserialize(S.get(i).toByteArray());
        ECPoint Pki = Secp256k1Operations.deserialize(Pk.get(i).toByteArray());

        // Eq 1: s_ri * G == TrG_i + e * R_i
        ECPoint lhs1 = Secp256k1Operations.multiplyG(sriInt);
        ECPoint eRi = Secp256k1Operations.multiply(Ri, eInt);
        ECPoint rhs1 = Secp256k1Operations.add(TrG[i], eRi);
        if (!lhs1.equals(rhs1)) {
          return false;
        }

        // Eq 2: s_m * G + s_ri * Pk_i == Tm + TrP_i + e * S_i
        ECPoint sriPki = Secp256k1Operations.multiply(Pki, sriInt);
        ECPoint lhs2 = Secp256k1Operations.add(SmG, sriPki);
        ECPoint eSi = Secp256k1Operations.multiply(Si, eInt);
        ECPoint rhs2 = Secp256k1Operations.add(Secp256k1Operations.add(Tm, TrP[i]), eSi);
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

