package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public class AccountCurrenciesResultJsonTests extends AbstractJsonTest {

  @Test
  public void testFullJson() throws JsonProcessingException, JSONException {
    AccountCurrenciesResult result = AccountCurrenciesResult.builder()
      .ledgerHash(Hash256.of("B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D"))
      .ledgerIndex(LedgerIndex.of(UnsignedLong.valueOf(37230600)))
      .status("success")
      .validated(true)
      .addSendCurrencies("USD")
      .addReceiveCurrencies("EUR")
      .addReceiveCurrencies("USD")
      .build();

    String json = "{\n" +
      "        \"send_currencies\": [\n" +
      "            \"USD\"\n" +
      "        ],\n" +
      "        \"receive_currencies\": [\n" +
      "            \"EUR\",\n" +
      "            \"USD\"\n" +
      "        ],\n" +
      "        \"ledger_hash\": \"B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D\",\n" +
      "        \"ledger_index\": 37230600,\n" +
      "        \"status\": \"success\",\n" +
      "        \"validated\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(result, json);
  }
}
