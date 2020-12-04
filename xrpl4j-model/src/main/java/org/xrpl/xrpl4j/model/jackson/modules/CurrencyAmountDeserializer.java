package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.io.IOException;

public class CurrencyAmountDeserializer extends StdDeserializer<CurrencyAmount> {

  protected CurrencyAmountDeserializer() {
    super(CurrencyAmount.class);
  }

  @Override
  public CurrencyAmount deserialize(
      JsonParser jsonParser,
      DeserializationContext deserializationContext
  ) throws IOException {
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
      return XrpCurrencyAmount.ofDrops(node.asLong());
    }
  }
}
