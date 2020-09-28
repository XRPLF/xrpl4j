package com.ripple.xrpl4j.jackson.modules;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.ripple.xrpl4j.transactions.Hash256;

public class Hash256Module extends SimpleModule {

  private static final String NAME = "Hash256Module";

  public Hash256Module() {
    super(
      NAME,
      new Version(
        1,
        0,
        0,
        null,
        "com.ripple.xrpl4j",
        "hash256"
      )
    );

    addSerializer(Hash256.class, new Hash256Serializer());
    addDeserializer(Hash256.class, new Hash256Deserializer());
  }

}
