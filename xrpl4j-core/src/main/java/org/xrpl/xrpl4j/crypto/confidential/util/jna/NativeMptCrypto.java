package org.xrpl.xrpl4j.crypto.confidential.util.jna;

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

/**
 * Pure Java bridge interface for the mpt-crypto native library.
 *
 * <p>This interface defines the contract for native cryptographic operations without depending on JNA.
 * The JNA-based implementation ({@code JnaNativeMptCrypto}) lives in the {@code xrpl4j-mpt-crypto} module
 * and is loaded via reflection when {@code xrpl4j-mpt-crypto} is on the classpath.</p>
 *
 * <p>This design allows {@code xrpl4j-core} to contain the encryptor/decryptor logic without
 * a compile-time dependency on JNA or the native library.</p>
 */
public interface NativeMptCrypto {

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
  int encryptAmount(long amount, byte[] pubkey, byte[] blindingFactor, byte[] outCiphertext);

  /**
   * Decrypts an ElGamal ciphertext to recover the original amount.
   *
   * @param ciphertext The 66-byte ciphertext buffer (C1 || C2).
   * @param privkey    The 32-byte private key.
   * @param outAmount  A 1-element {@code long[]} to receive the decrypted amount.
   *
   * @return 0 on success, -1 on failure.
   */
  int decryptAmount(byte[] ciphertext, byte[] privkey, long[] outAmount);

  /**
   * Generates a 32-byte random blinding factor suitable for ElGamal encryption.
   *
   * @param outFactor A 32-byte buffer to receive the blinding factor.
   *
   * @return 0 on success, -1 on failure.
   */
  int generateBlindingFactor(byte[] outFactor);

  /**
   * Generates a Schnorr Proof of Knowledge for a Confidential MPT conversion.
   *
   * @param pubkey   The 33-byte compressed secp256k1 public key.
   * @param privkey  The 32-byte private key.
   * @param ctxHash  The 32-byte context hash binding the proof to a transaction.
   * @param outProof A 65-byte buffer to receive the proof (T || s).
   *
   * @return 0 on success, -1 on failure.
   */
  int generateConvertProof(byte[] pubkey, byte[] privkey, byte[] ctxHash, byte[] outProof);

  /**
   * Verifies a Schnorr Proof of Knowledge for a Confidential MPT conversion.
   *
   * @param proof   The 65-byte proof (T || s).
   * @param pubkey  The 33-byte compressed secp256k1 public key.
   * @param ctxHash The 32-byte context hash.
   *
   * @return 1 if the proof is valid, 0 otherwise.
   */
  int verifyConvertProof(byte[] proof, byte[] pubkey, byte[] ctxHash);

  /**
   * Generates a Pedersen Commitment: C = amount * G + blindingFactor * H.
   *
   * @param amount          The value to commit to.
   * @param blindingFactor  The 32-byte blinding factor (rho).
   * @param outCommitment   A 33-byte buffer to receive the compressed commitment.
   *
   * @return 0 on success, -1 on failure.
   */
  int generatePedersenCommitment(long amount, byte[] blindingFactor, byte[] outCommitment);

  /**
   * Generates the combined proof for a ConfidentialMptSend transaction.
   *
   * <p>The proof includes same-plaintext, amount linkage, balance linkage, and range proofs.</p>
   *
   * @param privkey              The sender's 32-byte private key.
   * @param amount               The amount being sent.
   * @param recipientPubkeys     Flat array of recipient public keys (n * 33 bytes).
   * @param recipientCiphertexts Flat array of recipient encrypted amounts (n * 66 bytes).
   * @param numRecipients        Number of recipients.
   * @param txBlindingFactor     The 32-byte blinding factor used for encryption.
   * @param contextHash          The 32-byte context hash.
   * @param amountCommitment     The 33-byte Pedersen commitment for the amount.
   * @param amountValue          The amount value in the amount params.
   * @param amountCiphertext     The 66-byte encrypted amount in the amount params.
   * @param amountBlinding       The 32-byte blinding factor in the amount params.
   * @param balanceCommitment    The 33-byte Pedersen commitment for the balance.
   * @param balanceValue         The balance value in the balance params.
   * @param balanceCiphertext    The 66-byte encrypted amount in the balance params.
   * @param balanceBlinding      The 32-byte blinding factor in the balance params.
   * @param outProof             Buffer to receive the proof bytes.
   * @param outLen               1-element array: input is buffer size, output is actual proof size.
   *
   * @return 0 on success, -1 on failure.
   */
  int generateSendProof(
    byte[] privkey, long amount,
    byte[] recipientPubkeys, byte[] recipientCiphertexts, int numRecipients,
    byte[] txBlindingFactor, byte[] contextHash,
    byte[] amountCommitment, long amountValue, byte[] amountCiphertext, byte[] amountBlinding,
    byte[] balanceCommitment, long balanceValue, byte[] balanceCiphertext, byte[] balanceBlinding,
    byte[] outProof, int[] outLen
  );

