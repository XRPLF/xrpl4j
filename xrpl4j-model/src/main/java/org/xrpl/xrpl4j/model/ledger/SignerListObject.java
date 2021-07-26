package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.accounts.ImmutableAccountChannelsRequestParams;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.List;

/**
 * Represents a list of parties that, as a group, are authorized to sign a {@link Transaction} in place of an
 * individual account. You can create, replace, or remove a signer list using a {@link SignerListSet} transaction.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSignerListObject.class)
@JsonDeserialize(as = ImmutableSignerListObject.class)
public interface SignerListObject extends LedgerObject {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSignerListObject.Builder}.
   */
  static ImmutableSignerListObject.Builder builder() {
    return ImmutableSignerListObject.builder();
  }

  /**
   * The type of ledger object. In this case, always "SignerList".
   *
   * @return Always {@link org.xrpl.xrpl4j.model.ledger.LedgerObject.LedgerEntryType#SIGNER_LIST}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.SIGNER_LIST;
  }

  /**
   * A bit-map of Boolean {@link Flags.SignerListFlags} enabled for this signer list.
   *
   * @return The {@link org.xrpl.xrpl4j.model.flags.Flags.SignerListFlags} for this object.
   */
  @JsonProperty("Flags")
  Flags.SignerListFlags flags();

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
   * A hint indicating which page of the owner directory links to this object, in case the directory
   * consists of multiple pages.
   *
   * @return A {@link String} containing the hint.
   */
  @JsonProperty("OwnerNode")
  String ownerNode();

  /**
   * An ID for this signer list. Currently always set to 0. If a future amendment allows multiple
   * signer lists for an account, this may change.
   *
   * @return An {@link UnsignedInteger} representing the ID.
   */
  @JsonProperty("SignerListID")
  UnsignedInteger signerListId();

  /**
   * A target number for signer weights. To produce a valid signature for the owner of this {@link SignerListObject},
   * the signers must provide valid signatures whose weights sum to this value or more.
   *
   * @return An {@link UnsignedInteger} representing the signer quorum.
   */
  @JsonProperty("SignerQuorum")
  UnsignedInteger signerQuorum();

  /**
   * A {@link List} of {@link SignerEntry} objects representing the parties who are part of this signer list.
   *
   * @return A {@link List} of {@link SignerEntryWrapper}s for this signer list.
   */
  @JsonProperty("SignerEntries")
  List<SignerEntryWrapper> signerEntries();

  /**
   * Unique ID for this {@link SignerListObject}.
   *
   * @return A {@link Hash256} containing the ID.
   */
  Hash256 index();

}
