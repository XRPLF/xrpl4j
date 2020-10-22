package com.ripple.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.ripple.xrpl4j.model.transactions.Flags;

import java.io.IOException;

public class FlagsDeserializer extends StdDeserializer<Flags> {

  public FlagsDeserializer() {
    super(Flags.class);
  }

  @Override
  public Flags deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return Flags.of(Long.parseLong(jsonParser.getText()));
  }
}
