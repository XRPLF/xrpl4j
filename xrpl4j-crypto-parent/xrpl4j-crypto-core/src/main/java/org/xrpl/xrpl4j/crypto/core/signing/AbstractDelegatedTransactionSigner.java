package org.xrpl.xrpl4j.crypto.core.signing;

import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.AddressUtils;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Objects;

public abstract class AbstractDelegatedTransactionSigner implements DelegatedTransactionSigner {

  private final SignatureUtils signatureUtils;
  private final AddressUtils addressService;

  /**
   * Required-args Constructor.
   *
   * @param signatureUtils An {@link SignatureUtils} for help with signing.
   * @param addressService A {@link AddressUtils}.
   */
  public AbstractDelegatedTransactionSigner(
    final SignatureUtils signatureUtils,
    final AddressUtils addressService
  ) {
    this.signatureUtils = Objects.requireNonNull(signatureUtils);
    this.addressService = Objects.requireNonNull(addressService);
  }

  @Override
  public <T extends Transaction> SingleSingedTransaction<T> sign(KeyMetadata keyMetadata, T transaction) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(transaction);

    final UnsignedByteArray signableTransactionBytes = this.signatureUtils.toSignableBytes(transaction);
    final Signature signature = this.signingHelper(keyMetadata, signableTransactionBytes).transactionSignature();
    return this.signatureUtils.addSignatureToTransaction(transaction, signature);
  }

  @Override
  public Signature sign(final KeyMetadata keyMetadata, final UnsignedClaim unsignedClaim) {
    Objects.requireNonNull(keyMetadata);
    Objects.requireNonNull(unsignedClaim);

    final UnsignedByteArray signableBytes = signatureUtils.toSignableBytes(unsignedClaim);
    return this.signingHelper(keyMetadata, signableBytes).transactionSignature();
  }

  @Override
  public <T extends Transaction> SignatureWithKeyMetadata multiSign(KeyMetadata keyMetadata, T transaction) {
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
  private SignatureWithKeyMetadata signingHelper(
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

    return SignatureWithKeyMetadata.builder()
      .signingKeyMetadata(keyMetadata)
      .transactionSignature(signature)
      .build();
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
}
