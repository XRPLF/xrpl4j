package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Parameters that uniquely identify an {@link org.xrpl.xrpl4j.model.ledger.EscrowObject} on ledger that can be used in
 * a {@link LedgerEntryRequestParams} to request an {@link org.xrpl.xrpl4j.model.ledger.EscrowObject}.
 */
@Immutable
@JsonSerialize(as = ImmutableEscrowLedgerEntryParams.class)
@JsonDeserialize(as = ImmutableEscrowLedgerEntryParams.class)
public interface EscrowLedgerEntryParams {

  /**
   * Construct a {@code EscrowLedgerEntryParams} builder.
   *
   * @return An {@link ImmutableEscrowLedgerEntryParams.Builder}.
   */
  static ImmutableEscrowLedgerEntryParams.Builder builder() {
    return ImmutableEscrowLedgerEntryParams.builder();
  }

  /**
   * The owner (sender) of the Escrow object.
   *
   * @return The {@link Address} of the owner.
   */
  Address owner();

  /**
   * The Sequence Number of the transaction that created the Escrow object.
   *
   * @return An {@link UnsignedInteger}.
   */
  UnsignedInteger seq();

}
