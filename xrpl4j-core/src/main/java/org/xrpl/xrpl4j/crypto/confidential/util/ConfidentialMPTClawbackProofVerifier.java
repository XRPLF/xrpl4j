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
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMPTClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMPTClawbackProof;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;

/**
 * High-level interface for verifying ConfidentialMPTClawback proofs.
 *
 * <p>This interface verifies a plaintext equality proof that proves the issuer
 * knows the plaintext amount being clawed back from the holder's IssuerEncryptedBalance.</p>
 *
 * <p>The verification handles the parameter swapping internally to match rippled's
 * clawback proof verification.</p>
 */
public interface ConfidentialMPTClawbackProofVerifier {

  /**
   * Verifies a ConfidentialMPTClawback proof.
   *
   * @param proof                  The proof to verify.
   * @param issuerEncryptedBalance The IssuerEncryptedBalance ciphertext from the MPToken.
   * @param issuerPublicKey        The issuer's ElGamal public key.
   * @param amount                 The amount being clawed back.
   * @param context                The context hash binding the proof to a specific transaction.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   *
   * @throws NullPointerException if any parameter is null.
   */
  boolean verifyProof(
    ConfidentialMPTClawbackProof proof,
    EncryptedAmount issuerEncryptedBalance,
    PublicKey issuerPublicKey,
    UnsignedLong amount,
    ConfidentialMPTClawbackContext context
  );
}

