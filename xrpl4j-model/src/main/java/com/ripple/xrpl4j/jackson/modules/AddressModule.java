package com.ripple.xrpl4j.jackson.modules;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.ripple.xrpl4j.transactions.Address;

public class AddressModule extends SimpleModule {

  private static final String NAME = "AddressModule";

  public AddressModule() {
    super(
      NAME,
      new Version(
        1,
        0,
        0,
        null,
        "com.ripple.xrpl4j",
        "address"
      )
    );

    addSerializer(Address.class, new AddressSerializer());
    addDeserializer(Address.class, new AddressDeserializer());
  }

}
