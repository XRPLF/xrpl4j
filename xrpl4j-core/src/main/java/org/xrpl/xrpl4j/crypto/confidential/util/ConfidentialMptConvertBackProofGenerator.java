package org.xrpl.xrpl4j.crypto.confidential.util;

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
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertBackProof;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;

/**
 * High-level interface for generating ConfidentialMptConvertBack proofs.
 *
 * <p>This interface mirrors the C function {@code mpt_get_convert_back_proof} from mpt_utility.h,
 * but uses Java-friendly types.</p>
 *
 * <p>The proof consists of a compact AND-composed sigma proof (128 bytes) over the balance
 * witness, followed by a single Bulletproof range proof (688 bytes) over the remainder
 * commitment. Total size: 816 bytes.</p>
 */
public interface ConfidentialMptConvertBackProofGenerator {

  /**
   * Generates a ConfidentialMptConvertBack proof.
   *
   * @param senderKeyPair  The sender's key pair (must be secp256k1).
   * @param amount         The amount being converted back to public balance.
   * @param context        The context hash binding the proof to a specific transaction.
   * @param balanceParams  The Pedersen proof parameters for the sender's current balance.
   *
   * @return A {@link ConfidentialMptConvertBackProof} containing the complete proof.
   *
   * @throws NullPointerException     if any parameter is null.
   * @throws IllegalArgumentException if senderKeyPair is not secp256k1.
   * @throws IllegalStateException    if proof generation fails.
   */
  ConfidentialMptConvertBackProof generateProof(
    KeyPair senderKeyPair,
    UnsignedLong amount,
    ConfidentialMptConvertBackContext context,
    PedersenProofParams balanceParams
  );
}

