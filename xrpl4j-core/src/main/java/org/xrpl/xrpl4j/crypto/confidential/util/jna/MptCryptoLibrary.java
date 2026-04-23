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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

/**
 * JNA binding interface for the mpt-crypto native library.
 *
 * <p>This interface maps to the C functions declared in {@code mpt_utility.h} from the mpt-crypto library.
 * The native library provides ElGamal encryption, decryption, and zero-knowledge proof generation
 * for Confidential MPT transactions on the XRP Ledger.</p>
 *
 * <p>Access the singleton instance via {@link #getInstance()}.</p>
 */
public interface MptCryptoLibrary extends Library {

  /**
   * Returns the singleton instance of the native library.
   *
   * @return The {@link MptCryptoLibrary} singleton.
   */
  static MptCryptoLibrary getInstance() {
    return Holder.INSTANCE;
  }

  // =========================================================================
  // Encryption / Decryption
  // =========================================================================

  int mpt_encrypt_amount(long amount, byte[] pubkey, byte[] blindingFactor, byte[] outCiphertext);

  int mpt_decrypt_amount(byte[] ciphertext, byte[] privkey, long[] outAmount);

  int mpt_generate_blinding_factor(byte[] outFactor);

  int mpt_get_pedersen_commitment(long amount, byte[] blindingFactor, byte[] outCommitment);

  // =========================================================================
  // Proof Generation
  // =========================================================================

  int mpt_get_convert_proof(byte[] pubkey, byte[] privkey, byte[] ctxHash, byte[] outProof);

  int mpt_get_clawback_proof(
    byte[] priv, byte[] pub, byte[] contextHash, long amount, byte[] encryptedAmount, byte[] outProof
  );

  int mpt_get_convert_back_proof(
    byte[] priv, byte[] pub, byte[] contextHash, long amount,
    MptPedersenProofParams params, byte[] outProof
  );

  int mpt_get_confidential_send_proof(
    byte[] priv, byte[] pub, long amount,
    MptConfidentialRecipient recipients, long numRecipients,
    byte[] txBlindingFactor, byte[] contextHash,
    byte[] amountCommitment, MptPedersenProofParams balanceParams,
    byte[] outProof, long[] outLen
  );

  // =========================================================================
  // Proof Verification
  // =========================================================================

  int mpt_verify_convert_proof(byte[] proof, byte[] pubkey, byte[] contextHash);

  int mpt_verify_convert_back_proof(
    byte[] proof, byte[] pubkey, byte[] ciphertext,
    byte[] balanceCommitment, long amount, byte[] contextHash
  );

  int mpt_verify_send_proof(
    byte[] proof,
    MptConfidentialRecipient participants, byte numParticipants,
    byte[] senderSpendingCiphertext, byte[] amountCommitment,
    byte[] balanceCommitment, byte[] contextHash
  );

  int mpt_verify_clawback_proof(byte[] proof, long amount, byte[] pubkey, byte[] ciphertext, byte[] contextHash);

  // =========================================================================
  // Context Hash Generation
  // =========================================================================

  int mpt_get_convert_context_hash(MptAccountId account, MptIssuanceId iss, int sequence, byte[] outHash);

  int mpt_get_convert_back_context_hash(MptAccountId acc, MptIssuanceId iss, int seq, int ver, byte[] outHash);

  int mpt_get_send_context_hash(
    MptAccountId acc, MptIssuanceId iss, int seq, MptAccountId dest, int ver, byte[] outHash
  );

  int mpt_get_clawback_context_hash(MptAccountId acc, MptIssuanceId iss, int seq, MptAccountId holder, byte[] outHash);

  // =========================================================================
  // JNA Struct Types
  // =========================================================================

  @Structure.FieldOrder({"bytes"})
  class MptAccountId extends Structure implements Structure.ByValue {
    public byte[] bytes = new byte[20];
  }

  @Structure.FieldOrder({"bytes"})
  class MptIssuanceId extends Structure implements Structure.ByValue {
    public byte[] bytes = new byte[24];
  }

  @Structure.FieldOrder({"pedersenCommitment", "amount", "encryptedAmount", "blindingFactor"})
  class MptPedersenProofParams extends Structure {
    public byte[] pedersenCommitment = new byte[33];
    public long amount;
    public byte[] encryptedAmount = new byte[66];
    public byte[] blindingFactor = new byte[32];
  }

  @Structure.FieldOrder({"pubkey", "ciphertext"})
  class MptConfidentialRecipient extends Structure {
    public byte[] pubkey = new byte[33];
    public byte[] ciphertext = new byte[66];
  }

  // =========================================================================
  // Singleton Holder (lazy initialization)
  // =========================================================================

  /**
   * Holder class for lazy initialization of the native library singleton.
   */
  final class Holder {
    private static final MptCryptoLibrary INSTANCE = Native.load("mpt-crypto", MptCryptoLibrary.class);
  }
}
