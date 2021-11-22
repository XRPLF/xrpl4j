package org.xrpl.xrpl4j.crypto.core.signing;

import com.google.common.base.Preconditions;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.AddressUtils;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Objects;
import java.util.Set;

/**
 * An abstract implementation of {@link DelegatedTransactionVerifier} with common functionality that sub-classes can
 * utilize.
 */
public abstract class AbstractDelegatedTransactionVerifier implements DelegatedTransactionVerifier {

  private final SignatureUtils signatureUtils;
  private final AddressUtils addressService;

  /**
   * Required-args Constructor.
   *
   * @param signatureUtils An {@link SignatureUtils} for help with signing.
   * @param addressService An {@link AddressUtils}.
   */
  protected AbstractDelegatedTransactionVerifier(
    final SignatureUtils signatureUtils,
    final AddressUtils addressService
  ) {
    this.signatureUtils = Objects.requireNonNull(signatureUtils);
    this.addressService = Objects.requireNonNull(addressService);
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

  @Override
  public <T extends Transaction> boolean verifyMultiSigned(
    final Set<SignatureWithKeyMetadata> signatureWithKeyMetadataSet, final T unsignedTransaction
  ) {
    Objects.requireNonNull(signatureWithKeyMetadataSet);
    Objects.requireNonNull(unsignedTransaction);
    return verifyMultiSigned(signatureWithKeyMetadataSet, unsignedTransaction, signatureWithKeyMetadataSet.size());
  }

  @Override
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
