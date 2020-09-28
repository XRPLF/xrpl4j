package com.ripple.xrpl4j.jackson.modules;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.ripple.xrpl4j.transactions.Hash256;
import com.ripple.xrpl4j.transactions.TransactionType;

public class TransactionTypeModule extends SimpleModule {

  private static final String NAME = "TransactionTypeModule";

  public TransactionTypeModule() {
    super(
      NAME,
      new Version(
        1,
        0,
        0,
        null,
        "com.ripple.xrpl4j",
        "transaction-type"
      )
    );

    addSerializer(TransactionType.class, new TransactionTypeSerializer());
    addDeserializer(TransactionType.class, new TransactionTypeDeserializer());
  }

}
