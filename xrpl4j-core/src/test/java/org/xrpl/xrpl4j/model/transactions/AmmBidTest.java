package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.ledger.Asset;
import org.xrpl.xrpl4j.model.ledger.AuthAccount;
import org.xrpl.xrpl4j.model.ledger.AuthAccountWrapper;

class AmmBidTest extends AbstractJsonTest {

  @Test
  void testJsonWithoutMinAndMax() throws JSONException, JsonProcessingException {
    AmmBid bid = AmmBid.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .asset(Asset.XRP)
      .asset2(
        Asset.builder()
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .currency("TST")
          .build()
      )
      .addAuthAccounts(
        AuthAccountWrapper.of(AuthAccount.of(Address.of("rMKXGCbJ5d8LbrqthdG46q3f969MVK2Qeg"))),
        AuthAccountWrapper.of(AuthAccount.of(Address.of("rBepJuTLFJt3WmtLXYAxSjtBWAeQxVbncv")))
      )
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(9))
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
      "    \"AuthAccounts\" : [\n" +
      "        {\n" +
      "          \"AuthAccount\" : {\n" +
      "              \"Account\" : \"rMKXGCbJ5d8LbrqthdG46q3f969MVK2Qeg\"\n" +
      "          }\n" +
      "        },\n" +
      "        {\n" +
      "          \"AuthAccount\" : {\n" +
      "              \"Account\" : \"rBepJuTLFJt3WmtLXYAxSjtBWAeQxVbncv\"\n" +
      "          }\n" +
      "        }\n" +
      "    ],\n" +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : 2147483648,\n" +
      "    \"Sequence\" : 9,\n" +
      "    \"TransactionType\" : \"AMMBid\"\n" +
      "}";

    assertCanSerializeAndDeserialize(bid, json);
  }

  @Test
  void testJsonWithMinAndMax() throws JSONException, JsonProcessingException {
    AmmBid bid = AmmBid.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .asset(Asset.XRP)
      .asset2(
        Asset.builder()
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .currency("TST")
          .build()
      )
      .addAuthAccounts(
        AuthAccountWrapper.of(AuthAccount.of(Address.of("rMKXGCbJ5d8LbrqthdG46q3f969MVK2Qeg"))),
        AuthAccountWrapper.of(AuthAccount.of(Address.of("rBepJuTLFJt3WmtLXYAxSjtBWAeQxVbncv")))
      )
      .bidMax(
        IssuedCurrencyAmount.builder()
          .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
          .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
          .value("100")
          .build()
      )
      .bidMin(
        IssuedCurrencyAmount.builder()
          .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
          .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
          .value("100")
          .build()
      )
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(9))
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
      "    \"AuthAccounts\" : [\n" +
      "        {\n" +
      "          \"AuthAccount\" : {\n" +
      "              \"Account\" : \"rMKXGCbJ5d8LbrqthdG46q3f969MVK2Qeg\"\n" +
      "          }\n" +
      "        },\n" +
      "        {\n" +
      "          \"AuthAccount\" : {\n" +
      "              \"Account\" : \"rBepJuTLFJt3WmtLXYAxSjtBWAeQxVbncv\"\n" +
      "          }\n" +
      "        }\n" +
      "    ],\n" +
      "    \"BidMax\" : {\n" +
      "        \"currency\" : \"039C99CD9AB0B70B32ECDA51EAAE471625608EA2\",\n" +
      "        \"issuer\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "        \"value\" : \"100\"\n" +
      "    },\n" +
      "    \"BidMin\" : {\n" +
      "        \"currency\" : \"039C99CD9AB0B70B32ECDA51EAAE471625608EA2\",\n" +
      "        \"issuer\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "        \"value\" : \"100\"\n" +
      "    },\n" +
      "    \"Fee\" : \"10\",\n" +
      "    \"Flags\" : 2147483648,\n" +
      "    \"Sequence\" : 9,\n" +
      "    \"TransactionType\" : \"AMMBid\"\n" +
      "}";

    assertCanSerializeAndDeserialize(bid, json);
  }
}