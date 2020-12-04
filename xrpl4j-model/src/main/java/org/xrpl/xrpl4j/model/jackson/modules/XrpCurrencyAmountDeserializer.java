package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.io.IOException;

public class XrpCurrencyAmountDeserializer extends StdDeserializer<XrpCurrencyAmount> {

  public XrpCurrencyAmountDeserializer() {
    super(XrpCurrencyAmount.class);
  }

  @Override
  public XrpCurrencyAmount deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return XrpCurrencyAmount.ofDrops(jsonParser.getValueAsLong());
  }
}
