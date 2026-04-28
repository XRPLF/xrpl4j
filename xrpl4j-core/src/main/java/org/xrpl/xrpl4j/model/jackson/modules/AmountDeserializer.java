package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.transactions.Amount;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link Amount}s. Deserializes from a JSON string.
 */
public class AmountDeserializer extends StdDeserializer<Amount> {

  /**
   * No-args constructor.
   */
  public AmountDeserializer() {
    super(Amount.class);
  }

  @Override
  public Amount deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return Amount.builder().value(jsonParser.getText()).build();
  }
}
