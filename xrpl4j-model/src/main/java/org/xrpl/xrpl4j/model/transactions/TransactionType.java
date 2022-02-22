package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of the types of Transactions on the XRP Ledger.
 */
public enum TransactionType {

  /**
   * The {@link TransactionType} for the {@link AccountSet} transaction.
   */
  ACCOUNT_SET("AccountSet"),

  /**
   * The {@link TransactionType} for the {@link AccountDelete} transaction.
   */
  ACCOUNT_DELETE("AccountDelete"),

  /**
   * The {@link TransactionType} for the {@link CheckCancel} transaction.
   */
  CHECK_CANCEL("CheckCancel"),

  /**
   * The {@link TransactionType} for the {@link CheckCash} transaction.
   */
  CHECK_CASH("CheckCash"),

  /**
   * The {@link TransactionType} for the {@link CheckCreate} transaction.
   */
  CHECK_CREATE("CheckCreate"),

  /**
   * The {@link TransactionType} for the {@link DepositPreAuth} transaction.
   */
  DEPOSIT_PRE_AUTH("DepositPreauth"),

  /**
   * The {@link TransactionType} for the {@link EscrowCancel} transaction.
   */
  ESCROW_CANCEL("EscrowCancel"),

  /**
   * The {@link TransactionType} for the {@link EscrowCreate} transaction.
   */
  ESCROW_CREATE("EscrowCreate"),

  /**
   * The {@link TransactionType} for the {@link EscrowFinish} transaction.
   */
  ESCROW_FINISH("EscrowFinish"),

  /**
   * The {@link TransactionType} for the {@link NfTokenMint} transaction.
   */
  NFTOKEN_MINT("NFTokenMint"),
  
  /**
   * The {@link TransactionType} for the {@link NfTokenBurn} transaction.
   */
  NFTOKEN_BURN("NFTokenBurn"),

  /**
   * The {@link TransactionType} for the {@link NfTokenAcceptOffer} transaction.
   */
  NFTOKEN_ACCEPT_OFFER("NFTokenAcceptOffer"),

  /**
   * The {@link TransactionType} for the {@link NfTokenCancelOffer} transaction.
   */
  NFTOKEN_CANCEL_OFFER("NFTokenCancelOffer"),

  /**
   * The {@link TransactionType} for the {@link NfTokenCreateOffer} transaction.
   */
  NFTOKEN_CREATE_OFFER("NFTokenCreateOffer"),

  /**
   * The {@link TransactionType} for the {@link OfferCancel} transaction.
   */
  OFFER_CANCEL("OfferCancel"),

  /**
   * The {@link TransactionType} for the {@link OfferCreate} transaction.
   */
  OFFER_CREATE("OfferCreate"),

  /**
   * The {@link TransactionType} for the {@link Payment} transaction.
   */
  PAYMENT("Payment"),

  /**
   * The {@link TransactionType} for the {@link PaymentChannelClaim} transaction.
   */
  PAYMENT_CHANNEL_CLAIM("PaymentChannelClaim"),

  /**
   * The {@link TransactionType} for the {@link PaymentChannelCreate} transaction.
   */
  PAYMENT_CHANNEL_CREATE("PaymentChannelCreate"),

  /**
   * The {@link TransactionType} for the {@link PaymentChannelFund} transaction.
   */
  PAYMENT_CHANNEL_FUND("PaymentChannelFund"),

  /**
   * The {@link TransactionType} for the {@link SetRegularKey} transaction.
   */
  SET_REGULAR_KEY("SetRegularKey"),

  /**
   * The {@link TransactionType} for the {@link SignerListSet} transaction.
   */
  SIGNER_LIST_SET("SignerListSet"),

  /**
   * The {@link TransactionType} for the {@link TrustSet} transaction.
   */
  TRUST_SET("TrustSet"),

  /**
   * The {@link TransactionType} for the {@link TicketCreate} transaction.
   */
  TICKET_CREATE("TicketCreate");

  private final String value;

  TransactionType(String value) {
    this.value = value;
  }

  /**
   * Gets an instance of {@link TransactionType} for the given string value.
   *
   * @param value The {@link String} value corresponding to a {@link TransactionType}.
   *
   * @return The {@link TransactionType} with the corresponding value.
   */
  public static TransactionType forValue(String value) {
    for (TransactionType transactionType : TransactionType.values()) {
      if (transactionType.value.equals(value)) {
        return transactionType;
      }
    }

    throw new IllegalArgumentException("No matching TransactionType enum value for String value " + value);
  }

  /**
   * Get the underlying value of this {@link TransactionType}.
   *
   * @return The {@link String} value.
   */
  @JsonValue
  public String value() {
    return value;
  }
}
