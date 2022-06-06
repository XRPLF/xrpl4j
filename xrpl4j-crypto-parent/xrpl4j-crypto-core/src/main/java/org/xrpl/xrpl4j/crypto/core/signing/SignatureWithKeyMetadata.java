package org.xrpl.xrpl4j.crypto.core.signing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;

/**
 * A container object for a {@link Signature} and a corresponding {@link KeyMetadata}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSignatureWithKeyMetadata.class)
@JsonDeserialize(as = ImmutableSignatureWithKeyMetadata.class)
public interface SignatureWithKeyMetadata {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSignatureWithKeyMetadata.Builder}.
   */
  static ImmutableSignatureWithKeyMetadata.Builder builder() {
    return ImmutableSignatureWithKeyMetadata.builder();
  }

  /**
   * A signature for a transaction, verifiable using the {@link SignatureWithKeyMetadata#signingKeyMetadata()}.
   *
   * @return A {@link Signature} containing the transaction signature.
   */
  Signature transactionSignature();

  /**
   * Metadata to describe the key used to create this signature.
   *
   * @return A {@link KeyMetadata} containing the public key used to sign the transaction.
   */
  KeyMetadata signingKeyMetadata();

}
