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

import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SecretKeyProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertProof;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;

/**
 * High-level interface for generating Schnorr Proof of Knowledge for Confidential MPT conversions.
 *
 * <p>This interface mirrors the C utility function {@code mpt_get_convert_proof} from mpt_utility.h,
 * but uses Java-friendly types for all parameters and return values.</p>
 *
 * <p>The proof proves that the sender possesses the private key associated with the account,
 * binding it to the specific transaction via the context hash.</p>
 *
 * @see SecretKeyProofGenerator
 * @see ConfidentialMptConvertContext
 * @see ConfidentialMptConvertProof
 */
public interface ConfidentialMptConvertProofGenerator {

  /**
   * Generates a Schnorr Proof of Knowledge for a Confidential MPT conversion.
   *
   * <p>The proof format is: T (33 bytes compressed point) || s (32 bytes scalar) = 65 bytes total.</p>
   *
   * @param keyPair The secp256k1 key pair (both public and private key).
   * @param context The context hash binding the proof to a specific transaction.
   *
   * @return A {@link ConfidentialMptConvertProof} containing the 65-byte proof.
   *
   * @throws NullPointerException     if any parameter is null.
   * @throws IllegalArgumentException if keyPair is not a secp256k1 key pair.
   * @throws IllegalStateException    if proof generation fails.
   */
  ConfidentialMptConvertProof generateProof(KeyPair keyPair, ConfidentialMptConvertContext context);
}

