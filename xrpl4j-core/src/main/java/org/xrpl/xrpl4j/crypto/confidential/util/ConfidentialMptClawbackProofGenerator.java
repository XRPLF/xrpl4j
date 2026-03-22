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
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptClawbackProof;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

/**
 * High-level interface for generating ConfidentialMptClawback proofs.
 *
 * <p>This interface generates a plaintext equality proof that proves the issuer
 * knows the plaintext amount being clawed back from the holder's IssuerEncryptedBalance.</p>
 *
 * <p>The proof is a Chaum-Pedersen style Sigma protocol proof. For clawback, rippled
 * calls the C function with swapped parameters:
 * {@code secp256k1_equality_plaintext_prove(ctx, proof, &pk, &c2, &c1, amount, privateKey, contextHash)}
 * which maps to: c1 ← issuer's pk, c2 ← balance.c2, pk_recipient ← balance.c1.</p>
 *
 * <p>This interface handles the swapping internally, so callers simply pass the
 * IssuerEncryptedBalance ciphertext and issuer's keys.</p>
 */
public interface ConfidentialMptClawbackProofGenerator {

  /**
   * Generates a ConfidentialMptClawback proof.
   *
   * @param issuerEncryptedBalance The IssuerEncryptedBalance ciphertext from the MPToken.
   * @param issuerPublicKey        The issuer's ElGamal public key.
   * @param amount                 The amount being clawed back.
   * @param issuerPrivateKey       The issuer's ElGamal private key (used as "randomness" in the proof).
   * @param context                The context hash binding the proof to a specific transaction.
   *
   * @return A {@link ConfidentialMptClawbackProof} containing the 98-byte proof.
   *
   * @throws NullPointerException     if any parameter is null.
   * @throws IllegalArgumentException if any parameter is invalid.
   * @throws IllegalStateException    if proof generation fails.
   */
  ConfidentialMptClawbackProof generateProof(
    EncryptedAmount issuerEncryptedBalance,
    PublicKey issuerPublicKey,
    UnsignedLong amount,
    PrivateKey issuerPrivateKey,
    ConfidentialMptClawbackContext context
  );
}

