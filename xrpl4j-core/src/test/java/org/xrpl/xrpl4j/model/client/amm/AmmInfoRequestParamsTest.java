package org.xrpl.xrpl4j.model.client.amm;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.transactions.Address;

class AmmInfoRequestParamsTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    AmmInfoRequestParams params = AmmInfoRequestParams.builder()
      .asset(Issue.XRP)
      .asset2(
        Issue.builder()
          .currency("TST")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .build()
      )
      .build();
    String json = "{\n" +
      "      \"asset\": {\n" +
      "        \"currency\": \"XRP\"\n" +
      "      },\n" +
      "      \"asset2\": {\n" +
      "        \"currency\": \"TST\",\n" +
      "        \"issuer\": \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
      "      }\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }
}