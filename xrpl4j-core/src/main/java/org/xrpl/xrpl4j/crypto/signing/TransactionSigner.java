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

import org.xrpl.xrpl4j.crypto.keys.PrivateKeyable;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Objects;

/**
 * Defines how to sign an XRPL transaction using private key material supplied by the caller.
 */
public interface TransactionSigner<P extends PrivateKeyable> {

  /**
   * Accessor for the public-key corresponding to the supplied key meta-data. This method exists to support
   * implementations that hold private-key material internally, yet need a way for external callers to determine the
   * actual public key for signature verification or other purposes.
   *
   * @param privateKeyable A {@link PrivateKeyable} to derive a public key from.
   *
   * @return A {@link PublicKey}.
   */
  PublicKey derivePublicKey(P privateKeyable);

  /**
   * Obtain a singly-signed signature for the supplied transaction using {@code privateKeyable} and the single-sign
   * mechanism.
   *
   * @param privateKeyable The {@link P} used to sign {@code transaction}.
   * @param transaction    The {@link Transaction} to sign.
   * @param <T>            The type {@link Transaction} to be signed.
   *
   * @return A {@link SingleSignedTransaction} of type {@link T} containing everything related to a signed transaction.
   */
  <T extends Transaction> SingleSignedTransaction<T> sign(P privateKeyable, T transaction);

  /**
   * Signs a claim for usage in a Payment Channel.
   *
   * @param privateKeyable A {@link P} used for signing.
   * @param unsignedClaim  An {@link UnsignedClaim}.
   *
   * @return A {@link Signature}.
   */
  Signature sign(P privateKeyable, UnsignedClaim unsignedClaim);

  /**
   * Obtain a signature for the supplied unsigned transaction using the supplied {@link P}. The primary reason this
   * method's signature diverges from {@link #sign(PrivateKeyable, Transaction)} is that for multi-sign scenarios, the
   * interstitially signed transaction is always discarded. Instead, a quorum of signatures is need and then that quorum
   * is submitted to the ledger with the unsigned transaction. Thus, obtaining a multi-signed transaction here is not
   * useful and is not returned from this interface. Note that {@link SignatureUtils} can be used to assemble and obtain
   * the bytes for a multi-signed transaction (these diverge slightly from the bytes of a single-signed transaction).
   *
   * @param privateKeyable The {@link P} used to sign {@code transaction}.
   * @param transaction    The {@link Transaction} to sign.
   * @param <T>            The type of the transaction to be signed.
   *
   * @return A {@link Signature} for the transaction.
   */
  <T extends Transaction> Signature multiSign(P privateKeyable, T transaction);

  /**
   * Obtain a signature for the supplied unsigned transaction using the supplied {@link P}.
   *
   * As is the case with {@link #multiSign(PrivateKeyable, Transaction)}, the primary reason this method's signature
   * diverges from {@link #sign(PrivateKeyable, Transaction)} is that for multi-sign scenarios, the interstitially
   * signed transaction is always discarded. Instead, a quorum of signatures is need and then that quorum is submitted
   * to the ledger with the unsigned transaction. Thus, obtaining a multi-signed transaction here is not useful and is
   * not returned from this interface. Note that {@link SignatureUtils} can be used to assemble and obtain the bytes for
   * a multi-signed transaction (these diverge slightly from the bytes of a single-signed transaction).
   *
   * This method is nearly identical to {@link #multiSign(PrivateKeyable, Transaction)} except that it's return-type is
   * a {@link Signer}, which is more convenient for submitting transactions to the XRP Ledger. Note however that this
   * method internally calls {@link #derivePublicKey(PrivateKeyable)}, which in certain remote-key configurations (e.g.,
   * storing keys in a remote HSM) involves a potentially expensive remote call to the HSM. In most scenarios, the
   * public key will already be available, so callers of this method should consider using
   * {@link #multiSign(PrivateKeyable, Transaction)} instead if public-key derivation is a performance concern.
   *
   * @param privateKeyable The {@link P} used to sign {@code transaction}.
   * @param transaction    The {@link Transaction} to sign.
   * @param <T>            The type of the transaction to be signed.
   *
   * @return A {@link Signature} for the transaction.
   */
  default <T extends Transaction> Signer multiSignToSigner(P privateKeyable, T transaction) {
    Objects.requireNonNull(privateKeyable);
    Objects.requireNonNull(transaction);

    // Compute this only once, just in case public-key derivation is expensive (e.g., a remote HSM).
    final PublicKey signingPublicKey = this.derivePublicKey(privateKeyable);
    return Signer.builder()
      .account(signingPublicKey.deriveAddress())
      .signingPublicKey(signingPublicKey)
      .transactionSignature(multiSign(privateKeyable, transaction))
      .build();
  }
}
