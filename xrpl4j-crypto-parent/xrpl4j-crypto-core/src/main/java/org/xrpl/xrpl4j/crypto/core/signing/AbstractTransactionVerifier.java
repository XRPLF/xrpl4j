package org.xrpl.xrpl4j.crypto.core.signing;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Objects;
import java.util.Set;

/**
 * An abstract implementation of {@link TransactionVerifier}.
 */
public abstract class AbstractTransactionVerifier implements TransactionVerifier {

  private final SignatureUtils signatureUtils;

  /**
   * Required-args Constructor.
   *
   * @param signatureUtils A {@link SignatureUtils}.
   */
  public AbstractTransactionVerifier(final SignatureUtils signatureUtils) {
    this.signatureUtils = Objects.requireNonNull(signatureUtils);
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
  public <T extends Transaction> boolean verifyMultiSigned(
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
            unsignedTransaction, signatureWithPublicKey.signingPublicKey().deriveAddress()
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
   * Accessor for the {@link SignatureUtils} used by this class.
   *
   * @return A {@link SignatureUtils} used by this class.
   */
  private SignatureUtils getSignatureUtils() {
    return this.signatureUtils;
  }
}
