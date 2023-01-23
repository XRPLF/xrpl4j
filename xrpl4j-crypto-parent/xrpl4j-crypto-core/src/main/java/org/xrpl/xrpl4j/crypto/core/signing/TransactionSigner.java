package org.xrpl.xrpl4j.crypto.core.signing;

import org.xrpl.xrpl4j.crypto.core.keys.PrivateKeyable;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Transaction;

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
}
