package com.ripple.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.CurrencyAmount;
import com.ripple.xrpl4j.model.transactions.Hash256;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;

public class Xrpl4jModule extends SimpleModule {

  private static final String NAME = "Xrpl4jModule";

  public Xrpl4jModule() {
    super(
        NAME,
        new Version(
            1,
            0,
            0,
            null,
            "com.ripple.xrpl4j",
            "xrpl4j"
        )
    );

    addSerializer(Address.class, new AddressSerializer());
    addDeserializer(Address.class, new AddressDeserializer());

    addSerializer(Hash256.class, new Hash256Serializer());
    addDeserializer(Hash256.class, new Hash256Deserializer());

    addSerializer(XrpCurrencyAmount.class, new XrpCurrencyAmountSerializer());
    addDeserializer(XrpCurrencyAmount.class, new XrpCurrencyAmountDeserializer());

    addDeserializer(CurrencyAmount.class, new CurrencyAmountDeserializer());

  }
}
