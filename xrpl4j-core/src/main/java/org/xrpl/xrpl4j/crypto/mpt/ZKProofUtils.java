package org.xrpl.xrpl4j.crypto.mpt;

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

import com.google.common.io.BaseEncoding;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.ElGamalPedersenLinkProof;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SamePlaintextMultiProof;

import java.util.Objects;

/**
 * Utility class for combining Zero-Knowledge proofs used in Confidential MPT transactions.
 *
 * <p>For ConfidentialMPTSend transactions, the ZK proof consists of three components:
 * <ul>
 *   <li>SamePlaintextMultiProof (359 bytes for 3 participants) - proves all ciphertexts encrypt the same amount</li>
 *   <li>Amount Linkage Proof (195 bytes) - proves the amount ciphertext matches the Pedersen commitment</li>
 *   <li>Balance Linkage Proof (195 bytes) - proves the balance ciphertext matches the Pedersen commitment</li>
 * </ul>
 * Total: 749 bytes for a standard 3-participant send.</p>
 */
public final class ZKProofUtils {

  /**
   * Expected size of the combined ZK proof for a 3-participant ConfidentialMPTSend transaction.
   * SamePlaintextMultiProof (359) + Amount Linkage (195) + Balance Linkage (195) = 749 bytes.
   */
  public static final int SEND_PROOF_SIZE_3_PARTICIPANTS = 749;

  private ZKProofUtils() {
    // Utility class - prevent instantiation
  }

  /**
   * Combines the three ZK proofs required for a ConfidentialMPTSend transaction into a single byte array.
   *
   * <p>The proofs are concatenated in the following order:
   * <ol>
   *   <li>SamePlaintextMultiProof - proves all ciphertexts encrypt the same amount</li>
   *   <li>Amount Linkage Proof - proves the sender's ciphertext matches the amount commitment</li>
   *   <li>Balance Linkage Proof - proves the sender's balance ciphertext matches the balance commitment</li>
   * </ol>
   *
   * @param samePlaintextProof The same plaintext multi proof.
   * @param amountLinkageProof The amount linkage proof.
   * @param balanceLinkageProof The balance linkage proof.
   *
   * @return The combined ZK proof as a byte array.
   *
   * @throws NullPointerException if any parameter is null.
   */
  public static byte[] combineSendProofs(
    SamePlaintextMultiProof samePlaintextProof,
    ElGamalPedersenLinkProof amountLinkageProof,
    ElGamalPedersenLinkProof balanceLinkageProof
  ) {
    Objects.requireNonNull(samePlaintextProof, "samePlaintextProof must not be null");
    Objects.requireNonNull(amountLinkageProof, "amountLinkageProof must not be null");
    Objects.requireNonNull(balanceLinkageProof, "balanceLinkageProof must not be null");

    byte[] samePlaintextBytes = samePlaintextProof.toBytes();
    byte[] amountLinkageBytes = amountLinkageProof.toBytes();
    byte[] balanceLinkageBytes = balanceLinkageProof.toBytes();

    byte[] combined = new byte[samePlaintextBytes.length + amountLinkageBytes.length + balanceLinkageBytes.length];

    System.arraycopy(samePlaintextBytes, 0, combined, 0, samePlaintextBytes.length);
    System.arraycopy(amountLinkageBytes, 0, combined, samePlaintextBytes.length, amountLinkageBytes.length);
    System.arraycopy(balanceLinkageBytes, 0, combined, samePlaintextBytes.length + amountLinkageBytes.length,
      balanceLinkageBytes.length);

    return combined;
  }

  /**
   * Combines the three ZK proofs required for a ConfidentialMPTSend transaction and returns the result as a hex string.
   *
   * @param samePlaintextProof The same plaintext multi proof.
   * @param amountLinkageProof The amount linkage proof.
   * @param balanceLinkageProof The balance linkage proof.
   *
   * @return The combined ZK proof as an uppercase hex string.
   *
   * @throws NullPointerException if any parameter is null.
   *
   * @see #combineSendProofs(SamePlaintextMultiProof, ElGamalPedersenLinkProof, ElGamalPedersenLinkProof)
   */
  public static String combineSendProofsHex(
    SamePlaintextMultiProof samePlaintextProof,
    ElGamalPedersenLinkProof amountLinkageProof,
    ElGamalPedersenLinkProof balanceLinkageProof
  ) {
    return BaseEncoding.base16().encode(combineSendProofs(samePlaintextProof, amountLinkageProof, balanceLinkageProof));
  }
}

