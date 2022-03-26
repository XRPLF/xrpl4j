package org.xrpl.xrpl4j.crypto.signing;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: crypto :: core
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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

import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Defines how to verify an XRPL transaction using public key material supplied by the caller.
 *
 * @deprecated Prefer the variant found in {@link org.xrpl.xrpl4j.crypto.core} instead.
 */
@Deprecated
public interface TransactionVerifier {

  /**
   * TransactionVerifier the supplied digital-signature to ensure that it was constructed using the private-key
   * corresponding to {@code publicKey}.
   *
   * @param keyMetadata              A {@link KeyMetadata} that describes the public/private Keypair to use for
   *                                 verification.
   * @param transactionWithSignature A {@link SignedTransaction} with a {@link Signature} over a supplied {@link
   *                                 Transaction}.
   * @param <T>                      The actual type of {@link Transaction}.
   *
   * @return {@code true} if the signature is valid and verified; {@code false} otherwise.
   */
  <T extends Transaction> boolean verify(KeyMetadata keyMetadata, SignedTransaction<T> transactionWithSignature);
}
