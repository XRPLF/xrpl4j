package com.ripple.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.Flags.SignerListFlags;
import com.ripple.xrpl4j.model.transactions.Hash256;
import com.ripple.xrpl4j.model.transactions.SignerListSet;
import com.ripple.xrpl4j.model.transactions.Transaction;
import org.immutables.value.Value;

import java.util.List;

/**
 * Represents a list of parties that, as a group, are authorized to sign a {@link Transaction} in place of an
 * individual account. You can create, replace, or remove a signer list using a {@link SignerListSet} transaction.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSignerList.class)
@JsonDeserialize(as = ImmutableSignerList.class)
public interface SignerList {

  static ImmutableSignerList.Builder builder() {
    return ImmutableSignerList.builder();
  }

  /**
   * The type of ledger object. In this case, always "SignerList".
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default String ledgerEntryType() {
    return "SignerList";
  }

  /**
   * A bit-map of Boolean {@link SignerListFlags} enabled for this signer list.
   */
  @JsonProperty("Flags")
  Flags.SignerListFlags flags();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * A hint indicating which page of the owner directory links to this object, in case the directory
   * consists of multiple pages.
   */
  @JsonProperty("OwnerNode")
  String ownerNode();

  /**
   * An ID for this signer list. Currently always set to 0. If a future amendment allows multiple
   * signer lists for an account, this may change.
   */
  @JsonProperty("SignerListID")
  UnsignedInteger signerListId();

  /**
   * A target number for signer weights. To produce a valid signature for the owner of this {@link SignerList},
   * the signers must provide valid signatures whose weights sum to this value or more.
   */
  @JsonProperty("SignerQuorum")
  UnsignedInteger signerQuorum();

  /**
   * A {@link List} of {@link SignerEntry} objects representing the parties who are part of this signer list.
   */
  @JsonProperty("SignerEntries")
  List<SignerEntryWrapper> signerEntries();

}
