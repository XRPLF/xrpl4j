package org.xrpl.xrpl4j.crypto.signing;

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
