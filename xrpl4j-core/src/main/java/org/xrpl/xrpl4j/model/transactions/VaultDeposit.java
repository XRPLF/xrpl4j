package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

/**
 * Deposit assets into a single asset vault in exchange for vault shares.
 *
 * <p>This class will be marked {@link Beta} until the SingleAssetVault amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableVaultDeposit.class)
@JsonDeserialize(as = ImmutableVaultDeposit.class)
@Beta
public interface VaultDeposit extends Transaction {

  /**
   * Construct a {@code VaultDeposit} builder.
   *
   * @return An {@link ImmutableVaultDeposit.Builder}.
   */
  static ImmutableVaultDeposit.Builder builder() {
    return ImmutableVaultDeposit.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link VaultDeposit}.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The ID of the Vault to deposit into.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("VaultID")
  Hash256 vaultId();

  /**
   * The amount of vault asset to deposit.
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("Amount")
  CurrencyAmount amount();

}
