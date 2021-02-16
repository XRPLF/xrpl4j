package org.xrpl.xrpl4j.crypto.signing;

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
   *
   * @return A {@link SignedTransaction} containing binary data that can be submitted to the XRP Ledger in order to
   *   effect a transaction.
   */
  SignedTransaction sign(KeyMetadata keyMetadata, Transaction transaction);
}
