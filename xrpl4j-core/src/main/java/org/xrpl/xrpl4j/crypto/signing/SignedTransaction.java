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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Arrays;

/**
 * Holds the bytes for a multi-signed XRPL transaction.
 *
 * @param <T> The type of {@link Transaction} that was signed.
 */
public interface SignedTransaction<T extends Transaction> {

  /**
   * The hash prefix used by the XRPL to identify transaction hashes.
   */
  String SIGNED_TRANSACTION_HASH_PREFIX = "54584E00";

  /**
   * The original transaction with no signature attached.
   *
   * @return A {@link Transaction}.
   */
  T unsignedTransaction();

  /**
   * The transaction with a signature blob attached.
   *
   * @return A {@link Transaction}.
   */
  T signedTransaction();

  /**
   * The {@link #signedTransaction()} encoded into bytes that are suitable for submission to the XRP Ledger.
   *
   * @return A byte-array containing the signed transaction blob.
   */
  @Value.Derived
  default UnsignedByteArray signedTransactionBytes() {
    try {
      return UnsignedByteArray.fromHex(
        XrplBinaryCodec.getInstance().encode(ObjectMapperFactory.create().writeValueAsString(signedTransaction()))
      );
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * The hash of the {@link #signedTransactionBytes()} which can be used as a handle to the transaction even though the
   * transaction hasn't yet been submitted to the XRP Ledger. This field is derived by computing the SHA512-Half of the
   * Signed Transaction hash prefix concatenated with {@link #signedTransactionBytes()}.
   *
   * @return A {@link Hash256} containing the transaction hash.
   */
  @Value.Derived
  default Hash256 hash() {
    byte[] hashBytes = Arrays.copyOfRange(
      Hashing.sha512().hashBytes(
        BaseEncoding.base16().decode(
          SIGNED_TRANSACTION_HASH_PREFIX.concat(signedTransactionBytes().hexValue()).toUpperCase()
        )).asBytes(),
      0,
      32 // <-- SHA512 Half is the first 32 bytes of the SHA512 hash.
    );
    return Hash256.of(BaseEncoding.base16().encode(hashBytes));
  }

}
