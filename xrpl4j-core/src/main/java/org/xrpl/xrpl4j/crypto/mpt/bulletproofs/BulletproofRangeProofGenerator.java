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
import org.xrpl.xrpl4j.crypto.mpt.wrapper.BulletproofRangeProof;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.PedersenCommitment;

/**
 * Interface for generating and verifying Bulletproof range proofs.
 *
 * <p>Bulletproofs are zero-knowledge range proofs that prove a committed value
 * is within the range [0, 2^64 - 1] without revealing the actual value.</p>
 *
 * <p>Used in ConfidentialMPTConvertBack to prove that the remaining balance
 * after conversion is non-negative (i.e., the user is not converting more
 * than they have).</p>
 *
 * <p>The proof is generated for a Pedersen commitment of the form:
 * C = value * G + blindingFactor * H</p>
 *
 * <p>Proof sizes:
 * <ul>
 *   <li>Single value: 688 bytes</li>
 *   <li>Double value (aggregated): 754 bytes</li>
 * </ul>
 *
 * @see BulletproofRangeProof
 */
public interface BulletproofRangeProofGenerator {

  /**
   * Generates a bulletproof range proof for a single value.
   *
   * <p>This proves that the value is in the range [0, 2^64 - 1].</p>
   *
   * @param value          The value to prove is in range.
   * @param blindingFactor The blinding factor used in the Pedersen commitment.
   * @param context        The context for domain separation.
   *
   * @return A {@link BulletproofRangeProof} containing the 688-byte proof.
   *
   * @throws NullPointerException if any required parameter is null.
   */
  BulletproofRangeProof generateProof(
    UnsignedLong value,
    BlindingFactor blindingFactor,
    LinkProofContext context
  );

  /**
   * Generates an aggregated bulletproof range proof for two values.
   *
   * <p>This proves that both values are in the range [0, 2^64 - 1].</p>
   *
   * @param value1          The first value to prove is in range.
   * @param blindingFactor1 The blinding factor for the first value's commitment.
   * @param value2          The second value to prove is in range.
   * @param blindingFactor2 The blinding factor for the second value's commitment.
   * @param context         The context for domain separation.
   *
   * @return A {@link BulletproofRangeProof} containing the 754-byte proof.
   *
   * @throws NullPointerException if any required parameter is null.
   */
  BulletproofRangeProof generateAggregatedProof(
    UnsignedLong value1,
    BlindingFactor blindingFactor1,
    UnsignedLong value2,
    BlindingFactor blindingFactor2,
    LinkProofContext context
  );

  /**
   * Verifies a bulletproof range proof for a single commitment.
   *
   * @param proof      The proof to verify.
   * @param commitment The Pedersen commitment to verify against.
   * @param context    The context for domain separation.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   *
   * @throws NullPointerException if any required parameter is null.
   */
  boolean verify(
    BulletproofRangeProof proof,
    PedersenCommitment commitment,
    LinkProofContext context
  );

  /**
   * Verifies an aggregated bulletproof range proof for two commitments.
   *
   * @param proof       The proof to verify.
   * @param commitment1 The first Pedersen commitment to verify against.
   * @param commitment2 The second Pedersen commitment to verify against.
   * @param context     The context for domain separation.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   *
   * @throws NullPointerException if any required parameter is null.
   */
  boolean verifyAggregated(
    BulletproofRangeProof proof,
    PedersenCommitment commitment1,
    PedersenCommitment commitment2,
    LinkProofContext context
  );
}

