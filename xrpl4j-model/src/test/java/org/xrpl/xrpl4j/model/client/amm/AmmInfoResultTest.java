package org.xrpl.xrpl4j.model.client.amm;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.ledger.AmmObject;
import org.xrpl.xrpl4j.model.ledger.Asset;
import org.xrpl.xrpl4j.model.ledger.AuctionSlot;
import org.xrpl.xrpl4j.model.ledger.AuthAccount;
import org.xrpl.xrpl4j.model.ledger.AuthAccountWrapper;
import org.xrpl.xrpl4j.model.ledger.VoteEntry;
import org.xrpl.xrpl4j.model.ledger.VoteEntryWrapper;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.TradingFee;
import org.xrpl.xrpl4j.model.transactions.VoteWeight;

class AmmInfoResultTest extends AbstractJsonTest {

  /*@Test
  void testJsonForCurrentLedger() throws JSONException, JsonProcessingException {
    AmmInfoResult result = AmmInfoResult.builder()
      .amm(
        AmmObject.builder()
          .ammAccount(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
          .asset(Asset.XRP)
          .asset2(
            Asset.builder()
              .currency("TST")
              .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
              .build()
          )
          .auctionSlot(
            AuctionSlot.builder()
              .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
              .addAuthAccounts(
                AuthAccountWrapper.of(AuthAccount.of(Address.of("rMKXGCbJ5d8LbrqthdG46q3f969MVK2Qeg"))),
                AuthAccountWrapper.of(AuthAccount.of(Address.of("rBepJuTLFJt3WmtLXYAxSjtBWAeQxVbncv")))
              )
              .discountedFee(TradingFee.of(UnsignedInteger.ZERO))
              .expiration(UnsignedInteger.valueOf(721870180))
              .price(
                IssuedCurrencyAmount.builder()
                  .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
                  .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
                  .value("0.8696263565463045")
                  .build()
              )
              .build()
          )
          .lpTokenBalance(
            IssuedCurrencyAmount.builder()
              .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
              .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
              .value("71150.53584131501")
              .build()
          )
          .tradingFee(TradingFee.of(UnsignedInteger.valueOf(600)))
          .addVoteSlots(
            VoteEntryWrapper.of(
              VoteEntry.builder()
                .voteWeight(VoteWeight.of(UnsignedInteger.valueOf(100000)))
                .tradingFee(TradingFee.of(UnsignedInteger.valueOf(600)))
                .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
                .build()
            )
          )
          .build()
      )
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(226645)))
      .validated(false)
      .build();
    String json = "{\n" +
      "    \"amm\": {\n" +
      "        \"AMMAccount\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "        \"LedgerEntryType\" : \"AMM\",\n" +
      "        \"Asset\" : {\n" +
      "          \"currency\" : \"XRP\"\n" +
      "        },\n" +
      "        \"Asset2\" : {\n" +
      "          \"currency\" : \"TST\",\n" +
      "          \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
      "        },\n" +
      "        \"AuctionSlot\" : {\n" +
      "          \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "          \"AuthAccounts\" : [\n" +
      "              {\n" +
      "                \"AuthAccount\" : {\n" +
      "                    \"Account\" : \"rMKXGCbJ5d8LbrqthdG46q3f969MVK2Qeg\"\n" +
      "                }\n" +
      "              },\n" +
      "              {\n" +
      "                \"AuthAccount\" : {\n" +
      "                    \"Account\" : \"rBepJuTLFJt3WmtLXYAxSjtBWAeQxVbncv\"\n" +
      "                }\n" +
      "              }\n" +
      "          ],\n" +
      "          \"DiscountedFee\" : 0,\n" +
      "          \"Expiration\" : 721870180,\n" +
      "          \"Price\" : {\n" +
      "              \"currency\" : \"039C99CD9AB0B70B32ECDA51EAAE471625608EA2\",\n" +
      "              \"issuer\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "              \"value\" : \"0.8696263565463045\"\n" +
      "          }\n" +
      "        },\n" +
      "        \"Flags\" : 0,\n" +
      "        \"LPTokenBalance\" : {\n" +
      "          \"currency\" : \"039C99CD9AB0B70B32ECDA51EAAE471625608EA2\",\n" +
      "          \"issuer\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "          \"value\" : \"71150.53584131501\"\n" +
      "        },\n" +
      "        \"TradingFee\" : 600,\n" +
      "        \"VoteSlots\" : [\n" +
      "          {\n" +
      "              \"VoteEntry\" : {\n" +
      "                \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "                \"TradingFee\" : 600,\n" +
      "                \"VoteWeight\" : 100000\n" +
      "              }\n" +
      "          }\n" +
      "        ]\n" +
      "    },\n" +
      "    \"ledger_current_index\": 226645,\n" +
      "    \"validated\": false\n" +
      "  }";

    assertCanSerializeAndDeserialize(result, json);

    assertThat(result.ledgerCurrentIndexSafe()).isEqualTo(result.ledgerCurrentIndex().get());
    assertThatThrownBy(result::ledgerIndexSafe).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void testJsonForValidatedLedger() throws JSONException, JsonProcessingException {
    AmmInfoResult result = AmmInfoResult.builder()
      .amm(
        AmmObject.builder()
          .ammAccount(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
          .asset(Asset.XRP)
          .asset2(
            Asset.builder()
              .currency("TST")
              .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
              .build()
          )
          .auctionSlot(
            AuctionSlot.builder()
              .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
              .addAuthAccounts(
                AuthAccountWrapper.of(AuthAccount.of(Address.of("rMKXGCbJ5d8LbrqthdG46q3f969MVK2Qeg"))),
                AuthAccountWrapper.of(AuthAccount.of(Address.of("rBepJuTLFJt3WmtLXYAxSjtBWAeQxVbncv")))
              )
              .discountedFee(TradingFee.of(UnsignedInteger.ZERO))
              .expiration(UnsignedInteger.valueOf(721870180))
              .price(
                IssuedCurrencyAmount.builder()
                  .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
                  .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
                  .value("0.8696263565463045")
                  .build()
              )
              .build()
          )
          .lpTokenBalance(
            IssuedCurrencyAmount.builder()
              .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
              .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
              .value("71150.53584131501")
              .build()
          )
          .tradingFee(TradingFee.of(UnsignedInteger.valueOf(600)))
          .addVoteSlots(
            VoteEntryWrapper.of(
              VoteEntry.builder()
                .voteWeight(VoteWeight.of(UnsignedInteger.valueOf(100000)))
                .tradingFee(TradingFee.of(UnsignedInteger.valueOf(600)))
                .account(Address.of("rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm"))
                .build()
            )
          )
          .build()
      )
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(226645)))
      .validated(true)
      .build();
    String json = "{\n" +
      "    \"amm\": {\n" +
      "        \"AMMAccount\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "        \"LedgerEntryType\" : \"AMM\",\n" +
      "        \"Asset\" : {\n" +
      "          \"currency\" : \"XRP\"\n" +
      "        },\n" +
      "        \"Asset2\" : {\n" +
      "          \"currency\" : \"TST\",\n" +
      "          \"issuer\" : \"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"\n" +
      "        },\n" +
      "        \"AuctionSlot\" : {\n" +
      "          \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "          \"AuthAccounts\" : [\n" +
      "              {\n" +
      "                \"AuthAccount\" : {\n" +
      "                    \"Account\" : \"rMKXGCbJ5d8LbrqthdG46q3f969MVK2Qeg\"\n" +
      "                }\n" +
      "              },\n" +
      "              {\n" +
      "                \"AuthAccount\" : {\n" +
      "                    \"Account\" : \"rBepJuTLFJt3WmtLXYAxSjtBWAeQxVbncv\"\n" +
      "                }\n" +
      "              }\n" +
      "          ],\n" +
      "          \"DiscountedFee\" : 0,\n" +
      "          \"Expiration\" : 721870180,\n" +
      "          \"Price\" : {\n" +
      "              \"currency\" : \"039C99CD9AB0B70B32ECDA51EAAE471625608EA2\",\n" +
      "              \"issuer\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "              \"value\" : \"0.8696263565463045\"\n" +
      "          }\n" +
      "        },\n" +
      "        \"Flags\" : 0,\n" +
      "        \"LPTokenBalance\" : {\n" +
      "          \"currency\" : \"039C99CD9AB0B70B32ECDA51EAAE471625608EA2\",\n" +
      "          \"issuer\" : \"rE54zDvgnghAoPopCgvtiqWNq3dU5y836S\",\n" +
      "          \"value\" : \"71150.53584131501\"\n" +
      "        },\n" +
      "        \"TradingFee\" : 600,\n" +
      "        \"VoteSlots\" : [\n" +
      "          {\n" +
      "              \"VoteEntry\" : {\n" +
      "                \"Account\" : \"rJVUeRqDFNs2xqA7ncVE6ZoAhPUoaJJSQm\",\n" +
      "                \"TradingFee\" : 600,\n" +
      "                \"VoteWeight\" : 100000\n" +
      "              }\n" +
      "          }\n" +
      "        ]\n" +
      "    },\n" +
      "    \"ledger_index\": 226645,\n" +
      "    \"validated\": true\n" +
      "  }";

    assertCanSerializeAndDeserialize(result, json);

    assertThat(result.ledgerIndexSafe()).isEqualTo(result.ledgerIndex().get());
    assertThatThrownBy(result::ledgerCurrentIndexSafe).isInstanceOf(IllegalStateException.class);
  }*/
}