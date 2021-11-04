package org.xrpl.xrpl4j.crypto.core.signing;

import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Defines how to sign XRPL transactions and claims using private key material that is not accessible to the JVM running
 * implementations of this interface. For example, an implementation might make a call to an HSM that never exposes
 * private-key material. Alternatively, an implementation may use key meta-data to lookup key material in some custom
 * manner (e.g., by deriving it from a secret value).
 */
public interface DelegatedTransactionSigner {

  /**
   * Obtain a signature for the supplied transaction using the private-key that corresponds to {@code keyMetadata}.
   *
   * @param keyMetadata A {@link KeyMetadata} that describes the public/private Keypair to use for signing operations.
   * @param transaction The {@link Transaction} to sign.
   * @param <T>         The type of the transaction to be signed.
   *
   * @return A {@link SingleSingedTransaction} containing binary data that can be submitted to the XRP Ledger in order
   *   to effect a transaction.
   */
  <T extends Transaction> SingleSingedTransaction<T> sign(KeyMetadata keyMetadata, T transaction);

  /**
   * Obtain a signature for the supplied unsigned payment channel claim using the private-key that corresponds to {@code
   * keyMetadata}.
   *
   * @param keyMetadata   A {@link KeyMetadata} that describes the public/private Keypair to use for signing
   *                      operations.
   * @param unsignedClaim The {@link UnsignedClaim} to sign.
   *
   * @return A {@link SingleSingedTransaction} containing binary data that can be submitted to the XRP Ledger in order
   *   to effect a transaction.
   */
  Signature sign(KeyMetadata keyMetadata, UnsignedClaim unsignedClaim);

  /**
   * Obtain a multi-sig signature for the supplied transaction using {@code keyMetadata}.
   *
   * @param keyMetadata A {@link KeyMetadata} that refers to a private key that can be used to sign {@code
   *                    transaction}.
   * @param transaction The {@link Transaction} to sign.
   * @param <T>         The type {@link Transaction} to be signed.
   *
   * @return A {@link SingleSingedTransaction} of type {@link T} containing everything related to a signed transaction.
   */
  <T extends Transaction> SignatureWithKeyMetadata multiSign(KeyMetadata keyMetadata, T transaction);
}
