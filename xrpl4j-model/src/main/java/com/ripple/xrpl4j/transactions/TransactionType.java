package com.ripple.xrpl4j.transactions;

public class TransactionType {

  public static final String PAYMENT_VALUE = "Payment";
  public static final TransactionType PAYMENT = new TransactionType(PAYMENT_VALUE);

  private String type;

  private TransactionType(String type) {
    this.type = type;
  }
}
