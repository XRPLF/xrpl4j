package org.xrpl.xrpl4j.crypto.signing;

import static java.util.Arrays.copyOfRange;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Holds the bytes for a signed XRPL transaction.
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
  String SIGNED_TRANSACTION_HASH_PREFIX = "54584E00";

  /**
   * A builder.
   *
   * @param <T> An instance of {@link Transaction}.
   *
   * @return An {@link ImmutableSignedTransaction.Builder}.
   */
  static <T extends Transaction> ImmutableSignedTransaction.Builder<T> builder() {
    return ImmutableSignedTransaction.builder();
  }

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
  UnsignedByteArray signedTransactionBytes();

  /**
   * The bytes of this message.
   *
   * @return A byte-array.
   */
  Signature signature();

  /**
   * The hash of the {@link #signedTransactionBytes()} which can be used as a handle to the transaction even though the
   * transaction hasn't yet been submitted to the XRP Ledger. This field is derived by computing the SHA512-Half of the
   * Signed Transaction hash prefix concatenated with {@link #signedTransactionBytes()}.
   *
   * @return A {@link Hash256} containing the transaction hash.
   */
  @Value.Derived
  default Hash256 hash() {
    byte[] hashBytes = copyOfRange(
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
