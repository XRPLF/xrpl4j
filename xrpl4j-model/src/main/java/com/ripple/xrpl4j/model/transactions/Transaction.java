package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Transaction {

  Map<Class<? extends Transaction>, TransactionType> typeMap =
    new ImmutableMap.Builder<Class<? extends Transaction>, TransactionType>()
      .put(ImmutablePayment.class, TransactionType.PAYMENT)
      .put(ImmutableAccountSet.class, TransactionType.ACCOUNT_SET)
      .put(ImmutableAccountDelete.class, TransactionType.ACCOUNT_DELETE)
      .put(ImmutableCheckCancel.class, TransactionType.CHECK_CANCEL)
      .put(ImmutableCheckCash.class, TransactionType.CHECK_CASH)
      .put(ImmutableCheckCreate.class, TransactionType.CHECK_CREATE)
      .build();

  /**
   * The unique {@link Address} of the account that initiated the transaction.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * The type of transaction.
   */
  @JsonProperty("TransactionType")
  @Value.Derived
  default TransactionType transactionType() {
    return typeMap.get(this.getClass());
  }

  @JsonProperty("Flags")
  @Value.Default
  default Flags flags() {
    return Flags.Payment.builder().fullyCanonicalSig(true).build();
  }

  /**
   * The {@link String} representation of an integer amount of XRP, in drops, to be destroyed as a cost for
   * distributing this Payment transaction to the network.
   *
   * This field is auto-fillable
   * @see "https://xrpl.org/transaction-common-fields.html#auto-fillable-fields"
   */
  @JsonProperty("Fee")
  XrpCurrencyAmount fee();

  /**
   * The sequence number of the account submitting the {@link Transaction}. A {@link Transaction} is only valid if the Sequence
   * number is exactly 1 greater than the previous transaction from the same account.
   *
   * This field is auto-fillable
   * @see "https://xrpl.org/transaction-common-fields.html#auto-fillable-fields"
   */
  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  /**
   * Hash value identifying another transaction. If provided, this {@link Transaction} is only valid if the sending
   * account's previously-sent transaction matches the provided hash.
   */
  @JsonProperty("AccountTxnID")
  Optional<Hash256> accountTransactionId();

  /**
   * Highest ledger index this transaction can appear in. Specifying this field places a strict upper limit
   * on how long the transaction can wait to be validated or rejected.
   */
  @JsonProperty("LastLedgerSequence")
  Optional<UnsignedInteger> lastLedgerSequence();

  /**
   * Additional arbitrary information used to identify this {@link Transaction}.
   */
  @JsonProperty("Memos")
  List<MemoWrapper> memos();

  /**
   * Array of {@link SignerWrapper}s that represent a multi-signature which authorizes this {@link Transaction}.
   */
  @JsonProperty("Signers")
  List<SignerWrapper> signers();

  /**
   * Arbitrary {@link UnsignedInteger} used to identify the reason for this {@link Transaction}, or a sender on whose
   * behalf this {@link Transaction} is made.
   */
  @JsonProperty("SourceTag")
  Optional<UnsignedInteger> sourceTag();

  /**
   * Hex representation of the public key that corresponds to the private key used to sign this transaction.
   * If an empty string, indicates a multi-signature is present in the {@link Transaction#signers()} field instead.
   *
   * This field is automatically added when signing this {@link Transaction}.
   */
  @JsonProperty("SigningPubKey")
  Optional<String> signingPublicKey();

  /**
   * The signature that verifies this transaction as originating from the account it says it is from.
   *
   * This field is automatically added when signing this {@link Transaction}.
   */
  @JsonProperty("TxnSignature")
  Optional<String> transactionSignature();
}
