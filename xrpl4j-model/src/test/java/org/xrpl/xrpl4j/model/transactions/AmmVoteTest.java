package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.ledger.Asset;

class AmmVoteTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    AmmVote vote = AmmVote.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .asset(Asset.XRP)
      .asset2(
        Asset.builder()
          .currency("TST")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .build()
      )
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(8))
      .tradingFee(TradingFee.of(UnsignedInteger.valueOf(600)))
      .build();

    String json = "{\n" +
      "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Asset\" : {\n" +
      "        \"currency\" : \"XRP\"\n" +
      "    },\n" +
      "    \"Asset2\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
      "    },\n" +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : 2147483648,\n" +
      "    \"Sequence\" : 8,\n" +
      "    \"TradingFee\" : 600,\n" +
      "    \"TransactionType\" : \"AMMVote\"\n" +
      "}";

    assertCanSerializeAndDeserialize(vote, json);
  }
}