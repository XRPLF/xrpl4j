package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesHotWallets;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesIssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.client.accounts.ImmutableGatewayBalancesHotWallets;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GatewayBalancesHotWalletsDeserializer extends StdDeserializer<ImmutableGatewayBalancesHotWallets> {

  public GatewayBalancesHotWalletsDeserializer() {
    super(ImmutableGatewayBalancesHotWallets.class);
  }

  @Override
  public ImmutableGatewayBalancesHotWallets deserialize(
    JsonParser jsonParser,
    DeserializationContext deserializationContext
  ) throws IOException, JsonProcessingException {
    Map<Address, List<GatewayBalancesIssuedCurrencyAmount>> balances = jsonParser
      .readValueAs(new TypeReference<Map<Address, List<GatewayBalancesIssuedCurrencyAmount>>>() {});

    ImmutableGatewayBalancesHotWallets hotWallets = GatewayBalancesHotWallets.builder()
      .balancesByHolder(balances)
      .build();
    return hotWallets;
  }

}
