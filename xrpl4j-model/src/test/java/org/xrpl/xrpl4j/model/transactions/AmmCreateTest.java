package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;

class AmmCreateTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    AmmCreate ammCreate = AmmCreate.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("TST")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("25")
          .build()
      )
      .amount2(XrpCurrencyAmount.ofDrops(250000000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .tradingFee(TradingFee.of(UnsignedInteger.valueOf(500)))
      .build();

    String json = "{\n" +
      "    \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "    \"Amount\" : {\n" +
      "        \"currency\" : \"TST\",\n" +
      "        \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\",\n" +
      "        \"value\" : \"25\"\n" +
      "    },\n" +
      "    \"Amount2\" : \"250000000\",\n" +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : 2147483648,\n" +
      "    \"Sequence\" : 6,\n" +
      "    \"TradingFee\" : 500,\n" +
      "    \"TransactionType\" : \"AMMCreate\"\n" +
      "}";

    assertCanSerializeAndDeserialize(ammCreate, json);
  }
}