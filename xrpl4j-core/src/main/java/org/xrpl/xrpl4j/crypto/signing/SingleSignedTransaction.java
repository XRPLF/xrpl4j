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
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Holds the bytes for a signed XRPL transaction.
 *
 * @param <T> The type of {@link Transaction} that was signed.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSingleSignedTransaction.class)
@JsonDeserialize(as = ImmutableSingleSignedTransaction.class)
public interface SingleSignedTransaction<T extends Transaction> extends SignedTransaction<T> {

  /**
   * A builder.
   *
   * @return An {@link ImmutableSingleSignedTransaction.Builder}.
   */
  static <T extends Transaction> ImmutableSingleSignedTransaction.Builder<T> builder() {
    return ImmutableSingleSignedTransaction.builder();
  }

  /**
   * Validates the state of the transaction to ensure that it does not already include a signature. This method asserts
   * that the unsigned transaction associated with the object does not have an existing signature. Transactions with an
   * existing signature are not valid for signing again.
   *
   * @throws IllegalArgumentException if the unsigned transaction already contains a signature.
   */
  @Check
  default void check() {
    Preconditions.checkArgument(
      !this.unsignedTransaction().transactionSignature().isPresent(),
      "Transactions to be signed must not already include a signature."
    );
  }

  /**
   * The signature and public key used to sign.
   *
   * @return A byte-array.
   */
  Signature signature();

}
