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
import org.xrpl.xrpl4j.crypto.mpt.tmp.BulletproofRangeProof;
import org.xrpl.xrpl4j.crypto.mpt.tmp.ElGamalPedersenLinkProof;
import org.xrpl.xrpl4j.crypto.mpt.tmp.SamePlaintextMultiProof;

import java.util.Objects;

/**
 * Utility class for combining Zero-Knowledge proofs used in Confidential MPT transactions.
 *
 * <p>For ConfidentialMPTSend transactions, the ZK proof consists of four components:
 * <ul>
 *   <li>SamePlaintextMultiProof (359 bytes for 3 participants) - proves all ciphertexts encrypt the same amount</li>
 *   <li>Amount Linkage Proof (195 bytes) - proves the amount ciphertext matches the Pedersen commitment</li>
 *   <li>Balance Linkage Proof (195 bytes) - proves the balance ciphertext matches the Pedersen commitment</li>
 *   <li>Bulletproof Range Proof (754 bytes) - proves amount and remaining balance are non-negative</li>
 * </ul>
 * Total: 1503 bytes for a standard 3-participant send.</p>
 *
 * <p>For ConfidentialMPTConvertBack transactions, the ZK proof consists of two components:
 * <ul>
 *   <li>Balance Linkage Proof (195 bytes) - proves the balance ciphertext matches the Pedersen commitment</li>
 *   <li>Bulletproof Range Proof (688 bytes) - proves the remaining balance is non-negative</li>
 * </ul>
 * Total: 883 bytes.</p>
 */
public final class ZKProofUtils {
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

  /**
   * Combines the four ZK proofs required for a ConfidentialMPTSend transaction into a single byte array.
   *
   * <p>The proofs are concatenated in the following order:
   * <ol>
   *   <li>SamePlaintextMultiProof - proves all ciphertexts encrypt the same amount</li>
   *   <li>Amount Linkage Proof - proves the sender's ciphertext matches the amount commitment</li>
   *   <li>Balance Linkage Proof - proves the sender's balance ciphertext matches the balance commitment</li>
   *   <li>Bulletproof Range Proof - proves amount and remaining balance are non-negative</li>
   * </ol>
   *
   * @param samePlaintextProof  The same plaintext multi proof.
   * @param amountLinkageProof  The amount linkage proof.
   * @param balanceLinkageProof The balance linkage proof.
   * @param bulletproof         The bulletproof range proof for amount and remaining balance.
   *
   * @return The combined ZK proof as a byte array.
   *
   * @throws NullPointerException if any parameter is null.
   */
  public static byte[] combineSendProofsWithBulletproof(
    SamePlaintextMultiProof samePlaintextProof,
    ElGamalPedersenLinkProof amountLinkageProof,
    ElGamalPedersenLinkProof balanceLinkageProof,
    BulletproofRangeProof bulletproof
  ) {
    Objects.requireNonNull(samePlaintextProof, "samePlaintextProof must not be null");
    Objects.requireNonNull(amountLinkageProof, "amountLinkageProof must not be null");
    Objects.requireNonNull(balanceLinkageProof, "balanceLinkageProof must not be null");
    Objects.requireNonNull(bulletproof, "bulletproof must not be null");

    byte[] samePlaintextBytes = samePlaintextProof.toBytes();
    byte[] amountLinkageBytes = amountLinkageProof.toBytes();
    byte[] balanceLinkageBytes = balanceLinkageProof.toBytes();
    byte[] bulletproofBytes = bulletproof.toBytes();

    byte[] combined = new byte[samePlaintextBytes.length + amountLinkageBytes.length
      + balanceLinkageBytes.length + bulletproofBytes.length];

    int offset = 0;
    System.arraycopy(samePlaintextBytes, 0, combined, offset, samePlaintextBytes.length);
    offset += samePlaintextBytes.length;
    System.arraycopy(amountLinkageBytes, 0, combined, offset, amountLinkageBytes.length);
    offset += amountLinkageBytes.length;
    System.arraycopy(balanceLinkageBytes, 0, combined, offset, balanceLinkageBytes.length);
    offset += balanceLinkageBytes.length;
    System.arraycopy(bulletproofBytes, 0, combined, offset, bulletproofBytes.length);

    return combined;
  }

