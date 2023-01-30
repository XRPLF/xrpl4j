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
import org.immutables.value.Value;
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
   * @param <T> An instance of {@link Transaction}.
   *
   * @return An {@link ImmutableSingleSignedTransaction.Builder}.
   */
  static <T extends Transaction> ImmutableSingleSignedTransaction.Builder<T> builder() {
    return ImmutableSingleSignedTransaction.builder();
  }

  /**
   * The signature and public key used to sign.
   *
   * @return A byte-array.
   */
  Signature signature();

}
