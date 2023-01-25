package org.xrpl.xrpl4j.crypto.core.signing;

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
