package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.VaultCreateFlags;
import org.xrpl.xrpl4j.model.ledger.Issue;

import java.util.Optional;

/**
 * Create a single asset vault. The vault aggregates assets from depositors and uses Multi-Purpose-Tokens
 * to represent ownership shares.
 *
 * <p>This class will be marked {@link Beta} until the SingleAssetVault amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableVaultCreate.class)
@JsonDeserialize(as = ImmutableVaultCreate.class)
@Beta
public interface VaultCreate extends Transaction {

  /**
   * Construct a {@code VaultCreate} builder.
   *
   * @return An {@link ImmutableVaultCreate.Builder}.
   */
  static ImmutableVaultCreate.Builder builder() {
    return ImmutableVaultCreate.builder();
  }

  /**
   * Set of {@link VaultCreateFlags}s for this {@link VaultCreate}.
   *
   * @return A {@link VaultCreateFlags}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default VaultCreateFlags flags() {
    return VaultCreateFlags.empty();
  }

  /**
   * The asset type that this vault holds. Can be XRP, IOU, or MPT.
   *
   * @return An {@link Issue}.
   */
  @JsonProperty("Asset")
  Issue asset();

  /**
   * The maximum amount of assets the vault can hold. 0 means no cap.
   *
   * @return An optionally-present {@link AssetAmount}.
   */
  @JsonProperty("AssetsMaximum")
  Optional<AssetAmount> assetsMaximum();

  /**
   * Metadata for the share MPToken, in hex format. Limited to 1024 bytes.
   *
   * @return An optionally-present {@link MpTokenMetadata}.
   */
  @JsonProperty("MPTokenMetadata")
  Optional<MpTokenMetadata> mpTokenMetadata();

  /**
   * The PermissionedDomain object ID for private vaults.
   *
   * @return An optionally-present {@link Hash256}.
   */
  @JsonProperty("DomainID")
  Optional<Hash256> domainId();

  /**
   * The withdrawal strategy. The only defined value is {@code 1} (first-come-first-serve).
   *
   * @return An optionally-present {@link WithdrawalPolicy}.
   */
  @JsonProperty("WithdrawalPolicy")
  Optional<WithdrawalPolicy> withdrawalPolicy();

  /**
   * Arbitrary vault metadata, limited to 256 bytes, in hex format.
   *
   * @return An optionally-present {@link VaultData}.
   */
  @JsonProperty("Data")
  Optional<VaultData> data();

  /**
   * Power of 10 for asset-to-share conversion. Default is 6. Only applicable for IOU assets.
   *
   * @return An optionally-present {@link AssetScale}.
   */
  @JsonProperty("Scale")
  Optional<AssetScale> scale();

  /**
   * Validate VaultCreate preconditions.
   */
  @Value.Check
  default void check() {
    domainId().ifPresent(domain -> {
      if (!flags().tfVaultPrivate()) {
        throw new IllegalArgumentException("DomainID is only allowed when the tfVaultPrivate flag is set.");
      }
    });

    scale().ifPresent(scaleValue -> asset().handle(
      xrpIssue -> {
        throw new IllegalArgumentException("Scale is only allowed for IOU assets.");
      },
      iouIssue -> {
        long scaleVal = scaleValue.value().longValue();
        if (scaleVal > 18) {
          throw new IllegalArgumentException("Scale must be between 0 and 18.");
        }
      },
      mptIssue -> {
        throw new IllegalArgumentException("Scale is only allowed for IOU assets.");
      }
    ));
  }

}
