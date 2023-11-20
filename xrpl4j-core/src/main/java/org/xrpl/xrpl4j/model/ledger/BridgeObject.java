package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XChainAccountCreateCommit;
import org.xrpl.xrpl4j.model.transactions.XChainBridge;
import org.xrpl.xrpl4j.model.transactions.XChainClaimId;
import org.xrpl.xrpl4j.model.transactions.XChainCount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Optional;

/**
 * Represents a single cross-chain bridge that connects the XRP Ledger with another blockchain, such as its sidechain,
 * and enables value in the form of XRP and other tokens (IOUs) to move efficiently between the two blockchains.
 *
 * <p>This interface will be marked {@link Beta} until the featureXChainBridge amendment is enabled on mainnet. Its API
 * is subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableBridgeObject.class)
@JsonDeserialize(as = ImmutableBridgeObject.class)
public interface BridgeObject extends LedgerObject {

  /**
   * Construct a {@code BridgeObject} builder.
   *
   * @return An {@link ImmutableBridgeObject.Builder}.
   */
  static ImmutableBridgeObject.Builder builder() {
    return ImmutableBridgeObject.builder();
  }

  /**
   * The type of ledger object, which will always be "Bridge" in this case.
   *
   * @return Always returns {@link org.xrpl.xrpl4j.model.ledger.LedgerObject.LedgerEntryType#BRIDGE}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.BRIDGE;
  }

  /**
   * A bit-map of boolean flags. No flags are defined for {@link BridgeObject}, so this value is always 0.
   *
   * @return Always {@link Flags#UNSET}.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags flags() {
    return Flags.UNSET;
  }

  /**
   * The account that owns this object.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * The minimum amount, in XRP, required for an XChainAccountCreateCommit transaction. If this isn't present, the
   * {@link XChainAccountCreateCommit} transaction will fail. This field can only be present on XRP-XRP bridges.
   *
   * @return An optionally-present {@link XrpCurrencyAmount}.
   */
  @JsonProperty("MinAccountCreateAmount")
  Optional<XrpCurrencyAmount> minAccountCreateAmount();

  /**
   * The total amount, in XRP, to be rewarded for providing a signature for cross-chain transfer or for signing for the
   * cross-chain reward. This amount will be split among the signers.
   *
   * @return An {@link XrpCurrencyAmount}.
   */
  @JsonProperty("SignatureReward")
  XrpCurrencyAmount signatureReward();

  /**
   * The door accounts and assets of the bridge this object correlates to.
   *
   * @return An {@link XChainBridge}.
   */
  @JsonProperty("XChainBridge")
  XChainBridge xChainBridge();

  /**
   * The value of the next XChainClaimID to be created.
   *
   * @return An {@link XChainClaimId}.
   */
  @JsonProperty("XChainClaimID")
  XChainClaimId xChainClaimId();

  /**
   * A counter used to order the execution of account create transactions. It is incremented every time a successful
   * {@link XChainAccountCreateCommit} transaction is run for the source chain.
   *
   * @return An {@link XChainCount}.
   */
  @JsonProperty("XChainAccountCreateCount")
  XChainCount xChainAccountCreateCount();

  /**
   * A counter used to order the execution of account create transactions. It is incremented every time a
   * {@link XChainAccountCreateCommit} transaction is "claimed" on the destination chain. When the "claim" transaction
   * is run on the destination chain, the {@link #xChainAccountClaimCount()} must match the value that the
   * {@link #xChainAccountCreateCount()} had at the time the {@link #xChainAccountClaimCount()} was run on the source
   * chain. This orders the claims so that they run in the same order that the {@link XChainAccountCreateCommit}
   * transactions ran on the source chain, to prevent transaction replay.
   *
   * @return An {@link XChainCount}.
   */
  @JsonProperty("XChainAccountClaimCount")
  XChainCount xChainAccountClaimCount();

  /**
   * A hint indicating which page of the sender's owner directory links to this object, in case the directory consists
   * of multiple pages.
   *
   * <p>Note: The object does not contain a direct link to the owner directory containing it, since that value can be
   * derived from the Account.
   *
   * @return A {@link String} containing the owner node hint.
   */
  @JsonProperty("OwnerNode")
  String ownerNode();

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
   * The unique ID of the {@link BridgeObject}.
   *
   * @return A {@link Hash256} containing the ID.
   */
  Hash256 index();
}
