package org.xrpl.xrpl4j.crypto.core.signing;

import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKeyReference;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKeyable;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Objects;

/**
 * An abstract implementation of {@link SignatureService} with common functionality that sub-classes can utilize.
 */
public abstract class AbstractTransactionSigner<PK extends PrivateKeyable> implements TransactionSigner<PK> {

  private final SignatureUtils signatureUtils;

  /**
   * Required-args Constructor.
   *
   * @param signatureUtils A {@link SignatureUtils}.
   */
  public AbstractTransactionSigner(final SignatureUtils signatureUtils) {
    this.signatureUtils = Objects.requireNonNull(signatureUtils);
  }

  @Override
  public <T extends Transaction> SingleSingedTransaction<T> sign(final PK privateKeyable, final T transaction) {
    Objects.requireNonNull(privateKeyable);
    Objects.requireNonNull(transaction);

    final UnsignedByteArray signableTransactionBytes = this.signatureUtils.toSignableBytes(transaction);
    final Signature signature = this.signingHelper(privateKeyable, signableTransactionBytes);
    return this.signatureUtils.addSignatureToTransaction(transaction, signature);
  }

  @Override
  public Signature sign(final PK privateKeyable, final UnsignedClaim unsignedClaim) {
    Objects.requireNonNull(privateKeyable);
    Objects.requireNonNull(unsignedClaim);

    final UnsignedByteArray signableBytes = signatureUtils.toSignableBytes(unsignedClaim);
    return this.signingHelper(privateKeyable, signableBytes);
  }

  @Override
  public <T extends Transaction> Signature multiSign(final PK privateKeyable, final T transaction) {
    Objects.requireNonNull(privateKeyable);
    Objects.requireNonNull(transaction);

    final Address address = derivePublicKey(privateKeyable).deriveAddress();
    final UnsignedByteArray signableTransactionBytes = this.signatureUtils.toMultiSignableBytes(transaction, address);

    return this.signingHelper(privateKeyable, signableTransactionBytes);
  }

  /**
   * Helper to generate a signature based upon an {@link UnsignedByteArray} of transaction bytes.
   *
   * @param privateKey               A {@link PrivateKeyReference} for the signing key.
   * @param signableTransactionBytes A {@link UnsignedByteArray} of transaction bytes.
   *
   * @return A {@link SignatureWithPublicKey}.
   */
  private Signature signingHelper(
    final PK privateKey, final UnsignedByteArray signableTransactionBytes
  ) {
    Objects.requireNonNull(privateKey);
    Objects.requireNonNull(signableTransactionBytes);

    final PublicKey publicKey = derivePublicKey(privateKey);
    switch (publicKey.versionType()) {
      case ED25519: {
        return this.edDsaSign(privateKey, signableTransactionBytes);
      }
      case SECP256K1: {
        return this.ecDsaSign(privateKey, signableTransactionBytes);
      }
      default: {
        throw new IllegalArgumentException("Unhandled PrivateKey VersionType: {}" + privateKey);
      }
    }
  }

  /**
   * Does the actual work of computing a signature using a ed25519 private-key, as locatable using {@code privateKey}.
   *
   * @param privateKey               A {@link PK} used for signing.
   * @param signableTransactionBytes A {@link UnsignedByteArray} to sign.
   *
   * @return A {@link Signature} with data that can be used to submit a transaction to the XRP Ledger.
   */
  protected abstract Signature edDsaSign(PK privateKey, UnsignedByteArray signableTransactionBytes);

  /**
   * Does the actual work of computing a signature using a secp256k1 private-key, as locatable using
   * {@code privateKey}.
   *
   * @param privateKey               A {@link PK} used for signing.
   * @param signableTransactionBytes A {@link UnsignedByteArray} to sign.
   *
   * @return A {@link Signature} with data that can be used to submit a transaction to the XRP Ledger.
   */
  protected abstract Signature ecDsaSign(PK privateKey, UnsignedByteArray signableTransactionBytes);
}
