package org.xrpl.xrpl4j.crypto.core.signing;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.AddressUtils;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.KeyStoreType;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
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

  private final KeyStoreType keyStoreType;

  private final SignatureUtils signatureUtils;

  private final AddressUtils addressService;

  /**
   * Required-args Constructor.
   *
   * @param keyStoreType   The {@link KeyStoreType} for this service.
   * @param signatureUtils An {@link SignatureUtils} for help with signing.
   * @param addressService A {@link AddressUtils}.
   */
  public AbstractDelegatedSignatureService(
    final KeyStoreType keyStoreType,
    final SignatureUtils signatureUtils,
    final AddressUtils addressService
  ) {
    this.keyStoreType = Objects.requireNonNull(keyStoreType);
    this.signatureUtils = Objects.requireNonNull(signatureUtils);
    this.addressService = Objects.requireNonNull(addressService);
  }

  @Override
  public final KeyStoreType keyStoreType() {
    return this.keyStoreType;
  }

  @Override
  public <T extends Transaction> SingleSingedTransaction<T> sign(KeyMetadata keyMetadata, T transaction) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(transaction);

    final UnsignedByteArray signableTransactionBytes = this.signatureUtils.toSignableBytes(transaction);
    final SignatureWithPublicKey signatureWithPublicKey = this.signingHelper(keyMetadata, signableTransactionBytes);

    return this.signatureUtils.addSignatureToTransaction(transaction, signatureWithPublicKey.transactionSignature());
  }

  @Override
  public <T extends Transaction> SignatureWithPublicKey multiSign(KeyMetadata keyMetadata, T transaction) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(transaction);

    final Address address = addressService.deriveAddress(this.getPublicKey(keyMetadata));
    final UnsignedByteArray signableTransactionBytes = this.signatureUtils.toMultiSignableBytes(transaction, address);

    return this.signingHelper(keyMetadata, signableTransactionBytes);
  }

  /**
   * Helper to generate a signature based upon an {@link UnsignedByteArray} of transaction bytes.
   *
   * @param keyMetadata              A {@link KeyMetadata} for the signing key.
   * @param signableTransactionBytes A {@link UnsignedByteArray} of transaction bytes.
   *
   * @return A {@link SignatureWithPublicKey}.
   */
  private SignatureWithPublicKey signingHelper(
    final KeyMetadata keyMetadata, final UnsignedByteArray signableTransactionBytes
  ) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(signableTransactionBytes);

    final PublicKey publicKey = this.getPublicKey(keyMetadata);
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

    return SignatureWithPublicKey.builder()
      .signingPublicKey(getPublicKey(keyMetadata))
      .transactionSignature(signature)
      .build();
  }

  @Override
  public <T extends Transaction> boolean verify(
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
  public <T extends Transaction> boolean verify(
    final Set<SignatureWithKeyMetadata> signatureWithKeyMetadataSet, final T unsignedTransaction
  ) {
    Objects.requireNonNull(signatureWithKeyMetadataSet);
    Objects.requireNonNull(unsignedTransaction);
    return verify(signatureWithKeyMetadataSet, unsignedTransaction, signatureWithKeyMetadataSet.size());
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
  public <T extends Transaction> boolean verify(
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
