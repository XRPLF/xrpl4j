package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;

public class BookOffersRequestJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    BookOffersRequestParams params = BookOffersRequestParams.builder()
      .taker(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .takerGets(PathCurrency.builder()
        .currency("XRP")
        .build())
      .takerPays(PathCurrency.builder()
        .currency("USD")
        .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
        .build())
      .build();

    String json = "{\n" +
      "            \"taker\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"taker_gets\": {\n" +
      "                \"currency\": \"XRP\"\n" +
      "            },\n" +
      "            \"taker_pays\": {\n" +
      "                \"currency\": \"USD\",\n" +
      "                \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\"\n" +
      "            }\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
