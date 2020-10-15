package com.ripple.xrplj4.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.transactions.Flags;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAccountInfoData.class)
@JsonDeserialize(as = ImmutableAccountInfoData.class)
public interface AccountInfoData {

  @JsonProperty("Account")
  String account();

  @JsonProperty("Balance")
  String balance();

  @JsonProperty("Flags")
  Flags flags();

  @JsonProperty("LedgerEntryType")
  String ledgerEntryType();

  @JsonProperty("OwnerCount")
  UnsignedInteger ownerCount();

  @JsonProperty("PreviousTxnID")
  String previousTransactionId();

  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousLedgerSequence();

  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  String index();

}
