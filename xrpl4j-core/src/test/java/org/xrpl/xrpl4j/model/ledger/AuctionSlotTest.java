package org.xrpl.xrpl4j.model.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.TradingFee;

class AuctionSlotTest extends AbstractJsonTest {

  @Test
  void authAccountsAddresses() {
    Address address1 = Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn");
    Address address2 = Address.of("rB1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn");
    AuctionSlot slot = AuctionSlot.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .discountedFee(TradingFee.of(UnsignedInteger.ONE))
      .price(mock(IssuedCurrencyAmount.class))
      .expiration(UnsignedInteger.ONE)
      .addAuthAccounts(
        AuthAccountWrapper.of(AuthAccount.of(address1)),
        AuthAccountWrapper.of(AuthAccount.of(address2))
      )
      .build();
    assertThat(slot.authAccountsAddresses()).asList().containsExactlyInAnyOrder(address1, address2);
  }

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    AuctionSlot slot = AuctionSlot.builder()
      .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
      .discountedFee(TradingFee.of(UnsignedInteger.ZERO))
      .price(
        IssuedCurrencyAmount.builder()
          .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
          .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
          .value("0.8696263565463045")
          .build()
      )
      .expiration(UnsignedInteger.valueOf(721870180))
      .addAuthAccounts(
        AuthAccountWrapper.of(AuthAccount.of(Address.of("rMKXGCbJ5d8LbrqthdG46q3f969MVK2Qeg"))),
        AuthAccountWrapper.of(AuthAccount.of(Address.of("rBepJuTLFJt3WmtLXYAxSjtBWAeQxVbncv")))
      )
      .build();

    String json = "{\n" +
      "      \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "      \"AuthAccounts\" : [\n" +
      "          {\n" +
      "            \"AuthAccount\" : {\n" +
      "                \"Account\" : \"rMKXGCbJ5d8LbrqthdG46q3f969MVK2Qeg\"\n" +
      "            }\n" +
      "          },\n" +
      "          {\n" +
      "            \"AuthAccount\" : {\n" +
      "                \"Account\" : \"rBepJuTLFJt3WmtLXYAxSjtBWAeQxVbncv\"\n" +
      "            }\n" +
      "          }\n" +
      "      ],\n" +
      "      \"DiscountedFee\" : 0,\n" +
      "      \"Expiration\" : 721870180,\n" +
      "      \"Price\" : {\n" +
      "          \"currency\" : \"039C99CD9AB0B70B32ECDA51EAAE471625608EA2\",\n" +
      "          \"issuer\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "          \"value\" : \"0.8696263565463045\"\n" +
      "      }\n" +
      "    }";

    assertCanSerializeAndDeserialize(slot, json, AuctionSlot.class);
  }
}