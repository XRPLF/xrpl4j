package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

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
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.context.LinkProofContext;
import org.xrpl.xrpl4j.crypto.mpt.tmp.BulletproofRangeProof;
import org.xrpl.xrpl4j.crypto.mpt.tmp.PedersenCommitment;

import java.util.List;

/**
 * Interface for generating and verifying Bulletproof range proofs.
 *
 * <p>Bulletproofs are zero-knowledge range proofs that prove committed values
 * are within the range [0, 2^64 - 1] without revealing the actual values.</p>
 *
 * <p>This interface mimics the C implementation's {@code secp256k1_bulletproof_prove_agg}
 * function, which handles both single and aggregated proofs based on the number of values.</p>
 *
 * <p>Used in:
 * <ul>
 *   <li>ConfidentialMPTConvertBack - single value proof (688 bytes) for remaining balance</li>
 *   <li>ConfidentialMPTSend - aggregated proof (754 bytes) for amount and remaining balance</li>
 * </ul>
 *
 * <p>The proof is generated for Pedersen commitments of the form:
 * C = value * G + blindingFactor * H</p>
 *
 * <p>Proof sizes (formula: 292 + 66 * rounds, where rounds = log2(64 * m)):
 * <ul>
 *   <li>Single value (m=1): 688 bytes (6 rounds)</li>
 *   <li>Double value (m=2): 754 bytes (7 rounds)</li>
 * </ul>
 *
 * @see BulletproofRangeProof
 */
public interface RangeProofGenerator {

  /**
   * Generates a bulletproof range proof for one or more values.
   *
   * <p>This proves that all values are in the range [0, 2^64 - 1].
   * The number of values must be a power of 2 (1 or 2 for current use cases).</p>
   *
   * <p>This method mimics the C implementation's {@code secp256k1_bulletproof_prove_agg}.</p>
   *
   * @param values          The values to prove are in range. Must have same length as blindingFactors.
   * @param blindingFactors The blinding factors used in the Pedersen commitments.
   * @param context         The context for domain separation.
   *
   * @return A {@link BulletproofRangeProof} containing the proof.
   *         Size is 688 bytes for 1 value, 754 bytes for 2 values.
   *
   * @throws NullPointerException     if any required parameter is null.
   * @throws IllegalArgumentException if values and blindingFactors have different lengths,
   *                                  or if the number of values is not a power of 2.
   */
  BulletproofRangeProof generateProof(
    List<UnsignedLong> values,
    List<BlindingFactor> blindingFactors,
    LinkProofContext context
  );

  /**
   * Verifies a bulletproof range proof for one or more commitments.
   *
   * @param proof       The proof to verify.
   * @param commitments The Pedersen commitments to verify against.
   * @param context     The context for domain separation.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   *
   * @throws NullPointerException if any required parameter is null.
   */
  boolean verify(
    BulletproofRangeProof proof,
    List<PedersenCommitment> commitments,
    LinkProofContext context
  );
}

