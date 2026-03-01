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
}