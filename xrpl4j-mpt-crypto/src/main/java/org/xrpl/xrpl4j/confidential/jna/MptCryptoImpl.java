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

import org.xrpl.xrpl4j.crypto.confidential.util.jna.NativeMptCrypto;

/**
 * JNA-backed implementation of {@link NativeMptCrypto} that delegates to the native mpt-crypto
 * library via {@link MptCryptoLibrary}.
 *
 * <p>This class is loaded reflectively by {@code xrpl4j-core} when {@code xrpl4j-mpt-crypto}
 * is on the classpath. It must have a public no-arg constructor.</p>
 */
public class MptCryptoImpl implements NativeMptCrypto {

  private final MptCryptoLibrary lib;

  /**
   * Constructs a new instance using the default native library singleton.
   */
  public MptCryptoImpl() {
    this(MptCryptoLibrary.INSTANCE);
  }

  /**
   * Constructs a new instance with the specified native library binding.
   *
   * @param lib The JNA library binding to delegate to.
   */
  MptCryptoImpl(final MptCryptoLibrary lib) {
    this.lib = lib;
  }

  @Override
  public int encryptAmount(long amount, byte[] pubkey, byte[] blindingFactor, byte[] outCiphertext) {
    return lib.mpt_encrypt_amount(amount, pubkey, blindingFactor, outCiphertext);
  }

  @Override
  public int decryptAmount(byte[] ciphertext, byte[] privkey, long[] outAmount) {
    return lib.mpt_decrypt_amount(ciphertext, privkey, outAmount);
  }

  @Override
  public int generateBlindingFactor(byte[] outFactor) {
    return lib.mpt_generate_blinding_factor(outFactor);
  }

  @Override
  public int generatePedersenCommitment(long amount, byte[] blindingFactor, byte[] outCommitment) {
    return lib.mpt_get_pedersen_commitment(amount, blindingFactor, outCommitment);
  }

  @Override
  public int generateConvertProof(byte[] pubkey, byte[] privkey, byte[] ctxHash, byte[] outProof) {
    return lib.mpt_get_convert_proof(pubkey, privkey, ctxHash, outProof);
  }

  @Override
  public int verifyConvertProof(byte[] proof, byte[] pubkey, byte[] ctxHash) {
    return lib.mpt_verify_convert_proof(proof, pubkey, ctxHash);
  }

  @Override
  public int verifyClawbackProof(
    byte[] proof, byte[] c1, byte[] c2, byte[] pubkey, long amount, byte[] ctxHash
  ) {
    byte[] ciphertext = new byte[c1.length + c2.length];
    System.arraycopy(c1, 0, ciphertext, 0, c1.length);
    System.arraycopy(c2, 0, ciphertext, c1.length, c2.length);
    return lib.mpt_verify_clawback_proof(proof, amount, pubkey, ciphertext, ctxHash);
  }

  @Override
  public int generateClawbackProof(
    byte[] privkey, byte[] pubkey, byte[] ctxHash, long amount, byte[] encryptedAmount, byte[] outProof
  ) {
    return lib.mpt_get_clawback_proof(privkey, pubkey, ctxHash, amount, encryptedAmount, outProof);
  }

  @Override
  public int verifyConvertBackProof(
    byte[] pubkey, byte[] ctxHash, long amount,
    byte[] encryptedBalance, byte[] balanceCommitment,
    byte[] proof
  ) {
    return lib.mpt_verify_convert_back_proof(proof, pubkey, encryptedBalance, balanceCommitment, amount, ctxHash);
  }

  @Override
  public int verifySendProof(
    byte[] recipientPubkeys, byte[] recipientCiphertexts, int numRecipients,
    byte[] senderSpendingCiphertext,
    byte[] ctxHash, byte[] amountCommitment, byte[] balanceCommitment,
    byte[] proof
  ) {
    MptConfidentialRecipient[] recipients =
      (MptConfidentialRecipient[]) new MptConfidentialRecipient().toArray(numRecipients);
    for (int i = 0; i < numRecipients; i++) {
      System.arraycopy(recipientPubkeys, i * 33, recipients[i].pubkey, 0, 33);
      System.arraycopy(recipientCiphertexts, i * 66, recipients[i].ciphertext, 0, 66);
    }

    return lib.mpt_verify_send_proof(
      proof,
      recipients[0], (byte) numRecipients,
      senderSpendingCiphertext, amountCommitment,
      balanceCommitment, ctxHash
    );
  }

