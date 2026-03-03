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
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTSendContext;
import org.xrpl.xrpl4j.crypto.mpt.models.ConfidentialMPTSendProof;
import org.xrpl.xrpl4j.crypto.mpt.models.MPTConfidentialParty;
import org.xrpl.xrpl4j.crypto.mpt.models.PedersenProofParams;

import java.util.List;

/**
 * High-level interface for generating ConfidentialMPTSend proofs.
 *
 * <p>This interface mirrors the C function {@code mpt_get_confidential_send_proof} from mpt_utility.h,
 * but uses Java-friendly types.</p>
 *
 * <p>The proof consists of:
 * <ul>
 *   <li>Same plaintext multi proof - proves all ciphertexts encrypt the same amount</li>
 *   <li>Amount linkage proof - links ElGamal ciphertext to Pedersen commitment for amount</li>
 *   <li>Balance linkage proof - links ElGamal ciphertext to Pedersen commitment for balance</li>
 *   <li>Aggregated bulletproof range proof - proves amount and remaining balance are in valid range</li>
 * </ul>
 */
public interface ConfidentialMPTSendProofGenerator {

  /**
   * Generates a ConfidentialMPTSend proof.
   *
   * @param senderKeyPair   The sender's key pair (must be secp256k1).
   * @param amount          The amount being sent.
   * @param recipients      The list of recipients (sender, destination, issuer, and optionally auditor).
   *                        Each recipient must include its own blinding factor.
   * @param context         The context hash binding the proof to a specific transaction.
   * @param amountParams    The Pedersen proof parameters for the amount.
   * @param balanceParams   The Pedersen proof parameters for the sender's balance.
   *
   * @return A {@link ConfidentialMPTSendProof} containing the complete proof.
   *
   * @throws NullPointerException     if any parameter is null.
   * @throws IllegalArgumentException if senderKeyPair is not secp256k1, or if recipients is empty.
   * @throws IllegalStateException    if proof generation fails.
   */
  ConfidentialMPTSendProof generateProof(
    KeyPair senderKeyPair,
    UnsignedLong amount,
    List<MPTConfidentialParty> recipients,
    ConfidentialMPTSendContext context,
    PedersenProofParams amountParams,
    PedersenProofParams balanceParams
  );
}

