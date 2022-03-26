package org.xrpl.xrpl4j.model.client.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
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

import static java.util.Arrays.copyOfRange;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Represents a transaction that has been signed.
 *
 * @param <T> The type of {@link Transaction} that was signed.
 *
 * @deprecated Prefer SignedTransaction from xrpl4j-crypto instead.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSignedTransaction.class)
@JsonDeserialize(as = ImmutableSignedTransaction.class)
@Deprecated
public interface SignedTransaction<T extends Transaction> {

  /**
   * The hash prefix used by the XRPL to identify transaction hashes.
   */
  String SIGNED_TRANSACTION_HASH_PREFIX = "54584E00";

  /**
   * Construct a builder for this class.
   *
   * @param <T> The actual type of {@link Transaction} enclosed in this {@code SignedTransaction}.
   *
   * @return An {@link ImmutableSignedTransaction.Builder}.
   */
  static <T extends Transaction> ImmutableSignedTransaction.Builder<T> builder() {
    return ImmutableSignedTransaction.builder();
  }

  /**
   * The signed transaction.
   *
   * @return A transaction that has been signed.
   */
  T signedTransaction();

  /**
   * The signed transaction, as a hex encoded binary string.
   *
   * @return A {@link String} containing the signed transaction blob.
   */
  String signedTransactionBlob();

  /**
   * The hash of the signed transaction. This field is derived by computing the SHA512Half of the Signed Transaction
   * hash prefix concatenated with the signed transaction blob.
   *
   * @return A {@link Hash256} containing the transaction hash.
   */
  @SuppressWarnings("UnstableApiUsage")
  @Value.Derived
  default Hash256 hash() {
    byte[] hashBytes = copyOfRange(
      Hashing.sha512().hashBytes(
        BaseEncoding.base16().decode(SIGNED_TRANSACTION_HASH_PREFIX.concat(signedTransactionBlob().toUpperCase()))
      ).asBytes(),
      0,
      32
    );
    return Hash256.of(BaseEncoding.base16().encode(hashBytes));
  }
}
