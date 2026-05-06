package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

/**
 * Delete a single asset vault. The vault must have no remaining assets or outstanding shares.
 *
 * <p>This class will be marked {@link Beta} until the SingleAssetVault amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableVaultDelete.class)
@JsonDeserialize(as = ImmutableVaultDelete.class)
@Beta
public interface VaultDelete extends Transaction {

  /**
   * Construct a {@code VaultDelete} builder.
   *
   * @return An {@link ImmutableVaultDelete.Builder}.
   */
  static ImmutableVaultDelete.Builder builder() {
    return ImmutableVaultDelete.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link VaultDelete}.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The ID of the Vault to delete.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("VaultID")
  Hash256 vaultId();

}
