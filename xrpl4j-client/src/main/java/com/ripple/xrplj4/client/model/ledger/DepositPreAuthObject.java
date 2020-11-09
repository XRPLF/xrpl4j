package com.ripple.xrplj4.client.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.Hash256;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDepositPreAuthObject.class)
@JsonDeserialize(as = ImmutableDepositPreAuthObject.class)
public interface DepositPreAuthObject extends LedgerObject {

  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.DEPOSIT_PRE_AUTH;
  }

  @JsonProperty("Account")
  Address account();

  @JsonProperty("Authorize")
  Address authorize();

  @JsonProperty("Flags")
  @Value.Derived
  default Flags flags() {
    return Flags.UNSET;
  }

  @JsonProperty("OwnerNode")
  String ownerNode();

  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

}
