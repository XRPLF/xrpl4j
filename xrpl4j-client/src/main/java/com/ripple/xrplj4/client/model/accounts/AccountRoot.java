package com.ripple.xrplj4.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.Hash256;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableAccountRoot.class)
@JsonDeserialize(as = ImmutableAccountRoot.class)
public interface AccountRoot {

  static ImmutableAccountRoot.Builder builder() {
    return ImmutableAccountRoot.builder();
  }

  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default String ledgerEntryType() {
    return "AccountRoot";
  }

  @JsonProperty("Account")
  Address account();

  @JsonProperty("Balance")
  String balance();

  @JsonProperty("Flags")
  Flags.AccountRootFlags flags();

  @JsonProperty("OwnerCount")
  UnsignedInteger ownerCount();

  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  @JsonProperty("AccountTxnID")
  Optional<Hash256> accountTransactionId();

  @JsonProperty("Domain")
  Optional<String> domain();

  @JsonProperty("EmailHash")
  Optional<String> emailHash();

  @JsonProperty("MessageKey")
  Optional<String> messageKey();

  @JsonProperty("RegularKey")
  Optional<Address> regularKey();

  @JsonProperty("TickSize")
  Optional<UnsignedInteger> tickSize();

  @JsonProperty("TransferRate")
  Optional<UnsignedInteger> transferRate();

  Hash256 index();

}
