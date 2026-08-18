package org.xrpl.xrpl4j.crypto.confidential.util;

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

import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;

/**
 * Homomorphic addition and subtraction of ElGamal ciphertexts ("encrypted amounts").
 *
 * <p>ElGamal encryption is additively homomorphic: two ciphertexts encrypted under the <em>same</em> public key can
 * be combined into a ciphertext encrypting the sum or difference of their plaintexts, without ever decrypting. This
 * mirrors the {@code secp256k1_elgamal_add} / {@code secp256k1_elgamal_subtract} routines rippled runs when it applies
 * a confidential transaction to a balance, so the result equals what the ledger will store byte-for-byte.</p>
 *
 * <p>This identity is what lets a client predict an account's post-transaction confidential balance when chaining
 * Confidential MPT operations for the same {@code (account, token)} inside a single Batch (XLS-56): each chained proof
 * must bind the balance the previous inner leaves behind, and homomorphic arithmetic reproduces that balance exactly.
 * Decrypting and re-encrypting cannot: the client does not know the on-ledger balance's blinding factor, so a
 * re-encryption yields a different ciphertext and the proof would bind the wrong value.</p>
 */
public interface ConfidentialCiphertextArithmetic {

  /**
   * Homomorphically adds two ElGamal ciphertexts encrypted under the same public key.
   *
   * @param augend An {@link EncryptedAmount}.
   * @param addend An {@link EncryptedAmount} encrypted under the same public key as {@code augend}.
   *
   * @return An {@link EncryptedAmount} encrypting {@code plaintext(augend) + plaintext(addend)}.
   *
   * @throws NullPointerException  if either argument is null.
   * @throws IllegalStateException if the native computation fails.
   */
  EncryptedAmount add(EncryptedAmount augend, EncryptedAmount addend);

  /**
   * Homomorphically subtracts one ElGamal ciphertext from another, both encrypted under the same public key.
   *
   * @param minuend    An {@link EncryptedAmount}.
   * @param subtrahend An {@link EncryptedAmount} encrypted under the same public key as {@code minuend}.
   *
   * @return An {@link EncryptedAmount} encrypting {@code plaintext(minuend) - plaintext(subtrahend)}.
   *
   * @throws NullPointerException  if either argument is null.
   * @throws IllegalStateException if the native computation fails.
   */
  EncryptedAmount subtract(EncryptedAmount minuend, EncryptedAmount subtrahend);
}
