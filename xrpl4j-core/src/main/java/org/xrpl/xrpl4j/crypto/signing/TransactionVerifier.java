package org.xrpl.xrpl4j.crypto.signing;

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

import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Set;

/**
 * Defines how to verify an XRPL transaction using public key material supplied by the caller.
 */
public interface TransactionVerifier {

  /**
   * Verify the supplied digital-signature to ensure that it was constructed using the private-key corresponding to
   * {@code signerPublicKey}.
   *
   * @param signatureWithPublicKey A {@link SignatureWithPublicKey} used for verification.
   * @param unsignedTransaction    The {@link Transaction} of type {@link T} that was signed.
   * @param <T>                    The actual type of {@link Transaction}.
   *
   * @return {@code true} if the signature is valid and verified; {@code false} otherwise.
   */
  <T extends Transaction> boolean verify(
    SignatureWithPublicKey signatureWithPublicKey, T unsignedTransaction
  );

  /**
   * Verify that all signers have properly signed the {@code unsignedTransaction}.
   *
   * @param signatureWithPublicKeys A {@link Set} of {@link SignatureWithPublicKey} used for verification.
   * @param unsignedTransaction     The {@link Transaction} of type {@link T} that was signed.
   * @param <T>                     The actual type of {@link Transaction}.
   *
   * @return {@code true} if a minimum number of signatures are valid for the supplied transaction; {@code false}
   *   otherwise.
   */
  default <T extends Transaction> boolean verifyMultiSigned(
    final Set<SignatureWithPublicKey> signatureWithPublicKeys, final T unsignedTransaction
  ) {
    return verifyMultiSigned(signatureWithPublicKeys, unsignedTransaction, signatureWithPublicKeys.size());
  }

  /**
   * Verify that {@code minSigners} from the collection of public keys have supplied signatures for a given signed
   * transaction.
   *
   * @param signatureWithPublicKeys A {@link Set} of {@link SignatureWithPublicKey} used for verification.
   * @param unsignedTransaction     The transaction of type {@link T} that was signed.
   * @param minSigners              The minimum number of signatures required to form a quorum.
   * @param <T>                     The actual type of {@link Transaction}.
   *
   * @return {@code true} if a minimum number of signatures are valid for the supplied transaction; {@code false}
   *   otherwise.
   */
  <T extends Transaction> boolean verifyMultiSigned(
    Set<SignatureWithPublicKey> signatureWithPublicKeys, T unsignedTransaction, int minSigners
  );

}
