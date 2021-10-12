package org.xrpl.xrpl4j.crypto.core.signing;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.AddressUtils;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Objects;
import java.util.Set;

/**
 * An abstract implementation of {@link SignatureService} with common functionality that sub-classes can utilize.
 */
public abstract class AbstractSignatureService implements SignatureService {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final SignatureUtils signatureUtils;
  private final AddressUtils addressUtils;

  /**
   * Required-args Constructor.
   *
   * @param signatureUtils A {@link SignatureUtils}.
   * @param addressUtils   A {@link AddressUtils}.
   */
  public AbstractSignatureService(
    final SignatureUtils signatureUtils,
    final AddressUtils addressUtils
  ) {
    this.signatureUtils = Objects.requireNonNull(signatureUtils);
    this.addressUtils = Objects.requireNonNull(addressUtils);
  }

  @Override
  public <T extends Transaction> SingleSingedTransaction<T> sign(final PrivateKey privateKey, final T transaction) {
    Objects.requireNonNull(privateKey);
    Objects.requireNonNull(transaction);

    final UnsignedByteArray signableTransactionBytes = signatureUtils.toSignableBytes(transaction);
    return this.signHelper(privateKey, transaction, signableTransactionBytes);
  }

  @Override
  public <T extends Transaction> Signature multiSign(
    final PrivateKey privateKey, final T transaction
  ) {
    Objects.requireNonNull(privateKey);
    Objects.requireNonNull(transaction);

    final Address signerAddress = addressUtils.deriveAddress(this.derivePublicKey(privateKey));
    final UnsignedByteArray signableTransactionBytes = signatureUtils.toMultiSignableBytes(transaction, signerAddress);

    return this.signHelper(privateKey, transaction, signableTransactionBytes).signature();
  }

  /**
   * Helper to sign a pre-assembled set of transaction bytes.
   *
   * @param privateKey               A {@link PrivateKey} used for signing.
   * @param transaction              The {@link Transaction} to sign.
   * @param signableTransactionBytes The actual binary bytes of the transaction to sign.
   * @param <T>                      A type of {@link Transaction}.
   *
   * @return A {@link SingleSingedTransaction}.
   */
  private <T extends Transaction> SingleSingedTransaction<T> signHelper(
    final PrivateKey privateKey, final T transaction, final UnsignedByteArray signableTransactionBytes
  ) {
    Objects.requireNonNull(privateKey);
    Objects.requireNonNull(transaction);
    Objects.requireNonNull(signableTransactionBytes);

    final Signature signature;
    switch (privateKey.versionType()) {
      case ED25519: {
        signature = this.edDsaSign(privateKey, signableTransactionBytes);
        break;
      }
      case SECP256K1: {
        signature = this.ecDsaSign(privateKey, signableTransactionBytes);
        break;
      }
      default: {
        throw new IllegalArgumentException("Unhandled PrivateKey VersionType: {}" + privateKey.versionType());
      }
    }

    return this.signatureUtils.addSignatureToTransaction(transaction, signature);
  }

  @Override
  public <T extends Transaction> boolean verify(
    final SignatureWithPublicKey signatureWithPublicKey, final T unsignedTransaction
  ) {
    Objects.requireNonNull(signatureWithPublicKey);
    Objects.requireNonNull(unsignedTransaction);

    final UnsignedByteArray transactionBytesUba = this.getSignatureUtils().toSignableBytes(unsignedTransaction);

    final PublicKey publicKey = signatureWithPublicKey.signingPublicKey();
    switch (publicKey.versionType()) {
      case ED25519: {
        return edDsaVerify(publicKey, transactionBytesUba, signatureWithPublicKey.transactionSignature());
      }
      case SECP256K1: {
        return ecDsaVerify(publicKey, transactionBytesUba, signatureWithPublicKey.transactionSignature());
      }
      default: {
        throw new IllegalArgumentException("Unhandled PublicKey VersionType: {}" + publicKey.versionType());
      }
    }
  }

