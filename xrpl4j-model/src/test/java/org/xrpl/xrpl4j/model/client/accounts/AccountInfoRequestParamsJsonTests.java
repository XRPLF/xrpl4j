package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class AccountInfoRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  public void testMinimalJson() throws JsonProcessingException, JSONException {

    AccountInfoRequestParams params = AccountInfoRequestParams.builder()
        .account(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
        .strict(true)
        .ledgerIndex(LedgerIndex.CURRENT)
        .queue(true)
        .build();

    String json = "{\n" +
        "            \"account\": \"rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn\",\n" +
        "            \"strict\": true,\n" +
        "            \"ledger_index\": \"current\",\n" +
        "            \"signer_lists\": false,\n" +
        "            \"queue\": true\n" +
        "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testFullJson() throws JsonProcessingException, JSONException {

    AccountInfoRequestParams params = AccountInfoRequestParams.builder()
        .account(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
        .strict(true)
        .ledgerHash(Hash256.of("5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3"))
        .ledgerIndex(LedgerIndex.CURRENT)
        .queue(true)
        .build();

    String json = "{\n" +
        "            \"account\": \"rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn\",\n" +
        "            \"strict\": true,\n" +
        "            \"ledger_index\": \"current\",\n" +
        "            \"ledger_hash\": \"5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3\",\n" +
        "            \"signer_lists\": false,\n" +
        "            \"queue\": true\n" +
        "        }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
