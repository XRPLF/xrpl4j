package org.xrpl.xrpl4j.crypto.core.signing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Objects;
import java.util.Set;

/**
 * An abstract implementation of {@link DelegatedSignatureService} with common functionality that sub-classes can
 * utilize.
 */
public abstract class AbstractDelegatedSignatureService implements DelegatedSignatureService {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final DelegatedTransactionSigner delegatedTransactionSigner;
  private final DelegatedTransactionVerifier delegatedTransactionVerifier;

  /**
   * Required-args constructor.
   *
   * @param delegatedTransactionSigner   A {@link DelegatedTransactionSigner} to sign transactions.
   * @param delegatedTransactionVerifier A {@link DelegatedTransactionVerifier} to verify signatures.
   */
  public AbstractDelegatedSignatureService(
    final DelegatedTransactionSigner delegatedTransactionSigner,
    final DelegatedTransactionVerifier delegatedTransactionVerifier
  ) {
    this.delegatedTransactionSigner = Objects.requireNonNull(delegatedTransactionSigner);
    this.delegatedTransactionVerifier = Objects.requireNonNull(delegatedTransactionVerifier);
  }


  @Override
  public PublicKey createKeyPair(KeyMetadata keyMetadata) {
    return delegatedTransactionSigner.createKeyPair(keyMetadata);
  }

  @Override
  public PublicKey getPublicKey(KeyMetadata keyMetadata) {
    return delegatedTransactionSigner.getPublicKey(keyMetadata);
  }

  @Override
  public <T extends Transaction> SingleSingedTransaction<T> sign(KeyMetadata keyMetadata, T transaction) {
    return delegatedTransactionSigner.sign(keyMetadata, transaction);
  }

  @Override
  public Signature sign(KeyMetadata keyMetadata, UnsignedClaim unsignedClaim) {
    return delegatedTransactionSigner.sign(keyMetadata, unsignedClaim);
  }

  @Override
  public <T extends Transaction> SignatureWithKeyMetadata multiSign(KeyMetadata keyMetadata, T transaction) {
    return delegatedTransactionSigner.multiSign(keyMetadata, transaction);
  }

  @Override
  public <T extends Transaction> boolean verifySingleSigned(
    final SignatureWithKeyMetadata signatureWithKeyMetadata, final T unsignedTransaction
  ) {
    return delegatedTransactionVerifier.verifySingleSigned(signatureWithKeyMetadata, unsignedTransaction);
  }

  public <T extends Transaction> boolean verifyMultiSigned(
    final Set<SignatureWithKeyMetadata> signatureWithKeyMetadataSet, final T unsignedTransaction
  ) {
    return delegatedTransactionVerifier.verifyMultiSigned(signatureWithKeyMetadataSet, unsignedTransaction);
  }

  public <T extends Transaction> boolean verifyMultiSigned(
    final Set<SignatureWithKeyMetadata> signatureWithKeyMetadataSet, T unsignedTransaction, int minSigners
  ) {
    return delegatedTransactionVerifier.verifyMultiSigned(signatureWithKeyMetadataSet, unsignedTransaction, minSigners);
  }

}
