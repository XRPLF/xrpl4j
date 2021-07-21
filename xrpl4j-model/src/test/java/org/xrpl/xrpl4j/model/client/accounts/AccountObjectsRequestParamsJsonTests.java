package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndex;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public class AccountObjectsRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  public void testWithLedgerHash() throws JsonProcessingException, JSONException {
    AccountObjectsRequestParams params = AccountObjectsRequestParams.builder()
      .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .ledgerSpecifier(
        LedgerSpecifier.ledgerHash(Hash256.of("5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3"))
      )
      .type(AccountObjectsRequestParams.AccountObjectType.STATE)
      .deletionBlockersOnly(false)
      .limit(UnsignedInteger.valueOf(10))
      .build();

    String json = "{\n" +
      "            \"account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"ledger_hash\": \"5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3\",\n" +
      "            \"type\": \"state\",\n" +
      "            \"deletion_blockers_only\": false,\n" +
      "            \"limit\": 10\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithLedgerIndex() throws JsonProcessingException, JSONException {
    AccountObjectsRequestParams params = AccountObjectsRequestParams.builder()
      .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .ledgerSpecifier(
        LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedLong.ONE))
      )
      .type(AccountObjectsRequestParams.AccountObjectType.STATE)
      .deletionBlockersOnly(false)
      .limit(UnsignedInteger.valueOf(10))
      .build();

    String json = "{\n" +
      "            \"account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"ledger_index\": 1,\n" +
      "            \"type\": \"state\",\n" +
      "            \"deletion_blockers_only\": false,\n" +
      "            \"limit\": 10\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithLedgerIndexShortcut() throws JsonProcessingException, JSONException {
    AccountObjectsRequestParams params = AccountObjectsRequestParams.builder()
        .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
        .ledgerSpecifier(LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED))
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
