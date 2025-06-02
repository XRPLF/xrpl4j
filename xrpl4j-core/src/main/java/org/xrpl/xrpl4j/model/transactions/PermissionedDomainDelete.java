package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

/**
 * This transaction deletes a PermissionedDomain object.
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePermissionedDomainDelete.class)
@JsonDeserialize(as = ImmutablePermissionedDomainDelete.class)
public interface PermissionedDomainDelete extends Transaction {

  /**
   * Construct a {@code PermissionedDomainDelete} builder.
   *
   * @return An {@link ImmutablePermissionedDomainDelete.Builder}.
   */
  static ImmutablePermissionedDomainDelete.Builder builder() {
    return ImmutablePermissionedDomainDelete.builder();
  }

  /**
   * The ledger entry ID of an existing permissioned domain to delete.
   *
   * @return A {@link Hash256} representing DomainID.
   */
  @JsonProperty("DomainID")
  Hash256 domainId();

  /**
   * Set of {@link TransactionFlags}'s for this {@link PermissionedDomainDelete}, which only allows the
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