  @Override
  public int generateConvertBackProof(
    byte[] privkey, byte[] pubkey, byte[] ctxHash, long amount,
    byte[] balanceCommitment, long balanceValue, byte[] balanceCiphertext, byte[] balanceBlinding,
    byte[] outProof
  ) {
    MptPedersenProofParams params = newPedersenParams(
      balanceCommitment, balanceValue, balanceCiphertext, balanceBlinding
    );
    return lib.mpt_get_convert_back_proof(privkey, pubkey, ctxHash, amount, params, outProof);
  }

  @Override
  public int generateSendProof(
    byte[] privkey, byte[] pubkey, long amount,
    byte[] recipientPubkeys, byte[] recipientCiphertexts, int numRecipients,
    byte[] txBlindingFactor, byte[] contextHash,
    byte[] amountCommitment,
    byte[] balanceCommitment, long balanceValue, byte[] balanceCiphertext, byte[] balanceBlinding,
    byte[] outProof, int[] outLen
  ) {
    MptConfidentialRecipient[] recipients =
      (MptConfidentialRecipient[]) new MptConfidentialRecipient().toArray(numRecipients);
    for (int i = 0; i < numRecipients; i++) {
      System.arraycopy(recipientPubkeys, i * 33, recipients[i].pubkey, 0, 33);
      System.arraycopy(recipientCiphertexts, i * 66, recipients[i].ciphertext, 0, 66);
    }

    MptPedersenProofParams balanceParams = newPedersenParams(
      balanceCommitment, balanceValue, balanceCiphertext, balanceBlinding
    );

    long[] nativeOutLen = new long[]{outLen[0]};
    int result = lib.mpt_get_confidential_send_proof(
      privkey, pubkey, amount,
      recipients[0], numRecipients,
      txBlindingFactor, contextHash,
      amountCommitment, balanceParams,
      outProof, nativeOutLen
    );
    outLen[0] = (int) nativeOutLen[0];
    return result;
  }

  @Override
  public int generateConvertContextHash(byte[] account, byte[] issuanceId, int sequence, byte[] outHash) {
    return lib.mpt_get_convert_context_hash(newAccountId(account), newIssuanceId(issuanceId), sequence, outHash);
  }

  @Override
  public int generateConvertBackContextHash(
    byte[] account, byte[] issuanceId, int sequence, int version, byte[] outHash
  ) {
    return lib.mpt_get_convert_back_context_hash(
      newAccountId(account), newIssuanceId(issuanceId), sequence, version, outHash
    );
  }

  @Override
  public int generateSendContextHash(
    byte[] account, byte[] issuanceId, int sequence, byte[] destination, int version, byte[] outHash
  ) {
    return lib.mpt_get_send_context_hash(
      newAccountId(account), newIssuanceId(issuanceId), sequence, newAccountId(destination), version, outHash
    );
  }

  @Override
  public int generateClawbackContextHash(
    byte[] account, byte[] issuanceId, int sequence, byte[] holder, byte[] outHash
  ) {
    return lib.mpt_get_clawback_context_hash(
      newAccountId(account), newIssuanceId(issuanceId), sequence, newAccountId(holder), outHash
    );
  }

  private static MptPedersenProofParams newPedersenParams(
    byte[] commitment, long amount, byte[] ciphertext, byte[] blinding
  ) {
    MptPedersenProofParams params = new MptPedersenProofParams();
    System.arraycopy(commitment, 0, params.pedersenCommitment, 0, commitment.length);
    params.amount = amount;
    System.arraycopy(ciphertext, 0, params.encryptedAmount, 0, ciphertext.length);
    System.arraycopy(blinding, 0, params.blindingFactor, 0, blinding.length);
    return params;
  }

  private static MptAccountId newAccountId(byte[] bytes) {
    MptAccountId id = new MptAccountId();
    System.arraycopy(bytes, 0, id.bytes, 0, bytes.length);
    return id;
  }

  private static MptIssuanceId newIssuanceId(byte[] bytes) {
    MptIssuanceId id = new MptIssuanceId();
    System.arraycopy(bytes, 0, id.bytes, 0, bytes.length);
    return id;
  }
}
