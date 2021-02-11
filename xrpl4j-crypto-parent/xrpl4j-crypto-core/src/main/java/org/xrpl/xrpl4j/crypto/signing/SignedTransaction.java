package org.xrpl.xrpl4j.crypto.signing;

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
@JsonSerialize(as = ImmutableSignedTransaction.class)
@JsonDeserialize(as = ImmutableSignedTransaction.class)
public interface SignedTransaction<T extends Transaction> {

  /**
   * A builder.
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
   * The bytes of this message.
   *
   * @return A byte-array.
   */
  Signature signature();

}
