package com.ripple.xrpl4j.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
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
  Integer sequence();

  @JsonProperty("AccountTxnID")
  Optional<Hash256> accountTransactionId();

  @JsonProperty("Flags")
  Optional<Integer> flags();

  @JsonProperty("LastLedgerSequence")
  Optional<Integer> lastLedgerSequence();

  @JsonProperty("Memos")
  List<MemoWrapper> memos();

  @JsonProperty("Signers")
  List<SignerWrapper> signers();

  @JsonProperty("SourceTag")
  Optional<Integer> sourceTag();

  @JsonProperty("SigningPubKey")
  Optional<String> signingPublicKey();

  @JsonProperty("TxnSignature")
  Optional<String> transactionSignature();

}
