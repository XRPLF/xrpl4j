package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.VaultFlags;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetScale;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.NumberAmount;
import org.xrpl.xrpl4j.model.transactions.VaultData;
import org.xrpl.xrpl4j.model.transactions.WithdrawalPolicy;

import java.util.Optional;

/**
 * Represents a Vault ledger object as it appears in transaction metadata.
 *
 * <p>This class will be marked {@link Beta} until the SingleAssetVault amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Immutable
@JsonSerialize(as = ImmutableMetaVaultObject.class)
@JsonDeserialize(as = ImmutableMetaVaultObject.class)
@Beta
public interface MetaVaultObject extends MetaLedgerObject {

  /**
   * A bit-map of boolean flags for this vault.
   *
   * @return An optionally-present {@link VaultFlags}.
   */
  @JsonProperty("Flags")
  Optional<VaultFlags> flags();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return An optionally-present {@link Hash256}.
   */
  @JsonProperty("PreviousTxnID")
  Optional<Hash256> previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  Optional<UnsignedInteger> previousTransactionLedgerSequence();

  /**
   * The sequence number of the transaction that created this vault.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("Sequence")
  Optional<UnsignedInteger> sequence();

  /**
   * A hint indicating which page of the owner's directory links to this object.
   *
   * @return An optionally-present {@link String}.
   */
  @JsonProperty("OwnerNode")
  Optional<String> ownerNode();

  /**
   * The address of the vault owner.
   *
   * @return An optionally-present {@link Address}.
   */
  @JsonProperty("Owner")
  Optional<Address> owner();

  /**
   * The address of the vault pseudo-account.
   *
   * @return An optionally-present {@link Address}.
   */
  @JsonProperty("Account")
  Optional<Address> account();

  /**
   * Arbitrary metadata about this vault.
   *
   * @return An optionally-present {@link String}.
   */
  @JsonProperty("Data")
  Optional<VaultData> data();

  /**
   * The asset type that this vault holds.
   *
   * @return An optionally-present {@link Issue}.
   */
  @JsonProperty("Asset")
  Optional<Issue> asset();

  /**
   * The total value of the vault.
   *
   * @return An optionally-present {@link NumberAmount}.
   */
  @JsonProperty("AssetsTotal")
  Optional<NumberAmount> assetsTotal();

  /**
   * The available assets in the vault.
   *
   * @return An optionally-present {@link NumberAmount}.
   */
  @JsonProperty("AssetsAvailable")
  Optional<NumberAmount> assetsAvailable();

  /**
   * The maximum amount of assets the vault can hold.
   *
   * @return An optionally-present {@link NumberAmount}.
   */
  @JsonProperty("AssetsMaximum")
  Optional<NumberAmount> assetsMaximum();

  /**
   * The potential unrealized loss.
   *
   * @return An optionally-present {@link NumberAmount}.
   */
  @JsonProperty("LossUnrealized")
  Optional<NumberAmount> lossUnrealized();

  /**
   * The ID of the share MPTokenIssuance.
   *
   * @return An optionally-present {@link MpTokenIssuanceId}.
   */
  @JsonProperty("ShareMPTID")
  Optional<MpTokenIssuanceId> shareMptId();

  /**
   * The withdrawal strategy.
   *
   * @return An optionally-present {@link WithdrawalPolicy}.
   */
  @JsonProperty("WithdrawalPolicy")
  Optional<WithdrawalPolicy> withdrawalPolicy();

  /**
   * Power of 10 for asset-to-share conversion.
   *
   * @return An optionally-present {@link AssetScale}.
   */
  @JsonProperty("Scale")
  Optional<AssetScale> scale();

}
