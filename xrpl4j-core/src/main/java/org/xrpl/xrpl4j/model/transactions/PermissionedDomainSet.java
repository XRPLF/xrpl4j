package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Create a PermissionedDomain object in the ledger. The sender of this transaction is the issuer of the credential.
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePermissionedDomainSet.class)
@JsonDeserialize(as = ImmutablePermissionedDomainSet.class)
public interface PermissionedDomainSet extends Transaction {

  /**
   * Construct a {@code PermissionedDomainSet} builder.
   *
   * @return An {@link ImmutablePermissionedDomainSet.Builder}.
   */
  static ImmutablePermissionedDomainSet.Builder builder() {
    return ImmutablePermissionedDomainSet.builder();
  }

  /**
   * The ledger entry ID of an existing permissioned domain to modify.
   * If omitted, creates a new permissioned domain.
   *
   * @return A {@link Hash256} representing DomainID.
   */
  @JsonProperty("DomainID")
  Optional<Hash256> domainId();

  /**
   * The credentials that are accepted by the domain.
   * Ownership of one of these credentials automatically makes you a member of the domain.
   * When modifying an existing domain, this list replaces the existing list.
   *
   * @return A {@link CredentialType} defining the type of credential.
   */
  @JsonProperty("AcceptedCredentials")
  List<CredentialWrapper> acceptedCredentials();

  /**
   * Set of {@link TransactionFlags}'s for this {@link PermissionedDomainSet}, which only allows the
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
   * Validate {@link PermissionedDomainSet#acceptedCredentials} has less than or equal to 10 credentials.
   */
  @Value.Check
  default void validateCredentialList() {
    Preconditions.checkArgument(
      !acceptedCredentials().isEmpty() && acceptedCredentials().size() <= 10,
      "AcceptedCredentials shouldn't be empty and must have less than or equal to 10 credentials."
    );
  }

  /**
   * Validate {@link PermissionedDomainSet#acceptedCredentials} has unique credentials.
   */
  @Value.Check
  default void validateForUniqueValues() {
    Preconditions.checkArgument(
      new HashSet<>(acceptedCredentials()).size() == acceptedCredentials().size(),
      "AcceptedCredentials should have unique credentials."
    );
  }
}
