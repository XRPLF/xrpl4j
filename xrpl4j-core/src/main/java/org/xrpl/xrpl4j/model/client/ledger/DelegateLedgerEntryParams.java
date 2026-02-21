package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Parameters that uniquely identify a {@link org.xrpl.xrpl4j.model.ledger.DelegateObject} on ledger that can be
 * used in a {@link LedgerEntryRequestParams} to request a {@link org.xrpl.xrpl4j.model.ledger.DelegateObject}.
 *
 * <p>This class will be marked {@link Beta} until the featurePermissionDelegation amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 */
@Immutable
@JsonSerialize(as = ImmutableDelegateLedgerEntryParams.class)
@JsonDeserialize(as = ImmutableDelegateLedgerEntryParams.class)
@Beta
public interface DelegateLedgerEntryParams {

  /**
   * Construct a {@code DelegateLedgerEntryParams} builder.
   *
   * @return An {@link ImmutableDelegateLedgerEntryParams.Builder}.
   */
  static ImmutableDelegateLedgerEntryParams.Builder builder() {
    return ImmutableDelegateLedgerEntryParams.builder();
  }

  /**
   * The account that wants to authorize another account (the delegating account).
   *
   * @return An {@link Address}.
   */
  Address account();

  /**
   * The authorized account (the delegate).
   *
   * @return An {@link Address}.
   */
  Address authorize();
}

