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
import org.xrpl.xrpl4j.model.transactions.XChainBridge;
import org.xrpl.xrpl4j.model.transactions.XChainCount;

import java.util.List;

/**
 * The {@code XChainOwnedCreateAccountClaimID} ledger object is used to collect attestations for creating an account via
 * a cross-chain transfer.
 *
 * <p>It is created when an XChainAddAccountCreateAttestation transaction adds a signature attesting to a
 * XChainAccountCreateCommit transaction and the XChainAccountCreateCount is greater than or equal to the current
 * XChainAccountClaimCount on the Bridge ledger object.</p>
 *
 * <p>The ledger object is destroyed when all the attestations have been received and the funds have transferred to the
 * new account.</p>
 *
 * <p>This interface will be marked {@link Beta} until the featureXChainBridge amendment is enabled on mainnet. Its API
 * is subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableXChainOwnedCreateAccountClaimIdObject.class)
@JsonDeserialize(as = ImmutableXChainOwnedCreateAccountClaimIdObject.class)
public interface XChainOwnedCreateAccountClaimIdObject extends LedgerObject {

  /**
   * Construct a {@code XChainOwnedCreateAccountClaimIdObject} builder.
   *
   * @return An {@link ImmutableXChainOwnedCreateAccountClaimIdObject.Builder}.
   */
  static ImmutableXChainOwnedCreateAccountClaimIdObject.Builder builder() {
    return ImmutableXChainOwnedCreateAccountClaimIdObject.builder();
  }

  /**
   * The type of ledger object, which will always be "XChainOwnedCreateAccountClaimIdObject" in this case.
   *
   * @return Always returns {@link LedgerEntryType#XCHAIN_OWNED_CREATE_ACCOUNT_CLAIM_ID}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.XCHAIN_OWNED_CREATE_ACCOUNT_CLAIM_ID;
  }

  /**
   * A bit-map of boolean flags. No flags are defined for {@link XChainOwnedCreateAccountClaimIdObject}, so this value
   * is always 0.
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
   * The door accounts and assets of the bridge this object correlates to.
   *
   * @return An {@link XChainBridge}.
   */
  @JsonProperty("XChainBridge")
  @SuppressWarnings("MethodName")
  XChainBridge xChainBridge();

  /**
   * Attestations collected from the witness servers. This includes the parameters needed to recreate the message that
   * was signed, including the amount, destination, signature reward amount, and reward account for that signature. With
   * the exception of the reward account, all signatures must sign the message created with common parameters.
   *
   * @return A {@link List} of {@link XChainCreateAccountAttestation}s.
   */
  @JsonProperty("XChainCreateAccountAttestations")
  @SuppressWarnings("MethodName")
  List<XChainCreateAccountAttestation> xChainCreateAccountAttestations();

  /**
   * An integer that determines the order that accounts created through cross-chain transfers must be performed. Smaller
   * numbers must execute before larger numbers.
   *
   * @return An {@link XChainCount}.
   */
  @JsonProperty("XChainAccountCreateCount")
  @SuppressWarnings("MethodName")
  XChainCount xChainAccountCreateCount();

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
   * The unique ID of the {@link XChainOwnedCreateAccountClaimIdObject}.
   *
   * @return A {@link Hash256} containing the ID.
   */
  Hash256 index();
}
