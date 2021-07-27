package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;

public class AccountLinesResultJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    AccountLinesResult result = AccountLinesResult.builder()
        .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
        .status("success")
        .addLines(
            TrustLine.builder()
                .account(Address.of("r3vi7mWxru9rJCxETCyA1CHvzL96eZWx5z"))
                .balance("0")
                .currency("ASP")
                .limit("0")
                .limitPeer("10")
                .qualityIn(UnsignedInteger.ZERO)
                .qualityOut(UnsignedInteger.ZERO)
                .build(),
            TrustLine.builder()
                .account(Address.of("rs9M85karFkCRjvc6KMWn8Coigm9cbcgcx"))
                .balance("0")
                .currency("015841551A748AD2C1F76FF6ECB0CCCD00000000")
                .limit("10.01037626125837")
                .limitPeer("0")
                .noRipple(true)
                .qualityIn(UnsignedInteger.ZERO)
                .qualityOut(UnsignedInteger.ZERO)
                .build()
        )
        .build();

    String json = "{\n" +
        "        \"account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
        "        \"lines\": [\n" +
        "            {\n" +
        "                \"account\": \"r3vi7mWxru9rJCxETCyA1CHvzL96eZWx5z\",\n" +
        "                \"balance\": \"0\",\n" +
        "                \"currency\": \"ASP\",\n" +
        "                \"limit\": \"0\",\n" +
        "                \"limit_peer\": \"10\",\n" +
        "                \"no_ripple\": false,\n" +
        "                \"no_ripple_peer\": false,\n" +
        "                \"authorized\": false,\n" +
        "                \"peer_authorized\": false,\n" +
        "                \"freeze\": false,\n" +
        "                \"freeze_peer\": false,\n" +
        "                \"quality_in\": 0,\n" +
        "                \"quality_out\": 0\n" +
        "            },\n" +
        "            {\n" +
        "                \"account\": \"rs9M85karFkCRjvc6KMWn8Coigm9cbcgcx\",\n" +
        "                \"balance\": \"0\",\n" +
        "                \"currency\": \"015841551A748AD2C1F76FF6ECB0CCCD00000000\",\n" +
        "                \"limit\": \"10.01037626125837\",\n" +
        "                \"limit_peer\": \"0\",\n" +
        "                \"no_ripple\": true,\n" +
        "                \"no_ripple_peer\": false,\n" +
        "                \"authorized\": false,\n" +
        "                \"peer_authorized\": false,\n" +
        "                \"freeze\": false,\n" +
        "                \"freeze_peer\": false,\n" +
        "                \"quality_in\": 0,\n" +
        "                \"quality_out\": 0\n" +
        "            }\n" +
        "        ],\n" +
        "        \"status\": \"success\"\n" +
        "    }";

    assertCanSerializeAndDeserialize(result, json);
  }
}
