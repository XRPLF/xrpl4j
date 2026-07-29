package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Parameters that uniquely identify a {@link org.xrpl.xrpl4j.model.ledger.VaultObject} on ledger that can be used in a
 * {@link LedgerEntryRequestParams} to request a {@link org.xrpl.xrpl4j.model.ledger.VaultObject}.
 *
 * <p>This class will be marked {@link Beta} until the SingleAssetVault amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableVaultLedgerEntryParams.class)
@JsonDeserialize(as = ImmutableVaultLedgerEntryParams.class)
public interface VaultLedgerEntryParams {

  /**
   * Construct a {@code VaultLedgerEntryParams} builder.
   *
   * @return An {@link ImmutableVaultLedgerEntryParams.Builder}.
   */
  static ImmutableVaultLedgerEntryParams.Builder builder() {
    return ImmutableVaultLedgerEntryParams.builder();
  }

  /**
   * The owner of the Vault.
   *
   * @return The {@link Address} of the vault owner.
   */
  Address owner();

  /**
   * The Sequence Number of the transaction that created the Vault. If the transaction used a Ticket,
   * this should be the TicketSequence value.
   *
   * @return An {@link UnsignedInteger}.
   */
  UnsignedInteger seq();

}

