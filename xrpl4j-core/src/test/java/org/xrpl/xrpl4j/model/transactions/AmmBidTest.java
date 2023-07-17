package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.ledger.AuthAccount;
import org.xrpl.xrpl4j.model.ledger.AuthAccountWrapper;
import org.xrpl.xrpl4j.model.ledger.Issue;

class AmmBidTest extends AbstractJsonTest {

  @Test
  void testJsonWithoutMinAndMax() throws JSONException, JsonProcessingException {
    AmmBid bid = AmmBid.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .asset(Issue.XRP)
      .asset2(
        Issue.builder()
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
      "    \"Sequence\" : 9,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMBid\"\n" +
      "}";

    assertCanSerializeAndDeserialize(bid, json);
  }

  @Test
  void testJsonWithUnsetFlags() throws JSONException, JsonProcessingException {
    AmmBid bid = AmmBid.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .asset(Issue.XRP)
      .asset2(
        Issue.builder()
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
      .flags(TransactionFlags.UNSET)
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
      "    \"Flags\" : 0,\n" +
      "    \"Sequence\" : 9,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMBid\"\n" +
      "}";

    assertCanSerializeAndDeserialize(bid, json);
  }

  @Test
  void testJsonWithNonZeroFlags() throws JSONException, JsonProcessingException {
    AmmBid bid = AmmBid.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .asset(Issue.XRP)
      .asset2(
        Issue.builder()
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
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
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
      "    \"Flags\" : 0,\n" +
      "    \"Sequence\" : 9,\n" +
      "    \"Flags\" : 2147483648,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMBid\"\n" +
      "}";

    assertCanSerializeAndDeserialize(bid, json);
  }

  @Test
  void testJsonWithMinAndMax() throws JSONException, JsonProcessingException {
    AmmBid bid = AmmBid.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .asset(Issue.XRP)
      .asset2(
        Issue.builder()
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
      "    \"Sequence\" : 9,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TransactionType\" : \"AMMBid\"\n" +
      "}";

    assertCanSerializeAndDeserialize(bid, json);
  }
}