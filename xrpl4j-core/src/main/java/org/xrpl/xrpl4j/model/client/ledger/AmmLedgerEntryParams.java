package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.ledger.Issue;

/**
 * Parameters that uniquely identify an {@link org.xrpl.xrpl4j.model.ledger.AmmObject} on ledger that can be used
 * in a {@link LedgerEntryRequestParams} to request an {@link org.xrpl.xrpl4j.model.ledger.AmmObject}.
 */
@Immutable
@JsonSerialize(as = ImmutableAmmLedgerEntryParams.class)
@JsonDeserialize(as = ImmutableAmmLedgerEntryParams.class)
public interface AmmLedgerEntryParams {

  /**
   * Construct a {@code AmmLedgerEntryParams} builder.
   *
   * @return An {@link ImmutableAmmLedgerEntryParams.Builder}.
   */
  static ImmutableAmmLedgerEntryParams.Builder builder() {
    return ImmutableAmmLedgerEntryParams.builder();
  }

  /**
   * One of the two assets in the AMM's pool.
   *
   * @return An {@link Issue}.
   */
  Issue asset();

  /**
   * The other of the two assets in the AMM's pool.
   *
   * @return An {@link Issue}.
   */
  Issue asset2();

}
