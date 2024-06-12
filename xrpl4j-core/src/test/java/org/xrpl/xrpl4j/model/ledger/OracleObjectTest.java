package org.xrpl.xrpl4j.model.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetPrice;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.OracleProvider;
import org.xrpl.xrpl4j.model.transactions.PriceData;
import org.xrpl.xrpl4j.model.transactions.PriceDataWrapper;

class OracleObjectTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    OracleObject oracleObject = OracleObject.builder()
      .assetClass("63757272656E6379")
      .lastUpdateTime(UnsignedInteger.valueOf(1715797016))
      .owner(Address.of("rMS69A6J39RmBg5yWDft5XAM8zTGbtMMZy"))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("A5183686EF85C7D563B400C127DBEA71D1E404E419424BABB2891F4CC772E157"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(722809))
      .provider(OracleProvider.of("68747470733A2F2F74687265657872702E646576"))
      .addPriceDataSeries(
        PriceDataWrapper.of(
          PriceData.builder()
            .assetPrice(AssetPrice.of(UnsignedLong.valueOf("2030", 16)))
            .baseAsset("XRP")
            .quoteAsset("IDR")
            .build()
        )
      )
      .index(Hash256.of("72B5901470838D8C71E392C43999DC357BF49C1DA75583872C170AD0C28A12E8"))
      .build();

    String json = "{\n" +
      "            \"AssetClass\": \"63757272656E6379\",\n" +
      "            \"Flags\": 0,\n" +
      "            \"LastUpdateTime\": 1715797016,\n" +
      "            \"LedgerEntryType\": \"Oracle\",\n" +
      "            \"Owner\": \"rMS69A6J39RmBg5yWDft5XAM8zTGbtMMZy\",\n" +
      "            \"OwnerNode\": \"0\",\n" +
      "            \"PreviousTxnID\": \"A5183686EF85C7D563B400C127DBEA71D1E404E419424BABB2891F4CC772E157\",\n" +
      "            \"PreviousTxnLgrSeq\": 722809,\n" +
      "            \"PriceDataSeries\": [\n" +
      "                {\n" +
      "                    \"PriceData\": {\n" +
      "                        \"AssetPrice\": \"2030\",\n" +
      "                        \"BaseAsset\": \"XRP\",\n" +
      "                        \"QuoteAsset\": \"IDR\"\n" +
      "                    }\n" +
      "                }\n" +
      "            ],\n" +
      "            \"Provider\": \"68747470733A2F2F74687265657872702E646576\",\n" +
      "            \"index\": \"72B5901470838D8C71E392C43999DC357BF49C1DA75583872C170AD0C28A12E8\"\n" +
      "        }";

    assertCanSerializeAndDeserialize(oracleObject, json);
    assertThat(oracleObject.flags()).isEqualTo(Flags.UNSET);
  }
}