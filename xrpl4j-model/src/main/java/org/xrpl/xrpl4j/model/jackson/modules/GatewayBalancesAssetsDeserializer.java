package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesAssets;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesIssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.io.IOException;
import java.util.List;

public class GatewayBalancesAssetsDeserializer extends StdDeserializer<GatewayBalancesAssets> {

  public GatewayBalancesAssetsDeserializer() {
    super(GatewayBalancesAssets.class);
  }

  @Override
  public GatewayBalancesAssets deserialize(
    JsonParser jsonParser,
    DeserializationContext context
  ) throws IOException, JsonProcessingException {
    String address = jsonParser.currentName();
    List<GatewayBalancesIssuedCurrencyAmount> balances = jsonParser
      .readValueAs(new TypeReference<List<GatewayBalancesIssuedCurrencyAmount>>() {});

    GatewayBalancesAssets assets = GatewayBalancesAssets.builder()
      .issuer(Address.of(address))
      .balances(balances)
      .build();
    return assets;
  }
}
