package org.xrpl.xrpl4j.crypto.core.signing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;

/**
 * A container object for a {@link Signature} and a corresponding {@link PublicKey}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSignatureWithPublicKey.class)
@JsonDeserialize(as = ImmutableSignatureWithPublicKey.class)
public interface SignatureWithPublicKey {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSignatureWithPublicKey.Builder}.
   */
  static ImmutableSignatureWithPublicKey.Builder builder() {
    return ImmutableSignatureWithPublicKey.builder();
  }

  /**
   * A signature for a transaction, verifiable using the {@link SignatureWithPublicKey#signingPublicKey()}.
   *
   * @return A {@link Signature} containing the transaction signature.
   */
  Signature transactionSignature();

  /**
   * The public key used to create this signature.
   *
   * @return A {@link PublicKey} containing the public key used to sign the transaction.
   */
  PublicKey signingPublicKey();

}
