package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

class BookOffersRequestParamsTest extends AbstractJsonTest {

  @Test
  void testMinimalJson() throws JSONException, JsonProcessingException {
    BookOffersRequestParams expected = BookOffersRequestParams.builder()
      .takerGets(Issue.XRP)
      .takerPays(
        Issue.builder()
          .currency("USD")
          .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
          .build()
      )
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .build();
    String json =
      "{\n" +
      "  \"taker_gets\": {\n" +
      "    \"currency\": \"XRP\"\n" +
      "  },\n" +
      "  \"ledger_index\": \"current\",\n" +
      "  \"taker_pays\": {\n" +
      "    \"currency\": \"USD\",\n" +
      "    \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\"\n" +
      "  }\n" +
      "}";

    assertCanSerializeAndDeserialize(expected, json);
  }

  @Test
  void testFullJson() throws JSONException, JsonProcessingException {
    BookOffersRequestParams expected = BookOffersRequestParams.builder()
      .takerGets(Issue.XRP)
      .takerPays(
        Issue.builder()
          .currency("USD")
          .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
          .build()
      )
      .domain(Hash256.of("96F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1797"))
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .taker(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .limit(UnsignedInteger.ONE)
      .build();
    String json =
      "{\n" +
      "  \"taker\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "  \"ledger_index\": \"current\",\n" +
      "  \"taker_gets\": {\n" +
      "    \"currency\": \"XRP\"\n" +
      "  },\n" +
      "  \"taker_pays\": {\n" +
      "    \"currency\": \"USD\",\n" +
      "    \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\"\n" +
      "  },\n" +
      "  \"domain\": \"96F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1797\",\n" +
      "  \"limit\": 1\n" +
      "}";

    assertCanSerializeAndDeserialize(expected, json);
  }
}