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
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.port.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.mpt.port.PedersenLinkProofGeneratorPort;
import org.xrpl.xrpl4j.crypto.mpt.port.PedersenLinkProofVerifierPort;

import java.math.BigInteger;

/**
 * BouncyCastle implementation of {@link PedersenLinkProofVerifierPort}.
 *
 * <p>Port of {@code secp256k1_elgamal_pedersen_link_verify} from proof_link.c.</p>
 */
public class BcPedersenLinkProofVerifierPort implements PedersenLinkProofVerifierPort {

  @Override
  public boolean verifyProof(
    UnsignedByteArray proof,
    UnsignedByteArray c1,
    UnsignedByteArray c2,
    UnsignedByteArray pk,
    UnsignedByteArray pcm,
    UnsignedByteArray contextId
  ) {
    try {
      byte[] proofBytes = proof.toByteArray();

      // Check proof size
      if (proofBytes.length != PedersenLinkProofGeneratorPort.PROOF_SIZE) {
        return false;
      }

      // 1. Deserialize
      int offset = 0;

      byte[] t1Bytes = new byte[33];
      System.arraycopy(proofBytes, offset, t1Bytes, 0, 33);
      ECPoint T1 = Secp256k1Operations.deserialize(t1Bytes);
      if (T1 == null) return false;
      offset += 33;

      byte[] t2Bytes = new byte[33];
      System.arraycopy(proofBytes, offset, t2Bytes, 0, 33);
      ECPoint T2 = Secp256k1Operations.deserialize(t2Bytes);
      if (T2 == null) return false;
      offset += 33;

      byte[] t3Bytes = new byte[33];
      System.arraycopy(proofBytes, offset, t3Bytes, 0, 33);
      ECPoint T3 = Secp256k1Operations.deserialize(t3Bytes);
      if (T3 == null) return false;
      offset += 33;

      byte[] sm = new byte[32];
      System.arraycopy(proofBytes, offset, sm, 0, 32);
      if (!Secp256k1Operations.isValidScalar(sm)) return false;
      offset += 32;

      byte[] sr = new byte[32];
      System.arraycopy(proofBytes, offset, sr, 0, 32);
      if (!Secp256k1Operations.isValidScalar(sr)) return false;
      offset += 32;

      byte[] srho = new byte[32];
      System.arraycopy(proofBytes, offset, srho, 0, 32);
      if (!Secp256k1Operations.isValidScalar(srho)) return false;

      BigInteger smInt = new BigInteger(1, sm);
      BigInteger srInt = new BigInteger(1, sr);
      BigInteger srhoInt = new BigInteger(1, srho);

      // Parse input points
      ECPoint c1Point = Secp256k1Operations.deserialize(c1.toByteArray());
      ECPoint c2Point = Secp256k1Operations.deserialize(c2.toByteArray());
      ECPoint pkPoint = Secp256k1Operations.deserialize(pk.toByteArray());
      ECPoint pcmPoint = Secp256k1Operations.deserialize(pcm.toByteArray());

      if (c1Point == null || c2Point == null || pkPoint == null || pcmPoint == null) {
        return false;
      }

      // 2. Challenge
      UnsignedByteArray T1Bytes = UnsignedByteArray.of(t1Bytes);
      UnsignedByteArray T2Bytes = UnsignedByteArray.of(t2Bytes);
      UnsignedByteArray T3Bytes = UnsignedByteArray.of(t3Bytes);

      UnsignedByteArray e = ChallengeUtils.buildPedersenLinkChallenge(
        c1, c2, pk, pcm, T1Bytes, T2Bytes, T3Bytes, contextId
      );
      BigInteger eInt = new BigInteger(1, e.toByteArray());

      // 3. Verification Equations

      // Eq 1: sr * G == T1 + e * C1
      ECPoint lhs1 = Secp256k1Operations.multiplyG(srInt);
      ECPoint eC1 = Secp256k1Operations.multiply(c1Point, eInt);
      ECPoint rhs1 = Secp256k1Operations.add(T1, eC1);
      if (!lhs1.equals(rhs1)) return false;

      // Eq 2: sm * G + sr * Pk == T2 + e * C2
      ECPoint smG = Secp256k1Operations.multiplyG(smInt);
      ECPoint srPk = Secp256k1Operations.multiply(pkPoint, srInt);
      ECPoint lhs2 = Secp256k1Operations.add(smG, srPk);
      ECPoint eC2 = Secp256k1Operations.multiply(c2Point, eInt);
      ECPoint rhs2 = Secp256k1Operations.add(T2, eC2);
      if (!lhs2.equals(rhs2)) return false;

      // Eq 3: sm * G + srho * H == T3 + e * PCm
      ECPoint H = Secp256k1Operations.getH();
      ECPoint srhoH = Secp256k1Operations.multiply(H, srhoInt);
      ECPoint lhs3 = Secp256k1Operations.add(smG, srhoH);
      ECPoint ePcm = Secp256k1Operations.multiply(pcmPoint, eInt);
      ECPoint rhs3 = Secp256k1Operations.add(T3, ePcm);
      if (!lhs3.equals(rhs3)) return false;

      return true;

    } catch (Exception ex) {
      return false;
    }
  }
}

