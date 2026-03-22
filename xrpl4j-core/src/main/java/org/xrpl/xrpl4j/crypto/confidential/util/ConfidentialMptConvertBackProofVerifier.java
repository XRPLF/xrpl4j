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
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertBackProof;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

/**
 * High-level interface for verifying ConfidentialMptConvertBack proofs.
 *
 * <p>This interface mirrors the C verification logic for convert back proofs.</p>
 */
public interface ConfidentialMptConvertBackProofVerifier {

  /**
   * Verifies a ConfidentialMptConvertBack proof.
   *
   * @param proof             The proof to verify.
   * @param senderPublicKey   The sender's ElGamal public key.
   * @param encryptedBalance  The sender's encrypted balance from the ledger.
   * @param balanceCommitment The Pedersen commitment for the sender's balance.
   * @param amount            The amount being converted back.
   * @param context           The context hash binding the proof to a specific transaction.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   *
   * @throws NullPointerException if any parameter is null.
   */
  boolean verifyProof(
    ConfidentialMptConvertBackProof proof,
    PublicKey senderPublicKey,
    EncryptedAmount encryptedBalance,
    PedersenCommitment balanceCommitment,
    UnsignedLong amount,
    ConfidentialMptConvertBackContext context
  );
}

