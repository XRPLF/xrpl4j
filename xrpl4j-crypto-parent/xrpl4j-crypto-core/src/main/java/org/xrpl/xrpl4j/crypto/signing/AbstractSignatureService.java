package org.xrpl.xrpl4j.crypto.signing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.KeyStoreType;
import org.xrpl.xrpl4j.crypto.PublicKey;
import org.xrpl.xrpl4j.keypairs.KeyPairService;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Objects;

/**
 * An abstract implementation of {@link SignatureService} with common functionality that sub-classes can utilize.
 */
public abstract class AbstractSignatureService implements SignatureService {

  /**
   * A logger.
   */
  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final KeyStoreType keyStoreType;

  private final SignatureUtils signatureUtils;

  private final KeyPairService keyPairService;

  /**
   * Required-args Constructor.
   *
   * @param keyStoreType   The {@link KeyStoreType} for this service.
   * @param signatureUtils An {@link SignatureUtils} for help with signing.
   * @param keyPairService
   */
  public AbstractSignatureService(
    final KeyStoreType keyStoreType,
    final SignatureUtils signatureUtils,
    final KeyPairService keyPairService) {
    this.keyStoreType = Objects.requireNonNull(keyStoreType);
    this.signatureUtils = Objects.requireNonNull(signatureUtils);
    this.keyPairService = keyPairService;
  }

  @Override
  public final KeyStoreType keyStoreType() {
    return this.keyStoreType;
  }

  @Override
  public <T extends Transaction> SignedTransaction<T> sign(final KeyMetadata keyMetadata, final T transaction) {
    Signature signature = this.signWithBehavior(keyMetadata, transaction, SigningBehavior.SINGLE);
    return this.signatureUtils.addSignatureToTransaction(transaction, signature);
  }

  @Override
  public Signature signWithBehavior(
    final KeyMetadata keyMetadata,
    final Transaction transaction,
    final SigningBehavior behavior
  ) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(transaction);

    final PublicKey publicKey = this.getPublicKey(keyMetadata);
    final UnsignedByteArray signableTransactionBytes = behavior == SigningBehavior.SINGLE ?
      this.signatureUtils.toSignableBytes(transaction) :
      this.signatureUtils.toMultiSignableBytes(transaction, keyPairService.deriveAddress(publicKey.value()).value());

    final Signature signature;
    switch (publicKey.versionType()) {
      case ED25519: {
        signature = this.edDsaSign(keyMetadata, signableTransactionBytes);
        break;
      }
      case SECP256K1: {
        signature = this.ecDsaSign(keyMetadata, signableTransactionBytes);
        break;
      }
      default: {
        throw new IllegalArgumentException("Unhandled PrivateKey VersionType: {}" + keyMetadata);
      }
    }

    return signature;
  }

  @Override
  public boolean verify(
    final KeyMetadata keyMetadata,
    final SignedTransaction transactionWithSignature
  ) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(transactionWithSignature);

    final byte[] signableTransactionBytes = signatureUtils
      .toSignableBytes(transactionWithSignature.unsignedTransaction()).toByteArray();

    final PublicKey publicKey = this.getPublicKey(keyMetadata);
    final UnsignedByteArray signableTransactionUba = UnsignedByteArray.of(signableTransactionBytes);
    switch (publicKey.versionType()) {
      case ED25519: {
        return edDsaVerify(keyMetadata, transactionWithSignature, signableTransactionUba);
      }
      case SECP256K1: {
        return ecDsaVerify(keyMetadata, transactionWithSignature, signableTransactionUba);
      }
      default: {
        throw new IllegalArgumentException("Unhandled KeyMetadata: %s" + keyMetadata);
      }
    }
  }

  /**
   * Does the actual work of computing a signature using a ed25519 private-key, as locatable using {@code
   * privateKeyMetadata}.
   *
   * @param privateKeyMetadata       A {@link KeyMetadata} to describe the private-key to use for signing.
   * @param signableTransactionBytes A {@link UnsignedByteArray} to sign.
   *
   * @return A {@link Signature} with data that can be used to submit a transaction to the XRP Ledger.
   */
  protected abstract Signature edDsaSign(
    KeyMetadata privateKeyMetadata, UnsignedByteArray signableTransactionBytes
  );

  /**
   * Does the actual work of computing a signature using a secp256k1 private-key, as locatable using {@code
   * privateKeyMetadata}.
   *
   * @param privateKeyMetadata       A {@link KeyMetadata} to describe the private-key to use for signing.
   * @param signableTransactionBytes A {@link UnsignedByteArray} to sign.
   *
   * @return A {@link Signature} with data that can be used to submit a transaction to the XRP Ledger.
   */
  protected abstract Signature ecDsaSign(
    KeyMetadata privateKeyMetadata, UnsignedByteArray signableTransactionBytes
  );

  /**
   * Verify a signature.
   *
   * @param keyMetadata              A {@link KeyMetadata}.
   * @param transactionWithSignature A {@link SignedTransaction}.
   * @param signableTransactionBytes A {@link UnsignedByteArray}.
   *
   * @return {@code true} if the signature is valid; {@code false} otherwise.
   */
  protected abstract boolean edDsaVerify(
    final KeyMetadata keyMetadata,
    final SignedTransaction transactionWithSignature,
    final UnsignedByteArray signableTransactionBytes
  );

  /**
   * Verify a signature.
   *
   * @param keyMetadata              A {@link KeyMetadata}.
   * @param transactionWithSignature A {@link SignedTransaction}.
   * @param signableTransactionBytes A {@link UnsignedByteArray}.
   *
   * @return {@code true} if the signature is valid; {@code false} otherwise.
   */
  protected abstract boolean ecDsaVerify(
    final KeyMetadata keyMetadata,
    final SignedTransaction transactionWithSignature,
    final UnsignedByteArray signableTransactionBytes
  );

}
