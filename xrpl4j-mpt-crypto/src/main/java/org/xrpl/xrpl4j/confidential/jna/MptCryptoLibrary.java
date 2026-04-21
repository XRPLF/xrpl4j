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

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * JNA binding interface for the mpt-crypto native library.
 *
 * <p>This interface maps to the C functions declared in {@code mpt_utility.h} from the mpt-crypto library.
 * The native library provides ElGamal encryption, decryption, and zero-knowledge proof generation
 * for Confidential MPT transactions on the XRP Ledger.</p>
 *
 * <p>The native library is loaded from the classpath at {@code <platform>/libmptcrypto.dylib} (macOS)
 * or {@code <platform>/libmptcrypto.so} (Linux), following JNA's standard platform-specific
 * resource loading convention.</p>
 */
public interface MptCryptoLibrary extends Library {

  /**
   * Singleton instance of the native library, loaded via JNA.
   */
  MptCryptoLibrary INSTANCE = Native.load("mptcrypto", MptCryptoLibrary.class);

  /**
   * Encrypts a {@code uint64} amount using ElGamal encryption with a secp256k1 public key.
   *
   * @param amount          The integer value to encrypt.
   * @param pubkey          The 33-byte compressed secp256k1 public key.
   * @param blindingFactor  The 32-byte random blinding factor (scalar r).
   * @param outCiphertext   A 66-byte buffer to receive the resulting ciphertext (C1 || C2).
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_encrypt_amount(long amount, byte[] pubkey, byte[] blindingFactor, byte[] outCiphertext);

  /**
   * Decrypts an ElGamal ciphertext to recover the original amount.
   *
   * @param ciphertext The 66-byte ciphertext buffer (C1 || C2).
   * @param privkey    The 32-byte private key.
   * @param outAmount  A 1-element {@code long[]} to receive the decrypted amount.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_decrypt_amount(byte[] ciphertext, byte[] privkey, long[] outAmount);


  /**
   * Generates a 32-byte random blinding factor suitable for ElGamal encryption.
   *
   * @param outFactor A 32-byte buffer to receive the blinding factor.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_generate_blinding_factor(byte[] outFactor);

  /**
   * Generates a compact Schnorr Proof of Knowledge for a Confidential MPT conversion.
   *
   * @param pubkey   The 33-byte compressed secp256k1 public key.
   * @param privkey  The 32-byte private key.
   * @param ctxHash  The 32-byte context hash.
   * @param outProof A 64-byte buffer to receive the compact Schnorr proof.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_convert_proof(byte[] pubkey, byte[] privkey, byte[] ctxHash, byte[] outProof);

  /**
   * Verifies a compact Schnorr Proof of Knowledge for a Confidential MPT conversion.
   *
   * @param proof       The 64-byte compact Schnorr proof.
   * @param pubkey      The 33-byte compressed secp256k1 public key.
   * @param contextHash The 32-byte context hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_verify_convert_proof(byte[] proof, byte[] pubkey, byte[] contextHash);

  /**
   * Verifies the proof for a ConfidentialMptConvertBack transaction.
   * Proof size is 816 bytes (128 compact sigma + 688 single bulletproof).
   *
   * @param proof             The 816-byte proof blob.
   * @param pubkey            The 33-byte holder public key.
   * @param ciphertext        The 66-byte holder balance ciphertext.
   * @param balanceCommitment The 33-byte Pedersen commitment to the balance.
   * @param amount            The publicly revealed conversion amount.
   * @param contextHash       The 32-byte context hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_verify_convert_back_proof(
    byte[] proof, byte[] pubkey, byte[] ciphertext,
    byte[] balanceCommitment, long amount, byte[] contextHash
  );

  /**
   * Verifies the combined proof for a ConfidentialMptSend transaction.
   * Proof size is fixed at 946 bytes (192 compact sigma + 754 double bulletproof).
   *
   * @param proof                      The 946-byte proof blob.
   * @param participants               Pointer to array of mpt_confidential_participant structs.
   * @param numParticipants            Number of participants (3 or 4).
   * @param senderSpendingCiphertext   The sender's 66-byte on-ledger balance ciphertext.
   * @param amountCommitment           The 33-byte Pedersen commitment to the transfer amount.
   * @param balanceCommitment          The 33-byte Pedersen commitment to the sender's balance.
   * @param contextHash                The 32-byte context hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_verify_send_proof(
    byte[] proof,
    MptConfidentialRecipient participants, byte numParticipants,
    byte[] senderSpendingCiphertext, byte[] amountCommitment,
    byte[] balanceCommitment, byte[] contextHash
  );

  /**
   * Verifies a compact sigma proof for a ConfidentialMptClawback transaction.
   *
   * @param proof       The 64-byte compact sigma proof.
   * @param amount      The publicly known amount being clawed back.
   * @param pubkey      The 33-byte issuer public key.
   * @param ciphertext  The 66-byte sfIssuerEncryptedBalance from the ledger.
   * @param contextHash The 32-byte context hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_verify_clawback_proof(byte[] proof, long amount, byte[] pubkey, byte[] ciphertext, byte[] contextHash);

  /**
   * Generates a Pedersen Commitment: C = amount * G + blindingFactor * H.
   *
   * @param amount          The value to commit to.
   * @param blindingFactor  The 32-byte blinding factor.
   * @param outCommitment   A 33-byte buffer to receive the compressed commitment.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_pedersen_commitment(long amount, byte[] blindingFactor, byte[] outCommitment);

  /**
   * Generates the proof for a ConfidentialMptConvertBack transaction.
   *
   * @param priv        The holder's 32-byte private key.
   * @param pub         The holder's 33-byte public key.
   * @param contextHash The 32-byte context hash.
   * @param amount      The amount to convert back.
   * @param params      The Pedersen proof params for the balance.
   * @param outProof    Buffer to receive the proof.
   *
   * @return 0 on success, -1 on failure.
   */
  /**
   * Generates a compact sigma proof for a ConfidentialMptClawback transaction.
   *
   * @param priv            The issuer's 32-byte private key.
   * @param pub             The issuer's 33-byte public key.
   * @param contextHash     The 32-byte context hash.
   * @param amount          The amount to claw back.
   * @param encryptedAmount The 66-byte sfIssuerEncryptedBalance from the ledger.
   * @param outProof        A 64-byte buffer to receive the compact sigma proof.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_clawback_proof(
    byte[] priv, byte[] pub, byte[] contextHash, long amount, byte[] encryptedAmount, byte[] outProof
  );

  int mpt_get_convert_back_proof(
    byte[] priv, byte[] pub, byte[] contextHash, long amount,
    MptPedersenProofParams params, byte[] outProof
  );

  /**
   * Generates a compact AND-composed sigma proof + aggregated Bulletproof for ConfidentialMptSend.
   * Total proof size is fixed at 946 bytes.
   *
   * @param priv              The sender's 32-byte private key.
   * @param pub               The sender's 33-byte public key.
   * @param amount            The amount being sent.
   * @param recipients        Pointer to array of mpt_confidential_participant structs.
   * @param numRecipients     Number of participants (3 or 4).
   * @param txBlindingFactor  The 32-byte ElGamal randomness r (also blinding factor for pc_m).
   * @param contextHash       The 32-byte context hash.
   * @param amountCommitment  The 33-byte Pedersen commitment pc_m = m*G + r*H.
   * @param balanceParams     Pointer to mpt_pedersen_proof_params for balance.
   * @param outProof          Buffer to receive the 946-byte proof.
   * @param outLen            Pointer to size (in: capacity, out: bytes written).
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_confidential_send_proof(
    byte[] priv, byte[] pub, long amount,
    MptConfidentialRecipient recipients, long numRecipients,
    byte[] txBlindingFactor, byte[] contextHash,
    byte[] amountCommitment, MptPedersenProofParams balanceParams,
    byte[] outProof, long[] outLen
  );

  // =========================================================================
  // Context Hash Generation
  // =========================================================================

  /**
   * Generates the context hash for a ConfidentialMPTConvert transaction.
   *
   * @param account  The 20-byte account ID (passed by value).
   * @param iss      The 24-byte MPTokenIssuance ID (passed by value).
   * @param sequence The transaction sequence number.
   * @param outHash  A 32-byte buffer to receive the SHA512Half hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_convert_context_hash(MptAccountId account, MptIssuanceId iss, int sequence, byte[] outHash);

  /**
   * Generates the context hash for a ConfidentialMPTConvertBack transaction.
   *
   * @param acc     The 20-byte account ID (passed by value).
   * @param iss     The 24-byte MPTokenIssuance ID (passed by value).
   * @param seq     The transaction sequence number.
   * @param ver     The confidential balance version.
   * @param outHash A 32-byte buffer to receive the SHA512Half hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_convert_back_context_hash(MptAccountId acc, MptIssuanceId iss, int seq, int ver, byte[] outHash);

  /**
   * Generates the context hash for a ConfidentialMPTSend transaction.
   *
   * @param acc     The 20-byte sender account ID (passed by value).
   * @param iss     The 24-byte MPTokenIssuance ID (passed by value).
   * @param seq     The transaction sequence number.
   * @param dest    The 20-byte destination account ID (passed by value).
   * @param ver     The confidential balance version.
   * @param outHash A 32-byte buffer to receive the SHA512Half hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_send_context_hash(
    MptAccountId acc, MptIssuanceId iss, int seq, MptAccountId dest, int ver, byte[] outHash
  );

  /**
   * Generates the context hash for a ConfidentialMPTClawback transaction.
   *
   * @param acc     The 20-byte issuer account ID (passed by value).
   * @param iss     The 24-byte MPTokenIssuance ID (passed by value).
   * @param seq     The transaction sequence number.
   * @param holder  The 20-byte holder account ID (passed by value).
   * @param outHash A 32-byte buffer to receive the SHA512Half hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_clawback_context_hash(MptAccountId acc, MptIssuanceId iss, int seq, MptAccountId holder, byte[] outHash);
}
