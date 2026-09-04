package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.VaultSetFlags;

import java.util.Optional;

/**
 * Update a single asset vault's mutable fields.
 *
 * <p>This class will be marked {@link Beta} until the SingleAssetVault amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableVaultSet.class)
@JsonDeserialize(as = ImmutableVaultSet.class)
@Beta
public interface VaultSet extends Transaction {

  /**
   * Construct a {@code VaultSet} builder.
   *
   * @return An {@link ImmutableVaultSet.Builder}.
   */
  static ImmutableVaultSet.Builder builder() {
    return ImmutableVaultSet.builder();
  }

  /**
   * Set of {@link VaultSetFlags}s for this {@link VaultSet}.
   *
   * @return A {@link VaultSetFlags}, which defaults to {@link VaultSetFlags#empty()}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default VaultSetFlags flags() {
    return VaultSetFlags.empty();
  }

  /**
   * The ID of the Vault to modify.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("VaultID")
  Hash256 vaultId();

  /**
   * The maximum amount of assets the vault can hold. 0 means no cap.
   *
   * @return An optionally-present {@link Amount}.
   */
  @JsonProperty("AssetsMaximum")
  Optional<Amount> assetsMaximum();

  /**
   * The PermissionedDomain object ID for private vaults.
   *
   * @return An optionally-present {@link Hash256}.
   */
  @JsonProperty("DomainID")
  Optional<Hash256> domainId();

  /**
   * Arbitrary vault metadata, limited to 256 bytes, in hex format.
   *
   * @return An optionally-present {@link VaultData}.
   */
  @JsonProperty("Data")
  Optional<VaultData> data();

  /**
   * Validate VaultSet preconditions.
   */
  @Value.Check
  default void check() {
    if (flags().tfVaultDepositBlock() && flags().tfVaultDepositUnblock()) {
      throw new IllegalArgumentException(
        "tfVaultDepositBlock and tfVaultDepositUnblock cannot both be set."
      );
    }
  }

}
