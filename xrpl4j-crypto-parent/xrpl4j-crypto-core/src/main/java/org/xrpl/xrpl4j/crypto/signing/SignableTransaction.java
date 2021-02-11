package org.xrpl.xrpl4j.crypto.signing;

import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Holds both binary and typed manifestations of an XRPL transaction, for purposes of signing.
 */
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
   * The original transaction that corresponds to this object.
   *
   * @return A {@link Transaction}.
   */
  Transaction originalTransaction();

  /**
   * The bytes of this message in canonical format such that signing them can be used by the XRP Ledger.
   *
   * @return An {@link UnsignedByteArray}.
   */
  UnsignedByteArray signableTransactionBytes();
}
