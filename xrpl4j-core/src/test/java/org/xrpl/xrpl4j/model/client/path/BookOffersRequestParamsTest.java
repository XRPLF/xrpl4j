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
    String json = "{" +
      "  \"taker_gets\": {" +
      "    \"currency\": \"XRP\"" +
      "  }," +
      "  \"ledger_index\": \"current\"," +
      "  \"taker_pays\": {" +
      "    \"currency\": \"USD\"," +
      "    \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\"" +
      "  }" +
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
    String json = "{" +
      "  \"taker\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "  \"ledger_index\": \"current\"," +
      "  \"taker_gets\": {" +
      "    \"currency\": \"XRP\"" +
      "  }," +
      "  \"taker_pays\": {" +
      "    \"currency\": \"USD\"," +
      "    \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\"" +
      "  }," +
      "  \"domain\": \"96F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1797\"," +
      "  \"limit\": 1" +
      "}";

    assertCanSerializeAndDeserialize(expected, json);
  }
}