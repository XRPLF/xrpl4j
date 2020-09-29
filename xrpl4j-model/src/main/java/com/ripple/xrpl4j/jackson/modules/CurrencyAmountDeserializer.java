package com.ripple.xrpl4j.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.ripple.xrpl4j.transactions.Address;
import com.ripple.xrpl4j.transactions.CurrencyAmount;
import com.ripple.xrpl4j.transactions.IssuedCurrencyAmount;
import com.ripple.xrpl4j.transactions.XrpCurrencyAmount;

import java.io.IOException;

public class CurrencyAmountDeserializer extends StdDeserializer<CurrencyAmount> {

  protected CurrencyAmountDeserializer() {
    super(CurrencyAmount.class);
  }

  @Override
  public CurrencyAmount deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);

    if (node.isContainerNode()) {
      String currency = node.get("currency").asText();
      String value = node.get("value").asText();
      String issuer = node.get("issuer").asText();

      return IssuedCurrencyAmount.builder()
        .value(value)
        .issuer(Address.of(issuer))
        .currency(currency)
        .build();
    } else {
      return XrpCurrencyAmount.of(node.asText());
    }
  }
}
