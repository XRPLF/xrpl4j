package org.xrpl.xrpl4j.client;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.keypairs.HashUtils;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Represents a transaction that has been signed.
 *
 * @param <T> The type of {@link Transaction} that was signed.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSignedTransaction.class)
@JsonDeserialize(as = ImmutableSignedTransaction.class)
public interface SignedTransaction<T extends Transaction> {

  /**
   * The hash prefix used by the XRPL to identify transaction hashes.
   */
  String SIGNED_TRANSACTION_HASH_PREFIX = "54584e00";

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
  @Value.Derived
  default Hash256 hash() {
    return Hash256.of(
      HashUtils.sha512Half(
        UnsignedByteArray.fromHex(
          SIGNED_TRANSACTION_HASH_PREFIX.concat(signedTransactionBlob()))
      )
        .hexValue()
    );
  }
}
