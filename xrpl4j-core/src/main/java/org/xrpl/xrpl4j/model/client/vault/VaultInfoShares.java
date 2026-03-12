package org.xrpl.xrpl4j.model.client.vault;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceFlags;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;

import java.util.Optional;

/**
 * Represents the shares (MPTokenIssuance) sub-object returned in a {@code vault_info} response.
 *
 * <p>This class will be marked {@link Beta} until the SingleAssetVault amendment is enabled on mainnet. Its API is
 * subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableVaultInfoShares.class)
@JsonDeserialize(as = ImmutableVaultInfoShares.class)
@Beta
public interface VaultInfoShares {

  /**
   * Construct a {@code VaultInfoShares} builder.
   *
   * @return An {@link ImmutableVaultInfoShares.Builder}.
   */
  static ImmutableVaultInfoShares.Builder builder() {
    return ImmutableVaultInfoShares.builder();
  }

  /**
   * The flags for this MPTokenIssuance.
   *
   * @return A {@link MpTokenIssuanceFlags}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default MpTokenIssuanceFlags flags() {
    return MpTokenIssuanceFlags.UNSET;
  }

  /**
   * The issuer address (always the vault pseudo-account).
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Issuer")
  Address issuer();

  /**
   * The ledger entry type (always "MPTokenIssuance").
   *
   * @return A {@link LedgerObject.LedgerEntryType}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerObject.LedgerEntryType ledgerEntryType() {
    return LedgerObject.LedgerEntryType.MP_TOKEN_ISSUANCE;
  }

  /**
   * The total outstanding amount of shares.
   *
   * @return An {@link MpTokenNumericAmount}.
   */
  @JsonProperty("OutstandingAmount")
  MpTokenNumericAmount outstandingAmount();

  /**
   * A hint indicating which page of the owner directory links to this object.
   *
   * @return An optionally-present {@link String}.
   */
  @JsonProperty("OwnerNode")
  Optional<String> ownerNode();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256}.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * The sequence number of the account that issued this token.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  /**
   * The unique ID of this object in the ledger.
   *
   * @return A {@link Hash256}.
   */
  Hash256 index();

  /**
   * The MPTokenIssuanceId for this shares issuance. Only present in some responses.
   *
   * @return An optionally-present {@link MpTokenIssuanceId}.
   */
  @JsonProperty("mpt_issuance_id")
  Optional<MpTokenIssuanceId> mpTokenIssuanceId();

  /**
   * The PermissionedDomain ID associated with this issuance. Only present for private vaults.
   *
   * @return An optionally-present {@link Hash256}.
   */
  @JsonProperty("DomainID")
  Optional<Hash256> domainId();

}
