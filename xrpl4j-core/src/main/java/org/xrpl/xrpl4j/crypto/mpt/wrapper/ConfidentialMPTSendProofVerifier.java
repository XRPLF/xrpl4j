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

import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTSendContext;
import org.xrpl.xrpl4j.crypto.mpt.models.ConfidentialMPTSendProof;
import org.xrpl.xrpl4j.crypto.mpt.models.MPTConfidentialParty;
import org.xrpl.xrpl4j.crypto.mpt.models.PedersenCommitment;

import java.util.List;

/**
 * High-level interface for verifying ConfidentialMPTSend proofs.
 *
 * <p>This interface mirrors the C function {@code mpt_verify_confidential_send_proof} from mpt_utility.h,
 * but uses Java-friendly types.</p>
 *
 * <p>The verification checks:
 * <ul>
 *   <li>Same plaintext multi proof - all ciphertexts encrypt the same amount</li>
 *   <li>Amount linkage proof - ElGamal ciphertext and Pedersen commitment encode the same amount</li>
 *   <li>Balance linkage proof - ElGamal ciphertext and Pedersen commitment encode the same balance</li>
 *   <li>Aggregated bulletproof range proof - amount and remaining balance are in valid range</li>
 * </ul>
 */
public interface ConfidentialMPTSendProofVerifier {

  /**
   * Verifies a ConfidentialMPTSend proof.
   *
   * @param proof             The proof to verify.
   * @param recipients        The list of recipients (sender, destination, issuer, and optionally auditor).
   * @param context           The context hash binding the proof to a specific transaction.
   * @param amountCommitment  The Pedersen commitment for the amount.
   * @param balanceCommitment The Pedersen commitment for the sender's balance.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   *
   * @throws NullPointerException     if any parameter is null.
   * @throws IllegalArgumentException if recipients is empty.
   */
  boolean verifyProof(
    ConfidentialMPTSendProof proof,
    List<MPTConfidentialParty> recipients,
    ConfidentialMPTSendContext context,
    PedersenCommitment amountCommitment,
    PedersenCommitment balanceCommitment
  );
}

