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

  /**
   * Returns the shared secp256k1 context used by the mpt-crypto library.
   *
   * @return A pointer to the secp256k1 context.
   */
  com.sun.jna.Pointer mpt_secp256k1_context();

  /**
   * Parses a compressed secp256k1 public key into the internal representation.
   *
   * @param ctx      The secp256k1 context pointer.
   * @param pubkey   A 64-byte buffer to receive the internal public key representation.
   * @param input    The compressed public key bytes.
   * @param inputlen The length of the input (33 for compressed keys).
   *
   * @return 1 on success, 0 on failure.
   */
  int secp256k1_ec_pubkey_parse(
    com.sun.jna.Pointer ctx, byte[] pubkey, byte[] input, long inputlen
  );

  /**
   * Verifies a Schnorr Proof of Knowledge of a secret key.
   *
   * @param ctx       The secp256k1 context pointer.
   * @param proof     The 65-byte proof to verify.
   * @param pk        The 64-byte internal public key representation.
   * @param contextId The 32-byte context hash.
   *
   * @return 1 if the proof is valid, 0 otherwise.
   */
  int secp256k1_mpt_pok_sk_verify(
    com.sun.jna.Pointer ctx, byte[] proof, byte[] pk, byte[] contextId
  );
}