  /**
   * Generates the proof for a ConfidentialMptConvertBack transaction.
   *
   * @param privkey             The holder's 32-byte private key.
   * @param pubkey              The holder's 33-byte public key.
   * @param ctxHash             The 32-byte context hash.
   * @param amount              The amount to convert back.
   * @param balanceCommitment   The 33-byte Pedersen commitment for the balance.
   * @param balanceValue        The balance value.
   * @param balanceCiphertext   The 66-byte encrypted balance.
   * @param balanceBlinding     The 32-byte blinding factor for the balance.
   * @param outProof            An 883-byte buffer to receive the proof.
   *
   * @return 0 on success, -1 on failure.
   */
  int generateConvertBackProof(
    byte[] privkey, byte[] pubkey, byte[] ctxHash, long amount,
    byte[] balanceCommitment, long balanceValue, byte[] balanceCiphertext, byte[] balanceBlinding,
    byte[] outProof
  );

  /**
   * Generates an equality proof for a ConfidentialMptClawback transaction.
   *
   * @param privkey         The issuer's 32-byte private key.
   * @param pubkey          The issuer's 33-byte public key.
   * @param ctxHash         The 32-byte context hash.
   * @param amount          The amount to claw back.
   * @param encryptedAmount The 66-byte issuer encrypted balance from the ledger.
   * @param outProof        A 98-byte buffer to receive the equality proof.
   *
   * @return 0 on success, -1 on failure.
   */
  int generateClawbackProof(
    byte[] privkey, byte[] pubkey, byte[] ctxHash, long amount, byte[] encryptedAmount, byte[] outProof
  );

  /**
   * Verifies an equality proof for a ConfidentialMptClawback transaction.
   *
   * @param proof           The 98-byte equality proof.
   * @param c1              The 33-byte C1 component of the encrypted balance.
   * @param c2              The 33-byte C2 component of the encrypted balance.
   * @param pubkey          The 33-byte issuer public key.
   * @param amount          The clawback amount.
   * @param ctxHash         The 32-byte context hash.
   *
   * @return 1 if the proof is valid, 0 otherwise.
   */
  int verifyClawbackProof(byte[] proof, byte[] c1, byte[] c2, byte[] pubkey, long amount, byte[] ctxHash);

  /**
   * Verifies the proof for a ConfidentialMptConvertBack transaction.
   *
   * @param pubkey              The holder's 33-byte public key.
   * @param ctxHash             The 32-byte context hash.
   * @param amount              The amount converted back.
   * @param encryptedBalance    The 66-byte encrypted balance from the ledger.
   * @param balanceCommitment   The 33-byte Pedersen commitment for the balance.
   * @param proof               The 883-byte proof.
   *
   * @return 0 on success (valid), non-zero on failure.
   */
  int verifyConvertBackProof(
    byte[] pubkey, byte[] ctxHash, long amount,
    byte[] encryptedBalance, byte[] balanceCommitment,
    byte[] proof
  );

  /**
   * Verifies the combined proof for a ConfidentialMptSend transaction.
   *
   * @param recipientPubkeys     Flat array of recipient public keys (n * 33 bytes).
   * @param recipientCiphertexts Flat array of recipient encrypted amounts (n * 66 bytes).
   * @param numRecipients        Number of recipients.
   * @param ctxHash              The 32-byte context hash.
   * @param amountCommitment     The 33-byte Pedersen commitment for the amount.
   * @param balanceCommitment    The 33-byte Pedersen commitment for the balance.
   * @param proof                The proof bytes.
   * @param proofLen             The proof length.
   *
   * @return 0 on success (valid), non-zero on failure.
   */
  int verifySendProof(
    byte[] recipientPubkeys, byte[] recipientCiphertexts, int numRecipients,
    byte[] senderSpendingCiphertext,
    byte[] ctxHash, byte[] amountCommitment, byte[] balanceCommitment,
    byte[] proof, int proofLen
  );
}
