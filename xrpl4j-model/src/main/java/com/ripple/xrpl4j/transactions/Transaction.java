package com.ripple.xrpl4j.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface Transaction {

  @JsonProperty("Account")
  Address account();

  @JsonProperty("TransactionType")
  TransactionType type();

  @JsonProperty("Fee")
  String fee();

  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  @JsonProperty("AccountTxnID")
  Optional<Hash256> accountTransactionId();

  @JsonProperty("Flags")
  Optional<UnsignedInteger> flags();

  @JsonProperty("LastLedgerSequence")
  Optional<UnsignedInteger> lastLedgerSequence();

  @JsonProperty("Memos")
  List<MemoWrapper> memos();

  @JsonProperty("Signers")
  List<SignerWrapper> signers();

  @JsonProperty("SourceTag")
  Optional<UnsignedInteger> sourceTag();

  @JsonProperty("SigningPubKey")
  Optional<String> signingPublicKey();

  @JsonProperty("TxnSignature")
  Optional<String> transactionSignature();

}
