package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.RippleStateObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;

public class AccountObjectsResultJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    AccountObjectsResult result = AccountObjectsResult.builder()
        .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
        .addAccountObjects(
            RippleStateObject.builder()
                .balance(IssuedCurrencyAmount.builder()
                    .currency("ASP")
                    .issuer(Address.of("rrrrrrrrrrrrrrrrrrrrBZbvji"))
                    .value("0")
                    .build())
                .flags(Flags.RippleStateFlags.of(65536))
                .highLimit(IssuedCurrencyAmount.builder()
                    .currency("ASP")
                    .issuer(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
                    .value("0")
                    .build())
                .highNode("0000000000000000")
                .lowLimit(IssuedCurrencyAmount.builder()
                    .currency("ASP")
                    .issuer(Address.of("r3vi7mWxru9rJCxETCyA1CHvzL96eZWx5z"))
                    .value("10")
                    .build())
                .lowNode("0000000000000000")
                .previousTransactionId(Hash256.of("BF7555B0F018E3C5E2A3FF9437A1A5092F32903BE246202F988181B9CED0D862"))
                .previousTransactionLedgerSequence(UnsignedInteger.valueOf(1438879))
                .index(Hash256.of("2243B0B630EA6F7330B654EFA53E27A7609D9484E535AB11B7F946DF3D247CE9"))
                .build(),
            RippleStateObject.builder()
                .balance(IssuedCurrencyAmount.builder()
                    .currency("XAU")
                    .issuer(Address.of("rrrrrrrrrrrrrrrrrrrrBZbvji"))
                    .value("0")
                    .build())
                .flags(Flags.RippleStateFlags.of(3342336))
                .highLimit(IssuedCurrencyAmount.builder()
                    .currency("XAU")
                    .issuer(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
                    .value("0")
                    .build())
                .highNode("0000000000000000")
                .lowLimit(IssuedCurrencyAmount.builder()
                    .currency("XAU")
                    .issuer(Address.of("r3vi7mWxru9rJCxETCyA1CHvzL96eZWx5z"))
                    .value("0")
                    .build())
                .lowNode("0000000000000000")
                .previousTransactionId(Hash256.of("79B26D7D34B950AC2C2F91A299A6888FABB376DD76CFF79D56E805BF439F6942"))
                .previousTransactionLedgerSequence(UnsignedInteger.valueOf(5982530))
                .index(Hash256.of("9ED4406351B7A511A012A9B5E7FE4059FA2F7650621379C0013492C315E25B97"))
                .build()
        )
        .ledgerHash(Hash256.of("4C99E5F63C0D0B1C2283B4F5DCE2239F80CE92E8B1A6AED1E110C198FC96E659"))
        .ledgerIndex(LedgerIndex.of(UnsignedLong.valueOf(14380380)))
        .limit(UnsignedInteger.valueOf(10))
        .marker("F60ADF645E78B69857D2E4AEC8B7742FEABC8431BD8611D099B428C3E816DF93," +
            "94A9F05FEF9A153229E2E997E64919FD75AAE2028C8153E8EBDB4440BD3ECBB5")
        .validated(true)
        .status("success")

        .build();

    String json = "{\n" +
        "        \"account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
        "        \"account_objects\": [\n" +
        "            {\n" +
        "                \"Balance\": {\n" +
        "                    \"currency\": \"ASP\",\n" +
        "                    \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
        "                    \"value\": \"0\"\n" +
        "                },\n" +
        "                \"Flags\": 65536,\n" +
        "                \"HighLimit\": {\n" +
        "                    \"currency\": \"ASP\",\n" +
        "                    \"issuer\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
        "                    \"value\": \"0\"\n" +
        "                },\n" +
        "                \"HighNode\": \"0000000000000000\",\n" +
        "                \"LedgerEntryType\": \"RippleState\",\n" +
        "                \"LowLimit\": {\n" +
        "                    \"currency\": \"ASP\",\n" +
        "                    \"issuer\": \"r3vi7mWxru9rJCxETCyA1CHvzL96eZWx5z\",\n" +
        "                    \"value\": \"10\"\n" +
        "                },\n" +
        "                \"LowNode\": \"0000000000000000\",\n" +
        "                \"PreviousTxnID\": \"BF7555B0F018E3C5E2A3FF9437A1A5092F32903BE246202F988181B9CED0D862\",\n" +
        "                \"PreviousTxnLgrSeq\": 1438879,\n" +
        "                \"index\": \"2243B0B630EA6F7330B654EFA53E27A7609D9484E535AB11B7F946DF3D247CE9\"\n" +
        "            },\n" +
        "            {\n" +
        "                \"Balance\": {\n" +
        "                    \"currency\": \"XAU\",\n" +
        "                    \"issuer\": \"rrrrrrrrrrrrrrrrrrrrBZbvji\",\n" +
        "                    \"value\": \"0\"\n" +
        "                },\n" +
        "                \"Flags\": 3342336,\n" +
        "                \"HighLimit\": {\n" +
        "                    \"currency\": \"XAU\",\n" +
        "                    \"issuer\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
        "                    \"value\": \"0\"\n" +
        "                },\n" +
        "                \"HighNode\": \"0000000000000000\",\n" +
        "                \"LedgerEntryType\": \"RippleState\",\n" +
        "                \"LowLimit\": {\n" +
        "                    \"currency\": \"XAU\",\n" +
        "                    \"issuer\": \"r3vi7mWxru9rJCxETCyA1CHvzL96eZWx5z\",\n" +
        "                    \"value\": \"0\"\n" +
        "                },\n" +
        "                \"LowNode\": \"0000000000000000\",\n" +
        "                \"PreviousTxnID\": \"79B26D7D34B950AC2C2F91A299A6888FABB376DD76CFF79D56E805BF439F6942\",\n" +
        "                \"PreviousTxnLgrSeq\": 5982530,\n" +
        "                \"index\": \"9ED4406351B7A511A012A9B5E7FE4059FA2F7650621379C0013492C315E25B97\"\n" +
        "            }\n" +
        "        ],\n" +
        "        \"ledger_hash\": \"4C99E5F63C0D0B1C2283B4F5DCE2239F80CE92E8B1A6AED1E110C198FC96E659\",\n" +
        "        \"ledger_index\": 14380380,\n" +
        "        \"limit\": 10,\n" +
        "        \"marker\": \"F60ADF645E78B69857D2E4AEC8B7742FEABC8431BD8611D099B428C3E816DF93," +
        "94A9F05FEF9A153229E2E997E64919FD75AAE2028C8153E8EBDB4440BD3ECBB5\",\n" +
        "        \"status\": \"success\",\n" +
        "        \"validated\": true\n" +
        "    }";

    assertCanSerializeAndDeserialize(result, json);
  }
}
