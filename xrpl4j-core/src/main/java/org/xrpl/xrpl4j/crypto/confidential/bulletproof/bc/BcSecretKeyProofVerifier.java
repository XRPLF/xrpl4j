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

import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SecretKeyProofVerifier;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * BouncyCastle implementation of {@link SecretKeyProofVerifier}.
 *
 * <p>Port of {@code secp256k1_mpt_pok_sk_verify} from proof_pok_sk.c.</p>
 */
@SuppressWarnings("checkstyle")
public class BcSecretKeyProofVerifier implements SecretKeyProofVerifier {

  @Override
  public boolean verifyProof(
    final UnsignedByteArray proof,
    final UnsignedByteArray pk,
    final UnsignedByteArray contextId
  ) {
    byte[] proofBytes = proof.toByteArray();
    byte[] s = new byte[32];

    try {
      // 1. Parse T (33 bytes)
      // if (!secp256k1_ec_pubkey_parse(ctx, &T, ptr, 33)) goto cleanup;
      byte[] TBytes = Arrays.copyOfRange(proofBytes, 0, 33);
      ECPoint T;
      try {
        T = Secp256k1Operations.deserialize(TBytes);
      } catch (Exception e) {
        return false;
      }

      // 2. Parse s (32 bytes)
      // memcpy(s, ptr, 32);
      System.arraycopy(proofBytes, 33, s, 0, 32);

      // if (!secp256k1_ec_seckey_verify(ctx, s)) goto cleanup;
      BigInteger sInt = new BigInteger(1, s);
      if (!Secp256k1Operations.isValidPrivateKey(sInt)) {
        return false;
      }

      // 3. Recompute Challenge
      // build_pok_challenge(ctx, e, pk, &T, context_id);
      UnsignedByteArray eUba = ChallengeUtils.buildPokChallenge(pk, UnsignedByteArray.of(TBytes), contextId);
      byte[] e = eUba.toByteArray();
      BigInteger eInt = new BigInteger(1, e);

      // 4. Verify Equation: s*G == T + e*Pk
      // if (!secp256k1_ec_pubkey_create(ctx, &LHS, s)) goto cleanup;
      ECPoint LHS = Secp256k1Operations.multiplyG(sInt);

      // ePk = *pk;
      // if (!secp256k1_ec_pubkey_tweak_mul(ctx, &ePk, e)) goto cleanup;
      ECPoint Pk = Secp256k1Operations.deserialize(pk.toByteArray());
      ECPoint ePk = Secp256k1Operations.multiply(Pk, eInt);

      // const secp256k1_pubkey* addends[2] = {&T, &ePk};
      // if (!secp256k1_ec_pubkey_combine(ctx, &RHS, addends, 2)) goto cleanup;
      ECPoint RHS = Secp256k1Operations.add(T, ePk);

      // if (!pubkey_equal(ctx, &LHS, &RHS)) goto cleanup;
      return LHS.equals(RHS);

    } finally {
      Arrays.fill(s, (byte) 0);
      Arrays.fill(proofBytes, (byte) 0);
    }
  }
}

