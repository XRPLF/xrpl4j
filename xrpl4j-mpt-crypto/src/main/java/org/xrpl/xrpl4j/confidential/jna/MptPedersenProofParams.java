package org.xrpl.xrpl4j.confidential.jna;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: mpt-crypto
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

import com.sun.jna.Structure;


/**
 * JNA mapping for the C struct {@code mpt_pedersen_proof_params}.
 *
 * <pre>
 * struct mpt_pedersen_proof_params {
 *     uint8_t pedersen_commitment[33];
 *     uint64_t amount;
 *     uint8_t encrypted_amount[66];
 *     uint8_t blinding_factor[32];
 * };
 * </pre>
 *
 * <p>JNA's {@link Structure} automatically handles the padding between
 * {@code pedersenCommitment} (33 bytes) and {@code amount} (8-byte aligned).</p>
 */
@Structure.FieldOrder({"pedersenCommitment", "amount", "encryptedAmount", "blindingFactor"})
public class MptPedersenProofParams extends Structure {

  /** The 33-byte Pedersen commitment. */
  public byte[] pedersenCommitment = new byte[33];

  /** The numeric amount being committed. */
  public long amount;

  /** The 66-byte ElGamal ciphertext (C1 || C2). */
  public byte[] encryptedAmount = new byte[66];

  /** The 32-byte blinding factor. */
  public byte[] blindingFactor = new byte[32];

  /** Default constructor required by JNA. */
  public MptPedersenProofParams() {
  }
}
