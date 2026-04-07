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
   * Generates a Schnorr Proof of Knowledge for a Confidential MPT conversion.
   *
   * @param pubkey   The 33-byte compressed secp256k1 public key.
   * @param privkey  The 32-byte private key.
   * @param ctxHash  The 32-byte context hash.
   * @param outProof A 65-byte buffer to receive the proof (T || s).
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_convert_proof(byte[] pubkey, byte[] privkey, byte[] ctxHash, byte[] outProof);

  // mpt_verify_convert_proof(proof[65], pubkey[33], context_hash[32])
  int mpt_verify_convert_proof(byte[] proof, byte[] pubkey, byte[] contextHash);

  // mpt_verify_convert_back_proof(proof[883], pubkey[33], ciphertext[66],
  //     balance_commitment[33], amount, context_hash[32])
  int mpt_verify_convert_back_proof(
    byte[] proof, byte[] pubkey, byte[] ciphertext,
    byte[] balanceCommitment, long amount, byte[] contextHash
  );

  // mpt_verify_send_proof(proof*, proof_len, participants*, n_participants,
  //     sender_spending_ciphertext[66], amount_commitment[33],
  //     balance_commitment[33], context_hash[32])
  int mpt_verify_send_proof(
    byte[] proof, long proofLen,
    MptConfidentialRecipient participants, byte numParticipants,
    byte[] senderSpendingCiphertext, byte[] amountCommitment,
    byte[] balanceCommitment, byte[] contextHash
  );

  // mpt_verify_clawback_proof(proof[98], amount, pubkey[33], ciphertext[66], context_hash[32])
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
   * Returns the total proof size for a ConfidentialMptSend with the given number of recipients.
   *
   * @param numRecipients The number of recipients.
   *
   * @return The expected proof size in bytes.
   */
  long get_confidential_send_proof_size(long numRecipients);

  /**
   * Generates the combined proof for a ConfidentialMptSend transaction.
   *
   * @param priv              The sender's 32-byte private key.
   * @param amount            The amount being sent.
   * @param recipients        Pointer to array of mpt_confidential_recipient structs.
   * @param numRecipients     Number of recipients.
   * @param txBlindingFactor  The 32-byte blinding factor.
   * @param contextHash       The 32-byte context hash.
   * @param amountParams      Pointer to mpt_pedersen_proof_params struct for amount.
   * @param balanceParams     Pointer to mpt_pedersen_proof_params struct for balance.
   * @param outProof          Buffer to receive the proof.
   * @param outLen            Pointer to size (in: buffer size, out: actual size).
   *
   * @return 0 on success, -1 on failure.
   */
  /**
   * Generates the proof for a ConfidentialMptConvertBack transaction.
   *
   * @param priv        The holder's 32-byte private key.
   * @param pub         The holder's 33-byte public key.
   * @param contextHash The 32-byte context hash.
   * @param amount      The amount to convert back.
   * @param params      The Pedersen proof params for the balance.
   * @param outProof    An 883-byte buffer to receive the proof.
   *
   * @return 0 on success, -1 on failure.
   */
  /**
   * Generates an equality proof for a ConfidentialMptClawback transaction.
   */
  int mpt_get_clawback_proof(
    byte[] priv, byte[] pub, byte[] contextHash, long amount, byte[] encryptedAmount, byte[] outProof
  );

  int mpt_get_convert_back_proof(
    byte[] priv, byte[] pub, byte[] contextHash, long amount,
    MptPedersenProofParams params, byte[] outProof
  );

  int mpt_get_confidential_send_proof(
    byte[] priv, long amount,
    MptConfidentialRecipient recipients, long numRecipients,
    byte[] txBlindingFactor, byte[] contextHash,
    MptPedersenProofParams amountParams, MptPedersenProofParams balanceParams,
    byte[] outProof, long[] outLen
  );
}
