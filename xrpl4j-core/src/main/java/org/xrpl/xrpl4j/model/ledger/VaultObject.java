package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.VaultFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Amount;
import org.xrpl.xrpl4j.model.transactions.AssetScale;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.VaultData;
import org.xrpl.xrpl4j.model.transactions.WithdrawalPolicy;

import java.util.Optional;

/**
 * Represents a Vault ledger object, which describes a single asset vault instance.
 *
 * <p>This class will be marked {@link Beta} until the SingleAssetVault amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableVaultObject.class)
@JsonDeserialize(as = ImmutableVaultObject.class)
@Beta
public interface VaultObject extends LedgerObject {

  /**
   * Construct a {@code VaultObject} builder.
   *
   * @return An {@link ImmutableVaultObject.Builder}.
   */
  static ImmutableVaultObject.Builder builder() {
    return ImmutableVaultObject.builder();
  }

  /**
   * The type of ledger object, which will always be "Vault" in this case.
   *
   * @return Always returns {@link LedgerEntryType#VAULT}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.VAULT;
  }

  /**
   * A bit-map of boolean flags for this vault.
   *
   * @return A {@link VaultFlags}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default VaultFlags flags() {
    return VaultFlags.UNSET;
  }

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link UnsignedInteger} representing the previous transaction ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * The sequence number of the transaction that created this vault.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  /**
   * A hint indicating which page of the owner's directory links to this object.
   *
   * @return A {@link String} containing the owner node hint.
   */
  @JsonProperty("OwnerNode")
  String ownerNode();

  /**
   * The address of the vault owner.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Owner")
  Address owner();

  /**
   * The address of the vault pseudo-account.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * Arbitrary metadata about this vault, in hex format, limited to 256 bytes.
   *
   * @return An optionally-present {@link VaultData}.
   */
  @JsonProperty("Data")
  Optional<VaultData> data();

  /**
   * The asset type that this vault holds.
   *
   * @return An {@link Issue}.
   */
  @JsonProperty("Asset")
  Issue asset();

  /**
   * The total value of the vault.
   *
   * @return An {@link Amount}, defaulting to "0".
   */
  @JsonProperty("AssetsTotal")
  @Value.Default
  default Amount assetsTotal() {
    return Amount.ZERO;
  }

  /**
   * The available assets in the vault.
   *
   * @return An {@link Amount}, defaulting to "0".
   */
  @JsonProperty("AssetsAvailable")
  @Value.Default
  default Amount assetsAvailable() {
    return Amount.ZERO;
  }

  /**
   * The maximum amount of assets the vault can hold. 0 means no cap.
   *
   * @return An {@link Amount}, defaulting to "0".
   */
  @JsonProperty("AssetsMaximum")
  @Value.Default
  default Amount assetsMaximum() {
    return Amount.ZERO;
  }

  /**
   * The potential unrealized loss.
   *
   * @return An {@link Amount}, defaulting to "0".
   */
  @JsonProperty("LossUnrealized")
  @Value.Default
  default Amount lossUnrealized() {
    return Amount.ZERO;
  }

  /**
   * The ID of the share MPTokenIssuance.
   *
   * @return A {@link MpTokenIssuanceId}.
   */
  @JsonProperty("ShareMPTID")
  MpTokenIssuanceId shareMptId();

  /**
   * The withdrawal strategy. The only defined value is {@code 1} (first-come-first-serve).
   *
   * @return A {@link WithdrawalPolicy}.
   */
  @JsonProperty("WithdrawalPolicy")
  WithdrawalPolicy withdrawalPolicy();

  /**
   * Power of 10 for asset-to-share conversion.
   *
   * @return An {@link AssetScale}, defaulting to 0.
   */
  @JsonProperty("Scale")
  @Value.Default
  default AssetScale scale() {
    return AssetScale.of(UnsignedInteger.ZERO);
  }

  /**
   * The share MPTokenIssuance information for this vault. Only present in {@code vault_info} RPC responses.
   *
   * @return An optionally-present {@link MpTokenIssuanceObject}.
   */
  @JsonProperty("shares")
  Optional<MpTokenIssuanceObject> shares();

  /**
   * The unique ID of this {@link VaultObject}.
   *
   * @return A {@link Hash256}.
   */
  Hash256 index();

}
