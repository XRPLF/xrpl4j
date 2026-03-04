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
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.SecureRandomBlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SecretKeyProofGenerator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * BouncyCastle implementation of {@link SecretKeyProofGenerator}.
 *
 * <p>Port of {@code secp256k1_mpt_pok_sk_prove} from proof_pok_sk.c.</p>
 */
public class BcSecretKeyProofGenerator implements SecretKeyProofGenerator {

  private static final int PROOF_LENGTH = 65;

  private final BlindingFactorGenerator blindingFactorGenerator;

  /**
   * Constructs a new instance using {@link SecureRandomBlindingFactorGenerator}.
   */
  public BcSecretKeyProofGenerator() {
    this(new SecureRandomBlindingFactorGenerator());
  }

  /**
   * Constructs a new instance with the specified blinding factor generator.
   *
   * @param blindingFactorGenerator The generator for random nonces.
   */
  public BcSecretKeyProofGenerator(final BlindingFactorGenerator blindingFactorGenerator) {
    this.blindingFactorGenerator = Objects.requireNonNull(
      blindingFactorGenerator, "blindingFactorGenerator must not be null"
    );
  }

  @Override
  public UnsignedByteArray generateProof(
    final UnsignedByteArray pk,
    final UnsignedByteArray sk,
    final UnsignedByteArray contextId
  ) {
    byte[] k = null;
    byte[] e = null;
    byte[] s = null;
    byte[] term = null;

    try {
      // if (!secp256k1_ec_seckey_verify(ctx, sk)) return 0;
      BigInteger skInt = new BigInteger(1, sk.toByteArray());
      if (!Secp256k1Operations.isValidPrivateKey(skInt)) {
        throw new IllegalStateException("Invalid secret key");
      }

      // if (!generate_random_scalar(ctx, k)) goto cleanup;
      BlindingFactor nonce = blindingFactorGenerator.generate();
      k = nonce.toBytes();
      BigInteger kInt = new BigInteger(1, k);

      // if (!secp256k1_ec_pubkey_create(ctx, &T, k)) goto cleanup;
      ECPoint T = Secp256k1Operations.multiplyG(kInt);
      if (T.isInfinity()) {
        throw new IllegalStateException("T is point at infinity");
      }
      byte[] TBytes = Secp256k1Operations.serializeCompressed(T);

      // build_pok_challenge(ctx, e, pk, &T, context_id);
      UnsignedByteArray eUba = ChallengeUtils.buildPokChallenge(pk, UnsignedByteArray.of(TBytes), contextId);
      e = eUba.toByteArray();
      BigInteger eInt = new BigInteger(1, e);

      // s = k + e*sk
      // memcpy(term, sk, 32);
      // if (!secp256k1_ec_seckey_tweak_mul(ctx, term, e)) goto cleanup;
      term = sk.toByteArray();
      BigInteger termInt = skInt.multiply(eInt).mod(Secp256k1Operations.getCurveOrder());

      // memcpy(s, k, 32);
      // if (!secp256k1_ec_seckey_tweak_add(ctx, s, term)) goto cleanup;
      BigInteger sInt = kInt.add(termInt).mod(Secp256k1Operations.getCurveOrder());
      s = Secp256k1Operations.toBytes32(sInt);

      // Serialize: T (33) || s (32)
      byte[] proof = new byte[PROOF_LENGTH];
      System.arraycopy(TBytes, 0, proof, 0, 33);
      System.arraycopy(s, 0, proof, 33, 32);

      return UnsignedByteArray.of(proof);

    } finally {
      // OPENSSL_cleanse(k, 32);
      // OPENSSL_cleanse(term, 32);
      // OPENSSL_cleanse(s, 32);
      if (k != null) {
        Arrays.fill(k, (byte) 0);
      }
      if (term != null) {
        Arrays.fill(term, (byte) 0);
      }
      if (s != null) {
        Arrays.fill(s, (byte) 0);
      }
    }
  }
}

