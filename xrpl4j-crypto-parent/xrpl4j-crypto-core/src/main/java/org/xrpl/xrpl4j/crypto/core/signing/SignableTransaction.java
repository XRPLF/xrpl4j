package org.xrpl.xrpl4j.crypto.core.signing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Holds both binary and typed manifestations of an XRPL transaction (unsigned), for purposes of signing.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSignableTransaction.class)
@JsonDeserialize(as = ImmutableSignableTransaction.class)
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
  Transaction originalUnsignedTransaction();

  /**
   * The (unsigned) bytes of the transaction to be signed, in canonical format.
   *
   * @return An {@link UnsignedByteArray}.
   */
  @JsonSerialize(using = UnsignedByteArraySerializer.class)
  @JsonDeserialize(using = UnsignedByteArrayDeserializer.class)
  UnsignedByteArray signableTransactionBytes();
}
