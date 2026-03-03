package org.xrpl.xrpl4j.crypto.mpt.wrapper;

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
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTConvertBackContext;
import org.xrpl.xrpl4j.crypto.mpt.models.ConfidentialMPTConvertBackProof;
import org.xrpl.xrpl4j.crypto.mpt.models.PedersenProofParams;

/**
 * High-level interface for generating ConfidentialMPTConvertBack proofs.
 *
 * <p>This interface mirrors the C function {@code mpt_get_convert_back_proof} from mpt_utility.h,
 * but uses Java-friendly types.</p>
 *
 * <p>The proof consists of:
 * <ul>
 *   <li>Balance linkage proof (195 bytes) - links ElGamal ciphertext to Pedersen commitment for balance</li>
 *   <li>Single bulletproof range proof (688 bytes) - proves remaining balance is in valid range [0, 2^64)</li>
 * </ul>
 */
public interface ConfidentialMPTConvertBackProofGenerator {

  /**
   * Generates a ConfidentialMPTConvertBack proof.
   *
   * @param senderKeyPair  The sender's key pair (must be secp256k1).
   * @param amount         The amount being converted back to public balance.
   * @param context        The context hash binding the proof to a specific transaction.
   * @param balanceParams  The Pedersen proof parameters for the sender's current balance.
   *
   * @return A {@link ConfidentialMPTConvertBackProof} containing the complete proof.
   *
   * @throws NullPointerException     if any parameter is null.
   * @throws IllegalArgumentException if senderKeyPair is not secp256k1.
   * @throws IllegalStateException    if proof generation fails.
   */
  ConfidentialMPTConvertBackProof generateProof(
    KeyPair senderKeyPair,
    UnsignedLong amount,
    ConfidentialMPTConvertBackContext context,
    PedersenProofParams balanceParams
  );
}

