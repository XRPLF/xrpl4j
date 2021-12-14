package org.xrpl.xrpl4j.crypto.core.signing;

import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Set;

/**
 * Defines how to verify an XRPL transaction using public key material that is not accessible to this JVM. For example,
 * an implementation of this interface might make a call to an HSM that never exposes private-key material.
 * Alternatively, an implementation may use supplied key meta-data to lookup key material in some custom manner (e.g.,
 * by deriving it from a secret value).
 */
public interface DelegatedTransactionVerifier extends DelegatedPublicKeyProvider {

  /**
   * TransactionVerifier the supplied digital-signature to ensure that it was constructed using the private-key
   * corresponding to {@code publicKey}.
   *
   * @param signatureWithKeyMetadata A {@link SignatureWithKeyMetadata} that contains a signature and a public key that
   *                                 can be used to verify the transaction signature.
   * @param unsignedTransaction      A {@link T} that was signed.
   * @param <T>                      A {@link Transaction}.
   *
   * @return {@code true} if the signature is valid and verified; {@code false} otherwise.
   */
  <T extends Transaction> boolean verifySingleSigned(
    SignatureWithKeyMetadata signatureWithKeyMetadata, T unsignedTransaction
  );

  /**
   * Verify that all signers have properly signed the {@code unsignedTransaction}.
   *
   * @param signatureWithKeyMetadataSet A {@link Set} of {@link SignatureWithKeyMetadata} used for verification.
   * @param unsignedTransaction         The {@link Transaction} of type {@link T} that was signed.
   * @param <T>                         The actual type of {@link Transaction}.
   *
   * @return {@code true} if a minimum number of signatures are valid for the supplied transaction; {@code false}
   *   otherwise.
   */
  default <T extends Transaction> boolean verifyMultiSigned(
    final Set<SignatureWithKeyMetadata> signatureWithKeyMetadataSet, final T unsignedTransaction
  ) {
    return verifyMultiSigned(signatureWithKeyMetadataSet, unsignedTransaction, signatureWithKeyMetadataSet.size());
  }

  /**
   * Verify that {@code minSigners} from the collection of public keys have supplied signatures for the given the {@code
   * unsignedTransaction}.
   *
   * @param signatureWithKeyMetadataSet A {@link Set} of {@link SignatureWithKeyMetadata} used for verification.
   * @param unsignedTransaction         The transaction of type {@link T} that was signed.
   * @param minSigners                  The minimum number of signatures required to form a quorum.
   * @param <T>                         The actual type of {@link Transaction}.
   *
   * @return {@code true} if a minimum number of signatures are valid for the supplied transaction; {@code false}
   *   otherwise.
   */
  <T extends Transaction> boolean verifyMultiSigned(
    Set<SignatureWithKeyMetadata> signatureWithKeyMetadataSet, T unsignedTransaction, int minSigners
  );
}
