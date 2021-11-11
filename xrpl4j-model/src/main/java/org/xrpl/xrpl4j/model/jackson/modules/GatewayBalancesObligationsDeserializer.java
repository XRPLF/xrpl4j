package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesIssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesObligations;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GatewayBalancesObligationsDeserializer extends StdDeserializer<GatewayBalancesObligations> {

  public GatewayBalancesObligationsDeserializer() {
    super(GatewayBalancesObligations.class);
  }

  @Override
  public GatewayBalancesObligations deserialize(
    JsonParser jsonParser,
    DeserializationContext deserializationContext
  ) throws IOException, JsonProcessingException {
    Map<String, String> rawBalances = jsonParser.readValueAs(new TypeReference<Map<String, String>>() {});

    List<GatewayBalancesIssuedCurrencyAmount> balances = rawBalances
      .entrySet()
      .stream()
      .map(e -> GatewayBalancesIssuedCurrencyAmount
        .builder()
        .currency(e.getKey())
        .value(e.getValue())
        .build()
      )
      .collect(Collectors.toList());

    return GatewayBalancesObligations
      .builder()
      .balances(balances)
      .build();
  }
}
