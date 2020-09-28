package com.ripple.xrpl4j.jackson.modules;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.ripple.xrpl4j.transactions.Flags;
import com.ripple.xrpl4j.transactions.Hash256;

public class FlagsModule extends SimpleModule {

  private static final String NAME = "FlagsModule";

  public FlagsModule() {
    super(
      NAME,
      new Version(
        1,
        0,
        0,
        null,
        "com.ripple.xrpl4j",
        "flags"
      )
    );

    addSerializer(Flags.class, new FlagsSerializer());
    addDeserializer(Flags.class, new FlagsDeserializer());
  }

}
