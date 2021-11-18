package org.xrpl.xrpl4j.crypto.core.signing;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.AddressUtils;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.KeyStoreType;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Objects;
import java.util.Set;

/**
 * An abstract implementation of {@link DelegatedSignatureService} with common functionality that sub-classes can
 * utilize.
 */
public abstract class AbstractDelegatedSignatureService implements DelegatedSignatureService {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  private SignatureUtils signatureUtils = null;

  private AddressUtils addressService = null;

  private final DelegatedTransactionSigner delegatedTransactionSigner;
  private final DelegatedTransactionVerifier delegatedTransactionVerifier;

  public AbstractDelegatedSignatureService(DelegatedTransactionSigner delegatedTransactionSigner, DelegatedTransactionVerifier delegatedTransactionVerifier) {
    this.delegatedTransactionSigner = delegatedTransactionSigner;
    this.delegatedTransactionVerifier = delegatedTransactionVerifier;
  }


  @Override
  public PublicKey getPublicKey(KeyMetadata keyMetadata) {
    return null;
  }

  @Override
  public <T extends Transaction> SingleSingedTransaction<T> sign(KeyMetadata keyMetadata, T transaction) {
    return delegatedTransactionSigner.sign(keyMetadata, transaction);
  }

  @Override
  public Signature sign(KeyMetadata keyMetadata, UnsignedClaim unsignedClaim) {
    return null;
  }

  @Override
  public <T extends Transaction> SignatureWithKeyMetadata multiSign(KeyMetadata keyMetadata, T transaction) {
    return null;
  }

  @Override
  public <T extends Transaction> boolean verifySingleSigned(
    final SignatureWithKeyMetadata signatureWithKeyMetadata, final T unsignedTransaction
  ) {
    Objects.requireNonNull(signatureWithKeyMetadata);
    Objects.requireNonNull(unsignedTransaction);

    final byte[] signableTransactionBytes = signatureUtils.toSignableBytes(unsignedTransaction).toByteArray();

    final PublicKey publicKey = this.getPublicKey(signatureWithKeyMetadata.signingKeyMetadata());
    final KeyMetadata keyMetadata = signatureWithKeyMetadata.signingKeyMetadata();
    final Signature signature = signatureWithKeyMetadata.transactionSignature();
    final UnsignedByteArray signableTransactionUba = UnsignedByteArray.of(signableTransactionBytes);
    switch (publicKey.versionType()) {
      case ED25519: {
        return edDsaVerify(keyMetadata, signableTransactionUba, signature);
      }
      case SECP256K1: {
        return ecDsaVerify(keyMetadata, signableTransactionUba, signature);
      }
      default: {
        throw new IllegalArgumentException("Unhandled KeyMetadata: %s" + keyMetadata);
      }
    }
  }

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
  public <T extends Transaction> boolean verifyMultiSigned(
    final Set<SignatureWithKeyMetadata> signatureWithKeyMetadataSet, final T unsignedTransaction
  ) {
    Objects.requireNonNull(signatureWithKeyMetadataSet);
    Objects.requireNonNull(unsignedTransaction);
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
  public <T extends Transaction> boolean verifyMultiSigned(
    final Set<SignatureWithKeyMetadata> signatureWithKeyMetadataSet, T unsignedTransaction, int minSigners
  ) {
    Objects.requireNonNull(signatureWithKeyMetadataSet);
    Objects.requireNonNull(unsignedTransaction);
    Preconditions.checkArgument(minSigners > 0);

    final long numValidSignatures = signatureWithKeyMetadataSet.stream()
      .map(signatureWithKeyMetadata -> {
        // Check signature against all public keys, hoping for a valid verification against one.
        final UnsignedByteArray unsignedTransactionBytes =
          signatureUtils.toMultiSignableBytes(
            unsignedTransaction,
            addressService.deriveAddress(getPublicKey(signatureWithKeyMetadata.signingKeyMetadata()))
          );
        final boolean oneValidSignature = verifyHelper(
          signatureWithKeyMetadata.signingKeyMetadata(),
          unsignedTransactionBytes,
          signatureWithKeyMetadata.transactionSignature()
        );
        return oneValidSignature;
      })
      .filter($ -> $) // Only count it if it's 'true'
      .count();

    return numValidSignatures >= minSigners;
  }

  /**
   * Helper to verify a signed transaction.
   *
   * @param keyMetadata              A {@link KeyMetadata} used to lookup a {@link PublicKey} to verify a signature.
   * @param unsignedTransactionBytes The actual binary bytes of the transaction that was signed.
   * @param signature                The {@link Signature} to verify.
   *
   * @return {@code true} if the signature was created by the private key corresponding to {@code publicKey}; otherwise
   *   {@code false}.
   */
  private boolean verifyHelper(
    final KeyMetadata keyMetadata, final UnsignedByteArray unsignedTransactionBytes, final Signature signature
  ) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(signature);
    Objects.requireNonNull(unsignedTransactionBytes);

    final PublicKey publicKey = getPublicKey(keyMetadata);
    switch (publicKey.versionType()) {
      case ED25519: {
        return edDsaVerify(keyMetadata, unsignedTransactionBytes, signature);
      }
      case SECP256K1: {
        return ecDsaVerify(keyMetadata, unsignedTransactionBytes, signature);
      }
      default: {
        throw new IllegalArgumentException("Unhandled PublicKey VersionType: {}" + publicKey.versionType());
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
   *//*
  protected Signature edDsaSign(
    KeyMetadata privateKeyMetadata, UnsignedByteArray signableTransactionBytes
  ) {
    return delegatedTransactionSigner.
  }

  *//**
   * Does the actual work of computing a signature using a secp256k1 private-key, as locatable using {@code
   * privateKeyMetadata}.
   *
   * @param privateKeyMetadata       A {@link KeyMetadata} to describe the private-key to use for signing.
   * @param signableTransactionBytes A {@link UnsignedByteArray} to sign.
   *
   * @return A {@link Signature} with data that can be used to submit a transaction to the XRP Ledger.
   *//*
  protected abstract Signature ecDsaSign(
    KeyMetadata privateKeyMetadata, UnsignedByteArray signableTransactionBytes
  );*/

  /**
   * Verify a signature.
   *
   * @param keyMetadata              A {@link KeyMetadata}.
   * @param signableTransactionBytes A {@link UnsignedByteArray}.
   * @param transactionSignature     A {@link Signature}.
   *
   * @return {@code true} if the signature is valid; {@code false} otherwise.
   */
  protected abstract boolean edDsaVerify(
    KeyMetadata keyMetadata,
    UnsignedByteArray signableTransactionBytes,
    Signature transactionSignature
  );

  /**
   * Verify a signature.
   *
   * @param keyMetadata              A {@link KeyMetadata}.
   * @param signableTransactionBytes A {@link UnsignedByteArray}.
   * @param transactionSignature     A {@link Signature}.
   *
   * @return {@code true} if the signature is valid; {@code false} otherwise.
   */
  protected abstract boolean ecDsaVerify(
    KeyMetadata keyMetadata,
    UnsignedByteArray signableTransactionBytes,
    Signature transactionSignature
  );

}
