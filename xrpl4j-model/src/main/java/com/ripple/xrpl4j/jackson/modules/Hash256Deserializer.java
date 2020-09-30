package com.ripple.xrpl4j.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.ripple.xrpl4j.transactions.Hash256;

import java.io.IOException;

public class Hash256Deserializer extends StdDeserializer<Hash256> {

  public Hash256Deserializer() {
    super(Hash256.class);
  }

  @Override
  public Hash256 deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return Hash256.of(jsonParser.getText());
  }
}
