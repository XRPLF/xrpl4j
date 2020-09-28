package com.ripple.xrpl4j.jackson.modules;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.ripple.xrpl4j.transactions.XrpCurrencyAmount;

public class XrpCurrencyAmountModule extends SimpleModule {

  private static final String NAME = "XrpCurrencyAmountModule";

  public XrpCurrencyAmountModule() {
    super(
      NAME,
      new Version(
        1,
        0,
        0,
        null,
        "com.ripple.xrpl4j",
        "xrp-currency-amount"
      )
    );

    addSerializer(XrpCurrencyAmount.class, new XrpCurrencyAmountSerializer());
    addDeserializer(XrpCurrencyAmount.class, new XrpCurrencyAmountDeserializer());
  }

}
