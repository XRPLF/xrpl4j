package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Parameters that uniquely identify a {@link org.xrpl.xrpl4j.model.ledger.DepositPreAuthObject} on ledger that can be
 * used in a {@link LedgerEntryRequestParams} to request an {@link org.xrpl.xrpl4j.model.ledger.DepositPreAuthObject}.
 */
@Immutable
@JsonSerialize(as = ImmutableDepositPreAuthLedgerEntryParams.class)
@JsonDeserialize(as = ImmutableDepositPreAuthLedgerEntryParams.class)
public interface DepositPreAuthLedgerEntryParams {

  /**
   * Construct a {@code DepositPreAuthLedgerEntryParams} builder.
   *
   * @return An {@link ImmutableDepositPreAuthLedgerEntryParams.Builder}.
   */
  static ImmutableDepositPreAuthLedgerEntryParams.Builder builder() {
    return ImmutableDepositPreAuthLedgerEntryParams.builder();
  }

  /**
   * The {@link Address} of the account that provided the preauthorization.
   *
   * @return An {@link Address}.
   */
  Address owner();

  /**
   * The {@link Address} of the account that received the preauthorization.
   *
   * @return An {@link Address}.
   */
  Address authorized();

}