  /**
   * Combines the four ZK proofs required for a ConfidentialMPTSend transaction and returns the result as a hex string.
   *
   * @param samePlaintextProof  The same plaintext multi proof.
   * @param amountLinkageProof  The amount linkage proof.
   * @param balanceLinkageProof The balance linkage proof.
   * @param bulletproof         The bulletproof range proof for amount and remaining balance.
   *
   * @return The combined ZK proof as an uppercase hex string.
   *
   * @throws NullPointerException if any parameter is null.
   *
   * @see #combineSendProofsWithBulletproof(SamePlaintextMultiProof, ElGamalPedersenLinkProof, ElGamalPedersenLinkProof,
   *   BulletproofRangeProof)
   */
  public static String combineSendProofsWithBulletproofHex(
    SamePlaintextMultiProof samePlaintextProof,
    ElGamalPedersenLinkProof amountLinkageProof,
    ElGamalPedersenLinkProof balanceLinkageProof,
    BulletproofRangeProof bulletproof
  ) {
    return BaseEncoding.base16().encode(
      combineSendProofsWithBulletproof(samePlaintextProof, amountLinkageProof, balanceLinkageProof, bulletproof)
    );
  }

  /**
   * Combines the two ZK proofs required for a ConfidentialMPTConvertBack transaction into a single byte array.
   *
   * <p>The proofs are concatenated in the following order:
   * <ol>
   *   <li>Balance Linkage Proof (195 bytes) - proves the balance ciphertext matches the Pedersen commitment</li>
   *   <li>Bulletproof Range Proof (688 bytes) - proves the remaining balance is non-negative</li>
   * </ol>
   *
   * @param balanceLinkageProof The balance linkage proof.
   * @param bulletproof         The bulletproof range proof for the remaining balance.
   *
   * @return The combined ZK proof as a byte array.
   *
   * @throws NullPointerException if any parameter is null.
   */
  public static byte[] combineConvertBackProofs(
    ElGamalPedersenLinkProof balanceLinkageProof,
    BulletproofRangeProof bulletproof
  ) {
    Objects.requireNonNull(balanceLinkageProof, "balanceLinkageProof must not be null");
    Objects.requireNonNull(bulletproof, "bulletproof must not be null");

    byte[] balanceLinkageBytes = balanceLinkageProof.toBytes();
    byte[] bulletproofBytes = bulletproof.toBytes();

    byte[] combined = new byte[balanceLinkageBytes.length + bulletproofBytes.length];

    System.arraycopy(balanceLinkageBytes, 0, combined, 0, balanceLinkageBytes.length);
    System.arraycopy(bulletproofBytes, 0, combined, balanceLinkageBytes.length, bulletproofBytes.length);

    return combined;
  }

  /**
   * Combines the two ZK proofs required for a ConfidentialMPTConvertBack transaction and returns the result as a hex
   * string.
   *
   * @param balanceLinkageProof The balance linkage proof.
   * @param bulletproof         The bulletproof range proof for the remaining balance.
   *
   * @return The combined ZK proof as an uppercase hex string.
   *
   * @throws NullPointerException if any parameter is null.
   *
   * @see #combineConvertBackProofs(ElGamalPedersenLinkProof, BulletproofRangeProof)
   */
  public static String combineConvertBackProofsHex(
    ElGamalPedersenLinkProof balanceLinkageProof,
    BulletproofRangeProof bulletproof
  ) {
    return BaseEncoding.base16().encode(combineConvertBackProofs(balanceLinkageProof, bulletproof));
  }
}

