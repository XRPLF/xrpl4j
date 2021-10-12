package org.xrpl.xrpl4j.crypto.core.signing;

import org.xrpl.xrpl4j.crypto.core.keys.PrivateKey;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Defines how to sign an XRPL transaction using private key material supplied by the caller.
 */
public interface TransactionSigner {

  /**
   * Obtain a signature for the supplied transaction using {@code privateKey}.
   *
   * @param privateKey  The {@link PrivateKey} used to sign {@code transaction}.
   * @param transaction The {@link Transaction} to sign.
   * @param <T>         The type {@link Transaction} to be signed.
   *
   * @return A {@link SingleSingedTransaction} of type {@link T} containing everything related to a signed transaction.
   */
  <T extends Transaction> SingleSingedTransaction<T> sign(PrivateKey privateKey, T transaction);

  /**
   * Obtain a signature for the supplied unsigned transaction using the supplied {@link PrivateKey}. The primary reason
   * this method's signature diverges from {@link #sign(PrivateKey, Transaction)} is that for multi-sign scenarios, the
   * interstitially signed transaction is always discarded. Instead, a quorum of signatures is need and then that quorum
   * is submitted to the ledger with the unsigned transaction. Thus, obtaining a multi-signed transaction here is not
   * useful (instead, see TransactionUtils to construct a multisigned transaction).
   *
   * @param privateKey  The {@link PrivateKey} used to sign {@code transaction}.
   * @param transaction The {@link Transaction} to sign.
   * @param <T>         The type of the transaction to be signed.
   *
   * @return A {@link Signature} for the transaction.
   */
  <T extends Transaction> Signature multiSign(PrivateKey privateKey, T transaction);
}
