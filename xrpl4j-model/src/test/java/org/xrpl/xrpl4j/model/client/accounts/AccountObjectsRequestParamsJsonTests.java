package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public class AccountObjectsRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {

    AccountObjectsRequestParams params = AccountObjectsRequestParams.builder()
        .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
        .ledgerIndex(LedgerIndex.VALIDATED)
        .type(AccountObjectsRequestParams.AccountObjectType.STATE)
        .deletionBlockersOnly(false)
        .limit(UnsignedInteger.valueOf(10))
        .build();

    String json = "{\n" +
        "            \"account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
        "            \"ledger_index\": \"validated\",\n" +
        "            \"type\": \"state\",\n" +
        "            \"deletion_blockers_only\": false,\n" +
        "            \"limit\": 10\n" +
        "        }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
