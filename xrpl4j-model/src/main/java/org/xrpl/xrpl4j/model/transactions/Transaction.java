package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value.Auxiliary;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.ledger.SignerListObject;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides an abstract interface for all concrete XRPL transactions.
 */
public interface Transaction {

  /**
   * XRP Ledger represents dates using a custom epoch called Ripple Epoch. This is a constant for
   * the start of that epoch.
   */
  long RIPPLE_EPOCH = 946684800;

  /**
   * A bi-directional map of immutable transaction types to their corresponding {@link TransactionType}.
   *
   * <p>This is useful for polymorphic Jackson deserialization.
   */
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

  /**
   * Computes the fee necessary for a multisigned transaction.
   *
   * <p>The transaction cost of a multisigned transaction must be at least {@code (N + 1) * (the normal
   * transaction cost)}, where {@code N} is the number of signatures provided.
   *
   * @param currentLedgerFeeDrops The current ledger fee, represented as an {@link XrpCurrencyAmount}.
   * @param signerList            The {@link SignerListObject} containing the signers of the transaction.
   *
   * @return An {@link XrpCurrencyAmount} representing the multisig fee.
   */
  static XrpCurrencyAmount computeMultiSigFee(
      final XrpCurrencyAmount currentLedgerFeeDrops,
      final SignerListObject signerList
  ) {
    Objects.requireNonNull(currentLedgerFeeDrops);
    Objects.requireNonNull(signerList);

    return currentLedgerFeeDrops
        .times(XrpCurrencyAmount.of(UnsignedLong.valueOf(signerList.signerEntries().size() + 1)));
  }

  /**
   * The unique {@link Address} of the account that initiated this transaction.
   *
   * @return The {@link Address} of the account submitting this transaction.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * The type of transaction.
   *
   * @return A {@link TransactionType}.
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
   * @return An {@link XrpCurrencyAmount} representing the transaction cost.
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
   * @return An {@link UnsignedInteger} representing the sequence of the transaction.
   * @see "https://xrpl.org/transaction-common-fields.html#auto-fillable-fields"
   */
  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  /**
   * Hash value identifying another transaction. If provided, this {@link Transaction} is only valid if the sending
   * account's previously-sent transaction matches the provided hash.
   *
   * @return An {@link Optional} of type {@link Hash256} containing the account transaction ID.
   */
  @JsonProperty("AccountTxnID")
  Optional<Hash256> accountTransactionId();

  /**
   * Highest ledger index this transaction can appear in. Specifying this field places a strict upper limit on how long
   * the transaction can wait to be validated or rejected.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the last ledger sequence.
   */
  @JsonProperty("LastLedgerSequence")
  Optional<UnsignedInteger> lastLedgerSequence();

  /**
   * Additional arbitrary information used to identify this {@link Transaction}.
   *
   * @return A {@link List} of {@link MemoWrapper}s.
   */
  @JsonProperty("Memos")
  List<MemoWrapper> memos();

  /**
   * Array of {@link SignerWrapper}s that represent a multi-signature which authorizes this {@link Transaction}.
   *
   * @return A {@link List} of {@link SignerWrapper}s.
   */
  @JsonProperty("Signers")
  List<SignerWrapper> signers();

  /**
   * Arbitrary {@link UnsignedInteger} used to identify the reason for this {@link Transaction}, or a sender on whose
   * behalf this {@link Transaction} is made.
   *
   * @return An {@link Optional} {@link UnsignedInteger} representing the source account's tag.
   */
  @JsonProperty("SourceTag")
  Optional<UnsignedInteger> sourceTag();

  /**
   * Hex representation of the public key that corresponds to the private key used to sign this transaction. If an empty
   * string, indicates a multi-signature is present in the {@link Transaction#signers()} field instead.
   *
   * <p>This field is automatically added when signing this {@link Transaction}.
   *
   * @return An {@link Optional} {@link String} containing the public key of the account submitting the transaction.
   */
  @JsonInclude(JsonInclude.Include.NON_ABSENT)
  @JsonProperty("SigningPubKey")
  Optional<String> signingPublicKey();

  /**
   * The signature that verifies this transaction as originating from the account it says it is from.
   *
   * <p>This field is automatically added when signing this {@link Transaction}.
   *
   * @return An {@link Optional} {@link String} containing the transaction signature.
   */
  @JsonProperty("TxnSignature")
  Optional<String> transactionSignature();

  /**
   * The approximate close time (using Ripple Epoch) of the ledger containing this transaction.
   * This is an undocumented field.
   *
   * @return An optionally-present {@link UnsignedLong}.
   */
  @JsonProperty("date")
  Optional<UnsignedLong> closeDate();

  /**
   * The approximate close time in UTC offset.
   * This is derived from undocumented field.
   *
   * @return An optionally-present {@link ZonedDateTime}.
   */
  @JsonIgnore
  @Auxiliary
  default Optional<ZonedDateTime> closeDateHuman() {
    return closeDate().map(secondsSinceRippleEpoch ->
      Instant.ofEpochSecond(RIPPLE_EPOCH + secondsSinceRippleEpoch.longValue()).atZone(ZoneId.of("UTC"))
    );
  }

  /**
   * The transaction hash of this transaction.  Only present in responses to {@code account_tx} rippled calls.
   *
   * @return An optionally present {@link Hash256} containing the transaction hash.
   */
  Optional<Hash256> hash();

  /**
   * The index of the ledger that this transaction was included in. Only present in responses to {@code account_tx}
   * rippled calls.
   *
   * @return An optionally-present {@link LedgerIndex}.
   */
  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

}
