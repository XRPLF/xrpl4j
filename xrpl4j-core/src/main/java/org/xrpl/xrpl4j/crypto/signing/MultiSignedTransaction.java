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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.immutables.value.Value.Check;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds the bytes for and assembles signers for a multi-signed XRPL transaction.
 *
 * @param <T> The type of {@link Transaction} that was signed.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMultiSignedTransaction.class)
@JsonDeserialize(as = ImmutableMultiSignedTransaction.class)
public interface MultiSignedTransaction<T extends Transaction> extends SignedTransaction<T> {

  /**
   * A builder.
   *
   * @param <T> An instance of {@link T}.
   *
   * @return An {@link ImmutableMultiSignedTransaction.Builder}.
   */
  static <T extends Transaction> ImmutableMultiSignedTransaction.Builder<T> builder() {
    return ImmutableMultiSignedTransaction.builder();
  }

  /**
   * The set of signatures and public keys used to sign.
   *
   * @return A {@link Set} of {@link Signer}s.
   */
  Set<Signer> signerSet();

  /**
   * The transaction with all signers in {@link #signerSet()} added to the {@link Transaction#signers()} field in the
   * correct order.
   *
   * @return A {@link T}.
   */
  @SuppressWarnings("unchecked")
  @Override
  @Value.Derived
  default T signedTransaction() {
    final List<SignerWrapper> signers = signerSet().stream()
      .map(SignerWrapper::of)
      .sorted(
        Comparator.comparing(
          signature -> new BigInteger(
            AddressCodec.getInstance().decodeAccountId(signature.signer().account()).hexValue(), 16
          )
        )
      )
      .collect(Collectors.toList());

    return SignatureUtils.getInstance().addMultiSignaturesToTransaction(unsignedTransaction(), signers);
  }

  /**
   * Validates the state of the current `MultiSignedTransaction` instance to ensure it meets the requirements for
   * properly forming a multi-signed XRP Ledger transaction.
   *
   * @throws IllegalArgumentException If the transaction already has a signature in the `TxnSignature` field or if the
   *                                  `signingPublicKey` is not set to the empty public key constant.
   */
  @Check
  default void check() {

    Preconditions.checkArgument(
      !this.unsignedTransaction().transactionSignature().isPresent(),
      "Transactions to be signed must not already include a signature."
    );

    // TODO: Once https://github.com/XRPLF/xrpl4j/pull/684 is merged, we should update this check to use the new
    // empty public key constant (and update the error message).
    Preconditions.checkArgument(
      this.unsignedTransaction().signingPublicKey().equals(PublicKey.MULTI_SIGN_PUBLIC_KEY),
      "Transactions to be multisigned must set `signingPublicKey` to an empty public key."
    );
  }
}
