package org.xrpl.xrpl4j.crypto.mpt.port;

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

import com.google.common.hash.Hashing;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Port of challenge computation functions from C implementation.
 *
 * <p>This class provides low-level challenge computation using {@link UnsignedByteArray}
 * for all inputs, matching the C implementation's interface.</p>
 *
 * <p>All intermediate byte arrays are cleared after use to match C's OPENSSL_cleanse behavior.</p>
 */
public final class ChallengeUtils {

  private static final String POK_SK_DOMAIN = "MPT_POK_SK_REGISTER";
  private static final String SAME_PLAINTEXT_DOMAIN = "MPT_POK_SAME_PLAINTEXT_PROOF";

  private ChallengeUtils() {
    // Utility class
  }

  /**
   * Builds the challenge hash for the Schnorr proof of knowledge of secret key.
   *
   * <p>Port of {@code build_pok_challenge} from proof_pok_sk.c.</p>
   *
   * <p>The challenge is computed as:
   * {@code e = reduce(SHA256("MPT_POK_SK_REGISTER" || pk || T [|| contextId])) mod n}</p>
   *
   * @param pk        The public key (33 bytes compressed).
   * @param T         The commitment point (33 bytes compressed).
   * @param contextId The optional 32-byte context identifier. Can be null.
   *
   * @return A 32-byte challenge scalar (reduced mod curve order).
   */
  public static UnsignedByteArray buildPokChallenge(
    final UnsignedByteArray pk,
    final UnsignedByteArray T,
    final UnsignedByteArray contextId
  ) {
    byte[] domainBytes = POK_SK_DOMAIN.getBytes(StandardCharsets.UTF_8);
    byte[] pkBytes = pk.toByteArray();
    byte[] tBytes = T.toByteArray();

    int contextIdLength = (contextId != null) ? 32 : 0;
    byte[] hashInput = new byte[domainBytes.length + 33 + 33 + contextIdLength];
    int offset = 0;

    System.arraycopy(domainBytes, 0, hashInput, offset, domainBytes.length);
    offset += domainBytes.length;
    System.arraycopy(pkBytes, 0, hashInput, offset, 33);
    offset += 33;
    System.arraycopy(tBytes, 0, hashInput, offset, 33);
    offset += 33;

    if (contextId != null) {
      byte[] contextBytes = contextId.toByteArray();
      System.arraycopy(contextBytes, 0, hashInput, offset, 32);
      Arrays.fill(contextBytes, (byte) 0);
    }

    byte[] h = Hashing.sha256().hashBytes(hashInput).asBytes();

    // secp256k1_mpt_scalar_reduce32
    byte[] e = Secp256k1Operations.reduceToScalar(h);

    // Clear intermediate values
    Arrays.fill(pkBytes, (byte) 0);
    Arrays.fill(tBytes, (byte) 0);
    Arrays.fill(hashInput, (byte) 0);
    Arrays.fill(h, (byte) 0);

    return UnsignedByteArray.of(e);
  }

  /**
   * Builds the challenge hash for the Same Plaintext Multi proof.
   *
   * <p>Port of {@code compute_challenge_multi} from proof_same_plaintext_multi.c.</p>
   *
   * <p>The challenge is computed as:
   * {@code e = reduce(SHA256("MPT_POK_SAME_PLAINTEXT_PROOF" || {R_i, S_i, Pk_i} || Tm || {TrG_i, TrP_i} [|| contextId])) mod n}</p>
   *
   * @param R         List of R points (c1 from ciphertexts), each 33 bytes compressed.
   * @param S         List of S points (c2 from ciphertexts), each 33 bytes compressed.
   * @param Pk        List of public keys, each 33 bytes compressed.
   * @param Tm        The shared commitment point (33 bytes compressed).
   * @param TrG       List of TrG commitment points, each 33 bytes compressed.
   * @param TrP       List of TrP commitment points, each 33 bytes compressed.
   * @param contextId The optional 32-byte context identifier. Can be null.
   *
   * @return A 32-byte challenge scalar (reduced mod curve order).
   */
  public static UnsignedByteArray buildSamePlaintextChallenge(
    final List<UnsignedByteArray> R,
    final List<UnsignedByteArray> S,
    final List<UnsignedByteArray> Pk,
    final UnsignedByteArray Tm,
    final List<UnsignedByteArray> TrG,
    final List<UnsignedByteArray> TrP,
    final UnsignedByteArray contextId
  ) {
    int n = R.size();
    byte[] domainBytes = SAME_PLAINTEXT_DOMAIN.getBytes(StandardCharsets.UTF_8);

    // Calculate total size: domain + n*(R+S+Pk) + Tm + n*(TrG+TrP) + optional contextId
    int contextIdLength = (contextId != null) ? 32 : 0;
    int totalSize = domainBytes.length + (n * 3 * 33) + 33 + (n * 2 * 33) + contextIdLength;
    byte[] hashInput = new byte[totalSize];
    int offset = 0;

    // Domain
    System.arraycopy(domainBytes, 0, hashInput, offset, domainBytes.length);
    offset += domainBytes.length;

    // 1. Public Inputs: {R_i, S_i, Pk_i}
    for (int i = 0; i < n; i++) {
      byte[] rBytes = R.get(i).toByteArray();
      System.arraycopy(rBytes, 0, hashInput, offset, 33);
      offset += 33;
      Arrays.fill(rBytes, (byte) 0);

      byte[] sBytes = S.get(i).toByteArray();
      System.arraycopy(sBytes, 0, hashInput, offset, 33);
      offset += 33;
      Arrays.fill(sBytes, (byte) 0);

      byte[] pkBytes = Pk.get(i).toByteArray();
      System.arraycopy(pkBytes, 0, hashInput, offset, 33);
      offset += 33;
      Arrays.fill(pkBytes, (byte) 0);
    }

    // 2. Commitments: Tm
    byte[] tmBytes = Tm.toByteArray();
    System.arraycopy(tmBytes, 0, hashInput, offset, 33);
    offset += 33;
    Arrays.fill(tmBytes, (byte) 0);

    // 2. Commitments: {TrG_i, TrP_i}
    for (int i = 0; i < n; i++) {
      byte[] trgBytes = TrG.get(i).toByteArray();
      System.arraycopy(trgBytes, 0, hashInput, offset, 33);
      offset += 33;
      Arrays.fill(trgBytes, (byte) 0);

      byte[] trpBytes = TrP.get(i).toByteArray();
      System.arraycopy(trpBytes, 0, hashInput, offset, 33);
      offset += 33;
      Arrays.fill(trpBytes, (byte) 0);
    }

    // 3. Context
    if (contextId != null) {
      byte[] contextBytes = contextId.toByteArray();
      System.arraycopy(contextBytes, 0, hashInput, offset, 32);
      Arrays.fill(contextBytes, (byte) 0);
    }

    byte[] h = Hashing.sha256().hashBytes(hashInput).asBytes();

    // secp256k1_mpt_scalar_reduce32
    byte[] e = Secp256k1Operations.reduceToScalar(h);

    // Clear intermediate values
    Arrays.fill(hashInput, (byte) 0);
    Arrays.fill(h, (byte) 0);

    return UnsignedByteArray.of(e);
  }
}