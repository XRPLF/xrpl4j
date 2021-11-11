package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesHotWallets;
import org.xrpl.xrpl4j.model.client.accounts.GatewayBalancesIssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.io.IOException;
import java.util.List;

public class GatewayBalancesHotWalletsDeserializer extends StdDeserializer<GatewayBalancesHotWallets> {

  public GatewayBalancesHotWalletsDeserializer() {
    super(GatewayBalancesHotWallets.class);
  }

  @Override
  public GatewayBalancesHotWallets deserialize(
    JsonParser jsonParser,
    DeserializationContext deserializationContext
  ) throws IOException, JsonProcessingException {
    String address = jsonParser.currentName();
    List<GatewayBalancesIssuedCurrencyAmount> balances = jsonParser
      .readValueAs(new TypeReference<List<GatewayBalancesIssuedCurrencyAmount>>() {});

    GatewayBalancesHotWallets hotWallets = GatewayBalancesHotWallets.builder()
      .holder(Address.of(address))
      .balances(balances)
      .build();
    return hotWallets;
  }
}
