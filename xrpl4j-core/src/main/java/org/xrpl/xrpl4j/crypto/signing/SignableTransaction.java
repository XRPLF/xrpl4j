package org.xrpl.xrpl4j.crypto.signing;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: crypto :: core
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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

import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Holds both binary and typed manifestations of an XRPL transaction (unsigned), for purposes of signing.
 *
 * @deprecated Prefer the variant found in {@link org.xrpl.xrpl4j.crypto.core} instead.
 */
@Deprecated
@Value.Immutable
public interface SignableTransaction {

  /**
   * A builder.
   *
   * @return An {@link ImmutableSignableTransaction.Builder}.
   */
  static ImmutableSignableTransaction.Builder builder() {
    return ImmutableSignableTransaction.builder();
  }

  /**
   * The original transaction (unsigned) that corresponds this signed transcation.
   *
   * @return A {@link Transaction}.
   */
  Transaction originalTransaction();

  /**
   * The (unsigned) bytes of the transaction to be signed, in canonical format.
   *
   * @return An {@link UnsignedByteArray}.
   */
  UnsignedByteArray signableTransactionBytes();
}
