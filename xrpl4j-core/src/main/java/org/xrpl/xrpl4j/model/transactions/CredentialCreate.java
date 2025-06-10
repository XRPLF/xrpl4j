package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * A {@link CredentialCreate} transaction creates a `Credential` object in the ledger. The sender of this transaction is
 * the issuer of the credential.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCredentialCreate.class)
@JsonDeserialize(as = ImmutableCredentialCreate.class)
public interface CredentialCreate extends Transaction {

  /**
   * Construct a {@code CredentialCreate} builder.
   *
   * @return An {@link ImmutableCredentialCreate.Builder}.
   */
  static ImmutableCredentialCreate.Builder builder() {
    return ImmutableCredentialCreate.builder();
  }

  /**
   * The subject of the credential.
   *
   * @return The unique {@link Address} of the subject this credential.
   */
  @JsonProperty("Subject")
  Address subject();

  /**
   * A (hex-encoded) value to identify the type of credential from the issuer.
   *
   * @return A {@link CredentialType} defining the type of credential.
   */
  @JsonProperty("CredentialType")
  CredentialType credentialType();

  /**
   * Time after which this credential expires.
   *
   * @return An {@link Optional} of type {@link UnsignedLong} representing the credential's expiration time.
   */
  @JsonProperty("Expiration")
  Optional<UnsignedLong> expiration();

  /**
   * Arbitrary additional data about the credential, such as the URL where users can look up an associated Verifiable
   * Credential document.
   *
   * @return An optionally-present {@link CredentialUri}.
   */
  @JsonProperty("URI")
  Optional<CredentialUri> uri();

  /**
   * Set of {@link TransactionFlags}'s for this {@link CredentialCreate}, which only allows the
   * {@code tfFullyCanonicalSig} flag, which is deprecated.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }
}
