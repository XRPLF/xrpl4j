package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.TradingFee;
import org.xrpl.xrpl4j.model.transactions.VoteWeight;

class VoteEntryWrapperTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    VoteEntryWrapper voteWrapper = VoteEntryWrapper.of(
      VoteEntry.builder()
        .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
        .tradingFee(TradingFee.of(UnsignedInteger.valueOf(600)))
        .voteWeight(VoteWeight.of(UnsignedInteger.valueOf(100000)))
        .build()
    );
    String json = "{\n" +
      "          \"VoteEntry\" : {\n" +
      "            \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "            \"TradingFee\" : 600,\n" +
      "            \"VoteWeight\" : 100000\n" +
      "          }\n" +
      "      }";

    assertCanSerializeAndDeserialize(voteWrapper, json, VoteEntryWrapper.class);
  }
}