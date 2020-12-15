package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class PayChannelObjectJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    PayChannelObject object = PayChannelObject.builder()
        .account(Address.of("rBqb89MRQJnMPq8wTwEbtz4kvxrEDfcYvt"))
        .destination(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .amount(XrpCurrencyAmount.ofDrops(4325800))
        .balance(XrpCurrencyAmount.ofDrops(2323423))
        .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
        .settleDelay(UnsignedLong.valueOf(3600))
        .expiration(UnsignedLong.valueOf(536027313))
        .cancelAfter(UnsignedLong.valueOf(536891313))
        .sourceTag(UnsignedInteger.ZERO)
        .destinationTag(UnsignedInteger.valueOf(1002341))
        .ownerNode("0000000000000000")
        .previousTransactionId(Hash256.of("F0AB71E777B2DA54B86231E19B82554EF1F8211F92ECA473121C655BFC5329BF"))
        .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14524914))
        .index(Hash256.of("96F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1797"))
        .build();

    String json = "{\n" +
        "    \"Account\": \"rBqb89MRQJnMPq8wTwEbtz4kvxrEDfcYvt\",\n" +
        "    \"Destination\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
        "    \"Amount\": \"4325800\",\n" +
        "    \"Balance\": \"2323423\",\n" +
        "    \"PublicKey\": \"32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A\",\n" +
        "    \"SettleDelay\": 3600,\n" +
        "    \"Expiration\": 536027313,\n" +
        "    \"CancelAfter\": 536891313,\n" +
        "    \"SourceTag\": 0,\n" +
        "    \"DestinationTag\": 1002341,\n" +
        "    \"Flags\": 0,\n" +
        "    \"LedgerEntryType\": \"PayChannel\",\n" +
        "    \"OwnerNode\": \"0000000000000000\",\n" +
        "    \"PreviousTxnID\": \"F0AB71E777B2DA54B86231E19B82554EF1F8211F92ECA473121C655BFC5329BF\",\n" +
        "    \"PreviousTxnLgrSeq\": 14524914,\n" +
        "    \"index\": \"96F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1797\"\n" +
        "}";

    assertCanSerializeAndDeserialize(object, json);
  }
}
