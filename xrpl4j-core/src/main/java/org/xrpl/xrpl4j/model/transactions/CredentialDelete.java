package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * A CredentialDelete transaction removes a credential from the ledger, effectively revoking it.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCredentialDelete.class)
@JsonDeserialize(as = ImmutableCredentialDelete.class)
public interface CredentialDelete extends Transaction {

  /**
   * Construct a {@code CredentialDelete} builder.
   *
   * @return An {@link ImmutableCredentialDelete.Builder}.
   */
  static ImmutableCredentialDelete.Builder builder() {
    return ImmutableCredentialDelete.builder();
  }

  /**
   * The issuer of the credential.
   *
   * @return The unique {@link Address} of the issuer this credential.
   */
  @JsonProperty("Issuer")
  Optional<Address> issuer();

  /**
   * The subject of the credential.
   *
   * @return The unique {@link Address} of the subject this credential.
   */
  @JsonProperty("Subject")
  Optional<Address> subject();

  /**
   * A (hex-encoded) value to identify the type of credential from the issuer.
   *
   * @return A {@link CredentialType} defining the type of credential.
   */
  @JsonProperty("CredentialType")
  CredentialType credentialType();

  /**
   * Set of {@link TransactionFlags}'s for this {@link CredentialDelete}, which only allows the
   * {@code tfFullyCanonicalSig} flag, which is deprecated.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * Validate either Subject or Issuer's presence.
   */
  @Value.Check
  default void check() {
    Preconditions.checkState(subject().isPresent() || issuer().isPresent(),
      "Either Subject or Issuer must be present."
    );
  }

}
