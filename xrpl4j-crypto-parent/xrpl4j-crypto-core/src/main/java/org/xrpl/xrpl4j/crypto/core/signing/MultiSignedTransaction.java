package org.xrpl.xrpl4j.crypto.core.signing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Set;

/**
 * Holds the bytes for a multi-signed XRPL transaction.
 *
 * @param <T> The type of {@link Transaction} that was signed.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMultiSignedTransaction.class)
@JsonDeserialize(as = ImmutableMultiSignedTransaction.class)
public interface MultiSignedTransaction<T extends Transaction> extends SignedTransaction<T> {

  /**
   * A builder.
   *
   * @param <T> An instance of {@link Transaction}.
   *
   * @return An {@link ImmutableMultiSignedTransaction.Builder}.
   */
  static <T extends Transaction> ImmutableMultiSignedTransaction.Builder<T> builder() {
    return ImmutableMultiSignedTransaction.builder();
  }

  /**
   * The signature and public key used to sign.
   *
   * @return A byte-array.
   */
  Set<SignatureWithPublicKey> signatureWithPublicKeySet();

  @Value.Derived
  @Override
  default Hash256 hash() {
    return SignedTransaction.super.hash();
  }
}