  @Override
  public <T extends Transaction> boolean verify(
    final Set<SignatureWithPublicKey> signatureWithPublicKeys,
    final T unsignedTransaction,
    final int minSigners
  ) {
    Objects.requireNonNull(signatureWithPublicKeys);
    Objects.requireNonNull(unsignedTransaction);
    Preconditions.checkArgument(minSigners > 0);

    final long numValidSignatures = signatureWithPublicKeys.stream()
      .map(signatureWithPublicKey -> {
        // Check signature against all public keys, hoping for a valid verification against one.
        final UnsignedByteArray unsignedTransactionBytes =
          this.getSignatureUtils().toMultiSignableBytes(
            unsignedTransaction, addressUtils.deriveAddress(signatureWithPublicKey.signingPublicKey())
          );
        final boolean oneValidSignature = verifyHelper(
          signatureWithPublicKey.signingPublicKey(),
          unsignedTransactionBytes,
          signatureWithPublicKey.transactionSignature()
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
   * @param publicKey                A {@link PublicKey} used to verify a signature.
   * @param unsignedTransactionBytes The actual binary bytes of the transaction that was signed.
   * @param signature                The {@link Signature} to verify.
   *
   * @return {@code true} if the signature was created by the private key corresponding to {@code publicKey}; otherwise
   *   {@code false}.
   */
  private boolean verifyHelper(
    final PublicKey publicKey, final UnsignedByteArray unsignedTransactionBytes, final Signature signature
  ) {
    Objects.requireNonNull(publicKey);
    Objects.requireNonNull(signature);
    Objects.requireNonNull(unsignedTransactionBytes);

    switch (publicKey.versionType()) {
      case ED25519: {
        return edDsaVerify(publicKey, unsignedTransactionBytes, signature);
      }
      case SECP256K1: {
        return ecDsaVerify(publicKey, unsignedTransactionBytes, signature);
      }
      default: {
        throw new IllegalArgumentException("Unhandled PublicKey VersionType: {}" + publicKey.versionType());
      }
    }
  }

  /**
   * Does the actual work of computing a signature using an Ed25519 private-key, as locatable using {@code privateKey}.
   *
   * @param privateKey               The {@link PrivateKey} to use for signing.
   * @param signableTransactionBytes The {@link UnsignedByteArray} to sign.
   *
   * @return A {@link Signature} with data that can be used to submit a transaction to the XRP Ledger.
   */
  protected abstract Signature edDsaSign(PrivateKey privateKey, UnsignedByteArray signableTransactionBytes);

  /**
   * Does the actual work of computing a signature using a secp256k1 private-key, as locatable using {@code
   * privateKeyMetadata}.
   *
   * @param privateKey               The {@link PrivateKey} to use for signing.
   * @param signableTransactionBytes The {@link UnsignedByteArray} to sign.
   *
   * @return A {@link Signature} with data that can be used to submit a transaction to the XRP Ledger.
   */
  protected abstract Signature ecDsaSign(PrivateKey privateKey, UnsignedByteArray signableTransactionBytes);

  /**
   * Verify a signature.
   *
   * @param publicKey        The {@link PublicKey} used to verify the signed transaction.
   * @param transactionBytes An {@link UnsignedByteArray} containing the bytes of the transaction that was signed.
   * @param signature        A {@link Signature} over the transaction.
   *
   * @return {@code true} if the signature is valid; {@code false} otherwise.
   */
  protected abstract boolean edDsaVerify(PublicKey publicKey, UnsignedByteArray transactionBytes, Signature signature);

  /**
   * Verify a signature.
   *
   * @param publicKey        A {@link PublicKey}.
   * @param transactionBytes An {@link UnsignedByteArray} containing the bytes of the transaction that was signed.
   * @param signature        A {@link Signature} over the transaction.
   *
   * @return {@code true} if the signature is valid; {@code false} otherwise.
   */
  protected abstract boolean ecDsaVerify(PublicKey publicKey, UnsignedByteArray transactionBytes, Signature signature);

  /**
   * Helper method to derive a public key from a private key.
   *
   * @param privateKey An instance of {@link PrivateKey}.
   *
   * @return A corresponding {@link PublicKey}.
   */
  protected abstract PublicKey derivePublicKey(PrivateKey privateKey);

  /**
   * Accessor for the {@link SignatureUtils} used by this class.
   *
   * @return A {@link SignatureUtils} used by this class.
   */
  public SignatureUtils getSignatureUtils() {
    return this.signatureUtils;
  }
}
