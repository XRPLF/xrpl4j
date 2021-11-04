package org.xrpl.xrpl4j.crypto.core.signing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.TransactionHashUtils;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Holds the bytes for a multi-signed XRPL transaction.
 *
 * @param <T> The type of {@link Transaction} that was signed.
 */
public interface SignedTransaction<T extends Transaction> {

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
  @JsonSerialize(using = UnsignedByteArraySerializer.class)
  @JsonDeserialize(using = UnsignedByteArrayDeserializer.class)
  UnsignedByteArray signedTransactionBytes();

  /**
   * The hash of the {@link #signedTransactionBytes()} which can be used as a handle to the transaction even though the
   * transaction hasn't yet been submitted to the XRP Ledger. This field is derived by computing the SHA512-Half of the
   * Signed Transaction hash prefix concatenated with {@link #signedTransactionBytes()}.
   *
   * @return A {@link Hash256} containing the transaction hash.
   */
  default Hash256 hash() {
    return TransactionHashUtils.computeHash(signedTransactionBytes());
  }

}
