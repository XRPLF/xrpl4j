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

import com.google.common.annotations.VisibleForTesting;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PrivateKeyable;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Objects;
import java.util.Set;

/**
 * An abstract implementation of {@link SignatureService} with common functionality that subclasses can utilize.
 */
public abstract class AbstractSignatureService<P extends PrivateKeyable> implements SignatureService<P> {

  private final AbstractTransactionSigner<P> abstractTransactionSigner;
  private final AbstractTransactionVerifier abstractTransactionVerifier;

  /**
   * Required-args Constructor.
   *
   * @param signatureUtils A {@link SignatureUtils}.
   */
  public AbstractSignatureService(final SignatureUtils signatureUtils) {
    this.abstractTransactionSigner = new AbstractTransactionSigner<P>(signatureUtils) {
      @Override
      protected Signature edDsaSign(P privateKey, UnsignedByteArray signableTransactionBytes) {
        return AbstractSignatureService.this.edDsaSign(privateKey, signableTransactionBytes);
      }

      @Override
      protected Signature ecDsaSign(P privateKey, UnsignedByteArray signableTransactionBytes) {
        return AbstractSignatureService.this.ecDsaSign(privateKey, signableTransactionBytes);
      }

      @Override
      public PublicKey derivePublicKey(P privateKey) {
        return AbstractSignatureService.this.derivePublicKey(privateKey);
      }
    };

    this.abstractTransactionVerifier = new AbstractTransactionVerifier(signatureUtils) {
      @Override
      protected boolean edDsaVerify(PublicKey publicKey, UnsignedByteArray transactionBytes, Signature signature) {
        return AbstractSignatureService.this.edDsaVerify(publicKey, transactionBytes, signature);
      }

      @Override
      protected boolean ecDsaVerify(PublicKey publicKey, UnsignedByteArray transactionBytes, Signature signature) {
        return AbstractSignatureService.this.ecDsaVerify(publicKey, transactionBytes, signature);
      }
    };
  }

  /**
   * Required-args Constructor, for testing.
   *
   * @param abstractTransactionSigner   A {@link AbstractTransactionSigner}.
   * @param abstractTransactionVerifier A {@link AbstractTransactionVerifier}.
   */
  @VisibleForTesting
  protected AbstractSignatureService(
    final AbstractTransactionSigner<P> abstractTransactionSigner,
    final AbstractTransactionVerifier abstractTransactionVerifier
  ) {
    this.abstractTransactionSigner = Objects.requireNonNull(abstractTransactionSigner);
    this.abstractTransactionVerifier = Objects.requireNonNull(abstractTransactionVerifier);
  }

  @Override
  public <T extends Transaction> SingleSignedTransaction<T> sign(final P privateKeyable, final T transaction) {
    return this.abstractTransactionSigner.sign(privateKeyable, transaction);
  }

  @Override
  public Signature sign(final P privateKeyable, final UnsignedClaim unsignedClaim) {
    return this.abstractTransactionSigner.sign(privateKeyable, unsignedClaim);
  }

  @Override
  public <T extends Transaction> Signature multiSign(final P privateKeyable, final T transaction) {
    return abstractTransactionSigner.multiSign(privateKeyable, transaction);
  }

  @Override
  public <T extends Transaction> boolean verify(
    final SignatureWithPublicKey signatureWithPublicKey, final T unsignedTransaction
  ) {
    return abstractTransactionVerifier.verify(signatureWithPublicKey, unsignedTransaction);
  }

  @Override
  public <T extends Transaction> boolean verifyMultiSigned(
    final Set<SignatureWithPublicKey> signatureWithPublicKeys,
    final T unsignedTransaction,
    final int minSigners
  ) {
    return abstractTransactionVerifier.verifyMultiSigned(signatureWithPublicKeys, unsignedTransaction, minSigners);
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
  public abstract PublicKey derivePublicKey(P privateKey);
}
