package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.model.ledger.SignerList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface Transaction {

  BiMap<Class<? extends Transaction>, TransactionType> typeMap =
      new ImmutableBiMap.Builder<Class<? extends Transaction>, TransactionType>()
          .put(ImmutableAccountSet.class, TransactionType.ACCOUNT_SET)
          .put(ImmutableAccountDelete.class, TransactionType.ACCOUNT_DELETE)
          .put(ImmutableCheckCancel.class, TransactionType.CHECK_CANCEL)
          .put(ImmutableCheckCash.class, TransactionType.CHECK_CASH)
          .put(ImmutableCheckCreate.class, TransactionType.CHECK_CREATE)
          .put(ImmutableDepositPreAuth.class, TransactionType.DEPOSIT_PRE_AUTH)
          .put(ImmutableEscrowCancel.class, TransactionType.ESCROW_CANCEL)
          .put(ImmutableEscrowCreate.class, TransactionType.ESCROW_CREATE)
          .put(ImmutableEscrowFinish.class, TransactionType.ESCROW_FINISH)
          .put(ImmutableOfferCancel.class, TransactionType.OFFER_CANCEL)
          .put(ImmutableOfferCreate.class, TransactionType.OFFER_CREATE)
          .put(ImmutablePayment.class, TransactionType.PAYMENT)
          .put(ImmutablePaymentChannelClaim.class, TransactionType.PAYMENT_CHANNEL_CLAIM)
          .put(ImmutablePaymentChannelCreate.class, TransactionType.PAYMENT_CHANNEL_CREATE)
          .put(ImmutablePaymentChannelFund.class, TransactionType.PAYMENT_CHANNEL_FUND)
          .put(ImmutableSetRegularKey.class, TransactionType.SET_REGULAR_KEY)
          .put(ImmutableSignerListSet.class, TransactionType.SIGNER_LIST_SET)
          .put(ImmutableTrustSet.class, TransactionType.TRUST_SET)
          .build();

  static XrpCurrencyAmount computeMultiSigFee(
      final XrpCurrencyAmount currentLedgerFeeDrops,
      final SignerList signerList
  ) {
    Objects.requireNonNull(currentLedgerFeeDrops);
    Objects.requireNonNull(signerList);

    return currentLedgerFeeDrops
        .times(XrpCurrencyAmount.of(UnsignedLong.valueOf(signerList.signerEntries().size() + 1)));
  }

  /**
   * The unique {@link Address} of the account that initiated the transaction.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * The type of transaction.
   */
  @JsonProperty("TransactionType")
  default TransactionType transactionType() {
    return typeMap.get(this.getClass());
  }

  /**
   * The {@link String} representation of an integer amount of XRP, in drops, to be destroyed as a cost for distributing
   * this Payment transaction to the network.
   *
   * <p>This field is auto-fillable
   *
   * @see "https://xrpl.org/transaction-common-fields.html#auto-fillable-fields"
   */
  @JsonProperty("Fee")
  XrpCurrencyAmount fee();

  /**
   * The sequence number of the account submitting the {@link Transaction}. A {@link Transaction} is only valid if the
   * Sequence number is exactly 1 greater than the previous transaction from the same account.
   *
   * <p>This field is auto-fillable
   *
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
   * Highest ledger index this transaction can appear in. Specifying this field places a strict upper limit on how long
   * the transaction can wait to be validated or rejected.
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
   * Hex representation of the public key that corresponds to the private key used to sign this transaction. If an empty
   * string, indicates a multi-signature is present in the {@link Transaction#signers()} field instead.
   *
   * <p>This field is automatically added when signing this {@link Transaction}.
   */
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  @JsonProperty("SigningPubKey")
  Optional<String> signingPublicKey();

  /**
   * The signature that verifies this transaction as originating from the account it says it is from.
   *
   * <p>This field is automatically added when signing this {@link Transaction}.
   */
  @JsonProperty("TxnSignature")
  Optional<String> transactionSignature();

}
