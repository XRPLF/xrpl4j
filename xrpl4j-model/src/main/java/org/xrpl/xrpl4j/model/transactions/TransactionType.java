package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of the types of Transactions on the XRP Ledger.
 */
public enum TransactionType {

  ACCOUNT_SET("AccountSet"),
  ACCOUNT_DELETE("AccountDelete"),
  CHECK_CANCEL("CheckCancel"),
  CHECK_CASH("CheckCash"),
  CHECK_CREATE("CheckCreate"),
  DEPOSIT_PRE_AUTH("DepositPreauth"),
  ESCROW_CANCEL("EscrowCancel"),
  ESCROW_CREATE("EscrowCreate"),
  ESCROW_FINISH("EscrowFinish"),
  OFFER_CANCEL("OfferCancel"),
  OFFER_CREATE("OfferCreate"),
  PAYMENT("Payment"),
  PAYMENT_CHANNEL_CLAIM("PaymentChannelClaim"),
  PAYMENT_CHANNEL_CREATE("PaymentChannelCreate"),
  PAYMENT_CHANNEL_FUND("PaymentChannelFund"),
  SET_REGULAR_KEY("SetRegularKey"),
  SIGNER_LIST_SET("SignerListSet"),
  TRUST_SET("TrustSet");

  private final String value;

  TransactionType(String value) {
    this.value = value;
  }

  public static TransactionType forValue(String value) {
    for (TransactionType transactionType : TransactionType.values()) {
      if (transactionType.value.equals(value)) {
        return transactionType;
      }
    }

    throw new IllegalArgumentException("No matching AccountSetFlag enum value for int value " + value);
  }

  @JsonValue
  public String value() {
    return value;
  }
}
