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
    com.sun.jna.Pointer ctx = lib.mpt_secp256k1_context();

    // Parse compressed 33-byte pubkey into internal 64-byte representation
    byte[] internalPk = new byte[64];
    int parseResult = lib.secp256k1_ec_pubkey_parse(ctx, internalPk, pubkey, pubkey.length);
    if (parseResult != 1) {
      return 0;
    }

    return lib.secp256k1_mpt_pok_sk_verify(ctx, proof, internalPk, ctxHash);
  }

  @Override
  public int generateClawbackProof(
    byte[] privkey, byte[] pubkey, byte[] ctxHash, long amount, byte[] encryptedAmount, byte[] outProof
  ) {
    return lib.mpt_get_clawback_proof(privkey, pubkey, ctxHash, amount, encryptedAmount, outProof);
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
    byte[] privkey, long amount,
    byte[] recipientPubkeys, byte[] recipientCiphertexts, int numRecipients,
    byte[] txBlindingFactor, byte[] contextHash,
    byte[] amountCommitment, long amountValue, byte[] amountCiphertext, byte[] amountBlinding,
    byte[] balanceCommitment, long balanceValue, byte[] balanceCiphertext, byte[] balanceBlinding,
    byte[] outProof, int[] outLen
  ) {
    // Build contiguous array of mpt_confidential_recipient structs
    MptConfidentialRecipient[] recipients =
      (MptConfidentialRecipient[]) new MptConfidentialRecipient().toArray(numRecipients);
    for (int i = 0; i < numRecipients; i++) {
      System.arraycopy(recipientPubkeys, i * 33, recipients[i].pubkey, 0, 33);
      System.arraycopy(recipientCiphertexts, i * 66, recipients[i].encryptedAmount, 0, 66);
    }

    MptPedersenProofParams amountParams = newPedersenParams(
      amountCommitment, amountValue, amountCiphertext, amountBlinding
    );
    MptPedersenProofParams balanceParams = newPedersenParams(
      balanceCommitment, balanceValue, balanceCiphertext, balanceBlinding
    );

    long[] nativeOutLen = new long[]{outLen[0]};
    int result = lib.mpt_get_confidential_send_proof(
      privkey, amount,
      recipients[0], numRecipients,
      txBlindingFactor, contextHash,
      amountParams, balanceParams,
      outProof, nativeOutLen
    );
    outLen[0] = (int) nativeOutLen[0];
    return result;
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
}
