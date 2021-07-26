package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

public class AccountLinesRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  public void testMinimalJson() throws JsonProcessingException, JSONException {

    AccountLinesRequestParams params = AccountLinesRequestParams.builder()
        .account(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
        .build();

    String json = "{\n" +
        "            \"account\": \"rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn\",\n" +
        "            \"ledger_index\": \"current\"\n" +
        "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testFullJson() throws JsonProcessingException, JSONException {

    AccountLinesRequestParams params = AccountLinesRequestParams.builder()
        .account(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
        .ledgerHash(Hash256.of("92FA6A9FC8EA6018D5D16532D7795C91BFB0831355BDFDA177E86C8BF997985F"))
        .peer(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .limit(UnsignedInteger.ONE)
        .marker(Marker.of("marker"))
        .build();

    String json = "{\n" +
        "            \"account\": \"rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn\",\n" +
        "            \"ledger_hash\": \"92FA6A9FC8EA6018D5D16532D7795C91BFB0831355BDFDA177E86C8BF997985F\",\n" +
        "            \"peer\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
        "            \"limit\": 1,\n" +
        "            \"marker\": \"marker\",\n" +
        "            \"ledger_index\": \"current\"\n" +
        "        }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
