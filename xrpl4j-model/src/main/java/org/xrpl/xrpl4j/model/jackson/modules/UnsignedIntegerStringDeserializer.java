package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedInteger;

import java.io.IOException;

public class UnsignedIntegerStringDeserializer extends StdDeserializer<UnsignedInteger> {

  protected UnsignedIntegerStringDeserializer() {
    super(UnsignedInteger.class);
  }

  @Override
  public UnsignedInteger deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    return UnsignedInteger.valueOf(p.getValueAsString());
  }
}
