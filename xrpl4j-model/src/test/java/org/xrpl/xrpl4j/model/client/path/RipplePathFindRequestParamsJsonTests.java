package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;

public class RipplePathFindRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {

    RipplePathFindRequestParams params = RipplePathFindRequestParams.builder()
      .destinationAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .destinationAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
        .value("0.001")
        .build())
      .sourceAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .addSourceCurrencies(
        PathCurrency.of("XRP"),
        PathCurrency.of("USD")
      )
      .build();

    String json = "{\n" +
      "            \"ledger_index\": \"current\",\n" +
      "            \"destination_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"destination_amount\": {\n" +
      "                \"currency\": \"USD\",\n" +
      "                \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "                \"value\": \"0.001\"\n" +
      "            },\n" +
      "            \"source_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"source_currencies\": [\n" +
      "                {\n" +
      "                    \"currency\": \"XRP\"\n" +
      "                },\n" +
      "                {\n" +
      "                    \"currency\": \"USD\"\n" +
      "                }\n" +
      "            ]\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
