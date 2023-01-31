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

import com.google.common.base.Preconditions;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Signer;
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
  public <T extends Transaction> boolean verify(final Signer signer, final T unsignedTransaction) {
    Objects.requireNonNull(signer);
    Objects.requireNonNull(unsignedTransaction);

    final UnsignedByteArray transactionBytesUba = this.getSignatureUtils().toSignableBytes(unsignedTransaction);

    final PublicKey publicKey = signer.signingPublicKey();
    switch (publicKey.keyType()) {
      case ED25519: {
        return edDsaVerify(publicKey, transactionBytesUba, signer.transactionSignature());
      }
      case SECP256K1: {
        return ecDsaVerify(publicKey, transactionBytesUba, signer.transactionSignature());
      }
      default: {
        throw new IllegalArgumentException("Unhandled PublicKey KeyType: {}" + publicKey.keyType());
      }
    }
  }

  @Override
  public <T extends Transaction> boolean verifyMultiSigned(
    final Set<Signer> signerSet,
    final T unsignedTransaction,
    final int minSigners
  ) {
    Objects.requireNonNull(signerSet);
    Objects.requireNonNull(unsignedTransaction);
    Preconditions.checkArgument(minSigners > 0);

    final long numValidSignatures = signerSet.stream()
      .map(signer -> {
        // Check signature against all public keys, hoping for a valid verification against one.
        final UnsignedByteArray unsignedTransactionBytes =
          this.getSignatureUtils().toMultiSignableBytes(
            unsignedTransaction, signer.signingPublicKey().deriveAddress()
          );
        final boolean oneValidSignature = verifyHelper(signer, unsignedTransactionBytes);
        return oneValidSignature;
      })
      .filter($ -> $) // Only count it if it's 'true'
      .count();

    return numValidSignatures >= minSigners;
  }

  /**
   * Helper to verify a signed transaction.
   *
   * @param signer                   A {@link Signer} used to verify a signature.
   * @param unsignedTransactionBytes The actual binary bytes of the transaction that was signed.
   *
   * @return {@code true} if the signature was created by the private key corresponding to {@code publicKey}; otherwise
   *   {@code false}.
   */
  private boolean verifyHelper(final Signer signer, final UnsignedByteArray unsignedTransactionBytes) {
    Objects.requireNonNull(signer);
    Objects.requireNonNull(unsignedTransactionBytes);

    final PublicKey signerPublicKey = signer.signingPublicKey();
    final Signature signerSignature = signer.transactionSignature();

    switch (signerPublicKey.keyType()) {
      case ED25519: {
        return edDsaVerify(signerPublicKey, unsignedTransactionBytes, signerSignature);
      }
      case SECP256K1: {
        return ecDsaVerify(signerPublicKey, unsignedTransactionBytes, signerSignature);
      }
      default: {
        throw new IllegalArgumentException("Unhandled PublicKey KeyType: {}" + signerPublicKey.keyType());
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
