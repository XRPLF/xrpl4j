package org.xrpl.xrpl4j.crypto.signing;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.PrivateKeyReference;
import org.xrpl.xrpl4j.crypto.keys.PrivateKeyable;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Objects;

/**
 * An abstract implementation of {@link SignatureService} with common functionality that subclasses can utilize.
 *
 * @param <P> A type that extends {@link PrivateKeyable}.
 */
public abstract class AbstractTransactionSigner<P extends PrivateKeyable> implements TransactionSigner<P> {

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
  public <T extends Transaction> SingleSignedTransaction<T> sign(final P privateKeyable, final T transaction) {
    Objects.requireNonNull(privateKeyable);
    Objects.requireNonNull(transaction);

    final UnsignedByteArray signableTransactionBytes = this.signatureUtils.toSignableBytes(transaction);
    final Signature signature = this.signingHelper(privateKeyable, signableTransactionBytes);
    return this.signatureUtils.addSignatureToTransaction(transaction, signature);
  }

  @Override
  public Signature sign(final P privateKeyable, final UnsignedClaim unsignedClaim) {
    Objects.requireNonNull(privateKeyable);
    Objects.requireNonNull(unsignedClaim);

    final UnsignedByteArray signableBytes = signatureUtils.toSignableBytes(unsignedClaim);
    return this.signingHelper(privateKeyable, signableBytes);
  }

  @Override
  public <T extends Transaction> Signature multiSign(final P privateKeyable, final T transaction) {
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
   * @return A {@link Signature}.
   */
  private Signature signingHelper(
    final P privateKey, final UnsignedByteArray signableTransactionBytes
  ) {
    Objects.requireNonNull(privateKey);
    Objects.requireNonNull(signableTransactionBytes);

    final PublicKey publicKey = derivePublicKey(privateKey);
    switch (publicKey.keyType()) {
      case ED25519: {
        return this.edDsaSign(privateKey, signableTransactionBytes);
      }
      case SECP256K1: {
        return this.ecDsaSign(privateKey, signableTransactionBytes);
      }
      default: {
        throw new IllegalArgumentException("Unhandled PrivateKey KeyType: {}" + privateKey);
      }
    }
  }

  /**
   * Does the actual work of computing a signature using a ed25519 private-key, as locatable using {@code privateKey}.
   *
   * @param privateKey               A {@link P} used for signing.
   * @param signableTransactionBytes A {@link UnsignedByteArray} to sign.
   *
   * @return A {@link Signature} with data that can be used to submit a transaction to the XRP Ledger.
   */
  protected abstract Signature edDsaSign(P privateKey, UnsignedByteArray signableTransactionBytes);

  /**
   * Does the actual work of computing a signature using a secp256k1 private-key, as locatable using
   * {@code privateKey}.
   *
   * @param privateKey               A {@link P} used for signing.
   * @param signableTransactionBytes A {@link UnsignedByteArray} to sign.
   *
   * @return A {@link Signature} with data that can be used to submit a transaction to the XRP Ledger.
   */
  protected abstract Signature ecDsaSign(P privateKey, UnsignedByteArray signableTransactionBytes);
}
