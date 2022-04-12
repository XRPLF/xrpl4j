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
 * Defines how to sign an XRPL transaction.
 */
public interface TransactionSigner {

  /**
   * Obtain a signature for the supplied transaction using the private-key that corresponds to {@code keyMetadata}. If
   * an implementation does not contain more than a single public/private key pair, then {@link KeyMetadata#EMPTY}
   * should be passed into this method.
   *
   * @param keyMetadata A {@link KeyMetadata} that describes the public/private Keypair to use for signing operations.
   * @param transaction A {@link Transaction} to sign.
   * @param <T>         The type of the transaction to be signed.
   *
   * @return A {@link SignedTransaction} containing binary data that can be submitted to the XRP Ledger in order to
   *   effect a transaction.
   */
  <T extends Transaction> SignedTransaction<T> sign(KeyMetadata keyMetadata, T transaction);

  /**
   * Obtain a signature according to the {@code behavior} specified for the supplied transaction using the private-key
   * that corresponds to {@code keyMetadata}. This method can be used to sign a multi-signed transaction by
   * passing in {@link SigningBehavior#MULTI}.
   *
   * <p>If an implementation does not contain more than a single public/private key pair, then {@link KeyMetadata#EMPTY}
   * should be passed into this method.
   *
   * @param keyMetadata A {@link KeyMetadata} that describes the public/private Keypair to use for signing operations.
   * @param transaction A {@link Transaction} to sign.
   * @param behavior    A {@link SigningBehavior} specifying the type of signature that should be produced.
   *
   * @return A {@link Signature} containing the transaction signature.
   */
  Signature signWithBehavior(KeyMetadata keyMetadata, Transaction transaction, SigningBehavior behavior);
}
