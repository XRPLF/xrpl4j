package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * Clawback assets from a vault holder. Must be submitted by the asset issuer. Cannot clawback XRP.
 *
 * <p>This class will be marked {@link Beta} until the SingleAssetVault amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableVaultClawback.class)
@JsonDeserialize(as = ImmutableVaultClawback.class)
@Beta
public interface VaultClawback extends Transaction {

  /**
   * Construct a {@code VaultClawback} builder.
   *
   * @return An {@link ImmutableVaultClawback.Builder}.
   */
  static ImmutableVaultClawback.Builder builder() {
    return ImmutableVaultClawback.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link VaultClawback}.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The ID of the Vault to clawback from.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("VaultID")
  Hash256 vaultId();

  /**
   * The account from which to clawback vault shares.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Holder")
  Address holder();

  /**
   * The amount to clawback. When Amount is 0 clawback all funds, up to the total shares the Holder owns.
   *
   * @return An optionally-present {@link CurrencyAmount}.
   */
  @JsonProperty("Amount")
  Optional<CurrencyAmount> amount();

  @Value.Check
  default void check() {
    amount().ifPresent(amt -> amt.handle(
      xrpAmount -> {
        throw new IllegalArgumentException(
          "VaultClawback amount cannot be XRP."
        );
      },
      issuedCurrencyAmount -> {},
      mptCurrencyAmount -> {}
    ));
  }

}
