package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Parameters that uniquely identifies a {@link org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject} on ledger that
 * can be used in a {@link LedgerEntryRequestParams} to request
 * {@link org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject}.
 */
@Immutable
@JsonSerialize(as = ImmutablePermissionedDomainLedgerEntryParams.class)
@JsonDeserialize(as = ImmutablePermissionedDomainLedgerEntryParams.class)
public interface PermissionedDomainLedgerEntryParams {

  /**
   * Construct a {@code PermissionedDomainLedgerEntryParams} builder.
   *
   * @return An {@link ImmutablePermissionedDomainLedgerEntryParams.Builder}.
   */
  static ImmutablePermissionedDomainLedgerEntryParams.Builder builder() {
    return ImmutablePermissionedDomainLedgerEntryParams.builder();
  }

  /**
   * The owner of the permissioned domain.
   *
   * @return The unique {@link Address} of the owner of this
   *   {@link org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject}.
   */
  Address account();

  /**
   * The Sequence Number of the transaction that created the
   * {@link org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject}.
   *
   * @return An {@link UnsignedInteger} representing transaction sequence.
   */
  UnsignedInteger seq();
}