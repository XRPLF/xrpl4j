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
   * Generates a compact Schnorr Proof of Knowledge for a Confidential MPT conversion.
   *
   * @param pubkey   The 33-byte compressed secp256k1 public key.
   * @param privkey  The 32-byte private key.
   * @param ctxHash  The 32-byte context hash binding the proof to a transaction.
   * @param outProof A 64-byte buffer to receive the compact Schnorr proof.
   *
   * @return 0 on success, -1 on failure.
   */
  int generateConvertProof(byte[] pubkey, byte[] privkey, byte[] ctxHash, byte[] outProof);

  /**
   * Verifies a compact Schnorr Proof of Knowledge for a Confidential MPT conversion.
   *
   * @param proof   The 64-byte compact Schnorr proof.
   * @param pubkey  The 33-byte compressed secp256k1 public key.
   * @param ctxHash The 32-byte context hash.
   *
   * @return 0 on success (valid), -1 on failure (invalid).
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
   * Generates the compact send proof for a ConfidentialMptSend transaction.
   *
   * <p>Produces a compact AND-composed sigma proof (192 bytes) + aggregated Bulletproof (754 bytes).
   * Total proof size is fixed at 946 bytes.</p>
   *
   * @param privkey              The sender's 32-byte private key.
   * @param pubkey               The sender's 33-byte public key.
   * @param amount               The amount being sent.
   * @param recipientPubkeys     Flat array of participant public keys (n * 33 bytes).
   * @param recipientCiphertexts Flat array of participant encrypted amounts (n * 66 bytes).
   * @param numRecipients        Number of participants (3 or 4).
   * @param txBlindingFactor     The 32-byte ElGamal randomness r (also blinding factor for pc_m).
   * @param contextHash          The 32-byte context hash.
   * @param amountCommitment     The 33-byte Pedersen commitment pc_m = m*G + r*H.
   * @param balanceCommitment    The 33-byte Pedersen commitment for the balance.
   * @param balanceValue         The balance value.
   * @param balanceCiphertext    The 66-byte encrypted balance.
   * @param balanceBlinding      The 32-byte blinding factor for the balance.
   * @param outProof             Buffer to receive the 946-byte proof.
   * @param outLen               1-element array: input is buffer size, output is actual proof size.
   *
   * @return 0 on success, -1 on failure.
   */
  int generateSendProof(
    byte[] privkey, byte[] pubkey, long amount,
    byte[] recipientPubkeys, byte[] recipientCiphertexts, int numRecipients,
    byte[] txBlindingFactor, byte[] contextHash,
    byte[] amountCommitment,
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
   * @param outProof            An 816-byte buffer to receive the proof.
   *
   * @return 0 on success, -1 on failure.
   */
  int generateConvertBackProof(
    byte[] privkey, byte[] pubkey, byte[] ctxHash, long amount,
    byte[] balanceCommitment, long balanceValue, byte[] balanceCiphertext, byte[] balanceBlinding,
    byte[] outProof
  );

  /**
   * Generates a compact sigma proof for a ConfidentialMptClawback transaction.
   *
   * @param privkey         The issuer's 32-byte private key.
   * @param pubkey          The issuer's 33-byte public key.
   * @param ctxHash         The 32-byte context hash.
   * @param amount          The amount to claw back.
   * @param encryptedAmount The 66-byte issuer encrypted balance from the ledger.
   * @param outProof        A 64-byte buffer to receive the compact sigma proof.
   *
   * @return 0 on success, -1 on failure.
   */
  int generateClawbackProof(
    byte[] privkey, byte[] pubkey, byte[] ctxHash, long amount, byte[] encryptedAmount, byte[] outProof
  );

  /**
   * Verifies a compact sigma proof for a ConfidentialMptClawback transaction.
   *
   * @param proof           The 64-byte compact sigma proof.
   * @param c1              The 33-byte C1 component of the encrypted balance.
   * @param c2              The 33-byte C2 component of the encrypted balance.
   * @param pubkey          The 33-byte issuer public key.
   * @param amount          The clawback amount.
   * @param ctxHash         The 32-byte context hash.
   *
   * @return 0 on success (valid), -1 on failure (invalid).
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
   * @param proof               The 816-byte proof.
   *
   * @return 0 on success (valid), non-zero on failure.
   */
  int verifyConvertBackProof(
    byte[] pubkey, byte[] ctxHash, long amount,
    byte[] encryptedBalance, byte[] balanceCommitment,
    byte[] proof
  );

  /**
   * Verifies the compact send proof for a ConfidentialMptSend transaction.
   * Proof size is fixed at 946 bytes.
   *
   * @param recipientPubkeys        Flat array of participant public keys (n * 33 bytes).
   * @param recipientCiphertexts    Flat array of participant encrypted amounts (n * 66 bytes).
   * @param numRecipients           Number of participants (3 or 4).
   * @param senderSpendingCiphertext The sender's 66-byte encrypted balance ciphertext.
   * @param ctxHash                 The 32-byte context hash.
   * @param amountCommitment        The 33-byte Pedersen commitment for the amount.
   * @param balanceCommitment       The 33-byte Pedersen commitment for the balance.
   * @param proof                   The 946-byte proof.
   *
   * @return 0 on success (valid), non-zero on failure.
   */
  int verifySendProof(
    byte[] recipientPubkeys, byte[] recipientCiphertexts, int numRecipients,
    byte[] senderSpendingCiphertext,
    byte[] ctxHash, byte[] amountCommitment, byte[] balanceCommitment,
    byte[] proof
  );

  // =========================================================================
  // Context Hash Generation
  // =========================================================================

  /**
   * Generates the context hash for a ConfidentialMPTConvert transaction.
   *
   * @param account    The 20-byte account ID.
   * @param issuanceId The 24-byte MPTokenIssuance ID.
   * @param sequence   The transaction sequence number.
   * @param outHash    A 32-byte buffer to receive the hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int generateConvertContextHash(byte[] account, byte[] issuanceId, int sequence, byte[] outHash);

  /**
   * Generates the context hash for a ConfidentialMPTConvertBack transaction.
   *
   * @param account    The 20-byte account ID.
   * @param issuanceId The 24-byte MPTokenIssuance ID.
   * @param sequence   The transaction sequence number.
   * @param version    The confidential balance version.
   * @param outHash    A 32-byte buffer to receive the hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int generateConvertBackContextHash(byte[] account, byte[] issuanceId, int sequence, int version, byte[] outHash);

  /**
   * Generates the context hash for a ConfidentialMPTSend transaction.
   *
   * @param account     The 20-byte sender account ID.
   * @param issuanceId  The 24-byte MPTokenIssuance ID.
   * @param sequence    The transaction sequence number.
   * @param destination The 20-byte destination account ID.
   * @param version     The confidential balance version.
   * @param outHash     A 32-byte buffer to receive the hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int generateSendContextHash(
    byte[] account, byte[] issuanceId, int sequence, byte[] destination, int version, byte[] outHash
  );

  /**
   * Generates the context hash for a ConfidentialMPTClawback transaction.
   *
   * @param account    The 20-byte issuer account ID.
   * @param issuanceId The 24-byte MPTokenIssuance ID.
   * @param sequence   The transaction sequence number.
   * @param holder     The 20-byte holder account ID.
   * @param outHash    A 32-byte buffer to receive the hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int generateClawbackContextHash(byte[] account, byte[] issuanceId, int sequence, byte[] holder, byte[] outHash);
}
