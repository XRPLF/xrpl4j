package com.ripple.xrplj4.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.Hash256;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableSignerList.class)
@JsonDeserialize(as = ImmutableSignerList.class)
public interface SignerList {

  static ImmutableSignerList.Builder builder() {
    return ImmutableSignerList.builder();
  }

  @JsonProperty("LedgerEntryType")
  String ledgerEntryType();

  @JsonProperty("Flags")
  Flags flags();

  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  @JsonProperty("OwnerNode")
  String ownerNode();

  @JsonProperty("SignerListID")
  UnsignedInteger signerListId();

  @JsonProperty("signerQuorum")
  UnsignedInteger signerQuorum();

  @JsonProperty("SignerEntries")
  List<SignerEntry> signerEntries();

}
