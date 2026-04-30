package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * Withdraw assets from a single asset vault by redeeming vault shares. The Amount can be specified
 * in the vault's asset (withdraw) or in vault shares (redeem).
 *
 * <p>This class will be marked {@link Beta} until the SingleAssetVault amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableVaultWithdraw.class)
@JsonDeserialize(as = ImmutableVaultWithdraw.class)
@Beta
public interface VaultWithdraw extends Transaction {

  /**
   * Construct a {@code VaultWithdraw} builder.
   *
   * @return An {@link ImmutableVaultWithdraw.Builder}.
   */
  static ImmutableVaultWithdraw.Builder builder() {
    return ImmutableVaultWithdraw.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link VaultWithdraw}.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The ID of the Vault to withdraw from.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("VaultID")
  Hash256 vaultId();

  /**
   * The amount to withdraw. Can be vault asset (withdraw) or vault shares (redeem).
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("Amount")
  CurrencyAmount amount();

  /**
   * The account to receive the withdrawn assets. If omitted, the submitting account receives the assets.
   *
   * @return An optionally-present {@link Address}.
   */
  @JsonProperty("Destination")
  Optional<Address> destination();

  /**
   * A tag for the destination account.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

}
