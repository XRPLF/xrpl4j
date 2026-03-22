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

import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SecretKeyProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertProof;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

/**
 * High-level interface for verifying Schnorr Proof of Knowledge for Confidential MPT conversions.
 *
 * <p>This interface mirrors the C utility function for proof verification,
 * but uses Java-friendly types for all parameters.</p>
 *
 * @see SecretKeyProofVerifier
 * @see ConfidentialMptConvertContext
 * @see ConfidentialMptConvertProof
 */
public interface ConfidentialMptConvertProofVerifier {

  /**
   * Verifies a Schnorr Proof of Knowledge.
   *
   * <p>Verification checks: s * G == T + e * P</p>
   *
   * @param proof     The proof to verify.
   * @param publicKey The secp256k1 public key.
   * @param context   The context hash binding the proof to a specific transaction.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   *
   * @throws NullPointerException     if any parameter is null.
   * @throws IllegalArgumentException if publicKey is not a secp256k1 key.
   */
  boolean verifyProof(ConfidentialMptConvertProof proof, PublicKey publicKey, ConfidentialMptConvertContext context);
}

