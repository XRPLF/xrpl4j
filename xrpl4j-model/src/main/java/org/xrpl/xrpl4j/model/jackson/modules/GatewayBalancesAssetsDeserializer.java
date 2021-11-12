package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesAssets;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesIssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.client.accounts.ImmutableGatewayBalancesAssets;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GatewayBalancesAssetsDeserializer extends StdDeserializer<ImmutableGatewayBalancesAssets> {

  public GatewayBalancesAssetsDeserializer() {
    super(ImmutableGatewayBalancesAssets.class);
  }

  @Override
  public ImmutableGatewayBalancesAssets deserialize(
    JsonParser jsonParser,
    DeserializationContext context
  ) throws IOException, JsonProcessingException {
    Map<Address, List<GatewayBalancesIssuedCurrencyAmount>> balances = jsonParser
      .readValueAs(new TypeReference<Map<Address, List<GatewayBalancesIssuedCurrencyAmount>>>() {});

    ImmutableGatewayBalancesAssets assets = GatewayBalancesAssets.builder()
      .balancesByIssuer(balances)
      .build();
    return assets;
  }

}
