package org.xrpl.xrpl4j.crypto.core.signing;

import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Defines how to sign an XRPL transaction using private key material that is not accessible to this JVM. For example,
 * an implementation of this interface might make a call to an HSM that never exposes private-key material.
 * Alternatively, an implementation may use supplied key meta-data to lookup key material in some custom manner (e.g.,
 * by deriving it from a secret value).
 */
public interface DelegatedTransactionSigner {

  /**
   * Obtain a signature for the supplied transaction using the private-key that corresponds to {@code keyMetadata}. If
   * an implementation does not contain more than a single public/private key pair, then {@link KeyMetadata#EMPTY}
   * should be passed into this method.
   *
   * @param keyMetadata A {@link KeyMetadata} that describes the public/private Keypair to use for signing operations.
   * @param transaction The {@link Transaction} to sign.
   * @param <T>         The type of the transaction to be signed.
   *
   * @return A {@link SingleSingedTransaction} containing binary data that can be submitted to the XRP Ledger in order
   *   to effect a transaction.
   */
  <T extends Transaction> SingleSingedTransaction<T> sign(final KeyMetadata keyMetadata, final T transaction);

  // TODO: Add ability to sign a claim.

  /**
   * Obtain a signature for the supplied transaction using {@code privateKey}.
   *
   * @param keyMetadata A {@link KeyMetadata} used to lookup the location of they private key used to sign {@code
   *                    transaction}.
   * @param transaction The {@link Transaction} to sign.
   * @param <T>         The type {@link Transaction} to be signed.
   *
   * @return A {@link SingleSingedTransaction} of type {@link T} containing everything related to a signed transaction.
   */
  <T extends Transaction> SignatureWithPublicKey multiSign(KeyMetadata keyMetadata, T transaction);
}
