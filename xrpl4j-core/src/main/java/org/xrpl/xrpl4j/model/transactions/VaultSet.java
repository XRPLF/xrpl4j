package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

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
   * Set of {@link TransactionFlags}s for this {@link VaultSet}, which only allows the
   * {@code tfFullyCanonicalSig} flag, which is deprecated.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
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
   * @return An optionally-present {@link AssetAmount}.
   */
  @JsonProperty("AssetsMaximum")
  Optional<AssetAmount> assetsMaximum();

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

}
