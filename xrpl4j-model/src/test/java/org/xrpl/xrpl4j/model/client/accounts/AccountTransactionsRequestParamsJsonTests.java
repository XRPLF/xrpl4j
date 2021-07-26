package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndex;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndexBound;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

public class AccountTransactionsRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  public void testWithStringMarker() throws JsonProcessingException, JSONException {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w"))
      .marker(Marker.of("marker1"))
      .limit(UnsignedInteger.valueOf(2))
      .build();

    String json = "{\n" +
      "            \"account\": \"rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w\",\n" +
      "            \"binary\": false,\n" +
      "            \"forward\": false,\n" +
      "            \"ledger_index_max\": -1,\n" +
      "            \"ledger_index_min\": -1,\n" +
      "            \"marker\": \"marker1\",\n" +
      "            \"limit\": 2\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithJsonMarker() throws JsonProcessingException, JSONException {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w"))
      .marker(Marker.of("{\"marker\":\"1\"}"))
      .limit(UnsignedInteger.valueOf(2))
      .build();

    String json = "{\n" +
      "            \"account\": \"rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w\",\n" +
      "            \"binary\": false,\n" +
      "            \"forward\": false,\n" +
      "            \"ledger_index_max\": -1,\n" +
      "            \"ledger_index_min\": -1,\n" +
      "            \"marker\": {\"marker\":\"1\"},\n" +
      "            \"limit\": 2\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithJsonWithLedgerIndexBounds() throws JsonProcessingException, JSONException {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w"))
      .ledgerIndexMin(LedgerIndexBound.of(-1))
      .ledgerIndexMax(LedgerIndexBound.of(-1))
      .limit(UnsignedInteger.valueOf(2))
      .build();

    String json = "{\n" +
      "            \"account\": \"rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w\",\n" +
      "            \"binary\": false,\n" +
      "            \"forward\": false,\n" +
      "            \"ledger_index_max\": -1,\n" +
      "            \"ledger_index_min\": -1,\n" +
      "            \"limit\": 2\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testJsonWithLedgerIndex() throws JsonProcessingException, JSONException {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w"))
      .ledgerSpecifier(LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedLong.ONE)))
      .limit(UnsignedInteger.valueOf(2))
      .build();

    String json = "{\n" +
      "            \"account\": \"rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w\",\n" +
      "            \"binary\": false,\n" +
      "            \"forward\": false,\n" +
      "            \"ledger_index\": 1,\n" +
      "            \"limit\": 2\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testJsonWithLedgerHash() throws JsonProcessingException, JSONException {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w"))
      .ledgerSpecifier(
        LedgerSpecifier.ledgerHash(Hash256.of("5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3"))
      )
      .limit(UnsignedInteger.valueOf(2))
      .build();

    String json = "{\n" +
      "            \"account\": \"rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w\",\n" +
      "            \"binary\": false,\n" +
      "            \"forward\": false,\n" +
      "            \"ledger_hash\": \"5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3\",\n" +
      "            \"limit\": 2\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testJsonWithLedgerIndexShortcut() throws JsonProcessingException, JSONException {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w"))
      .ledgerSpecifier(LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED))
      .limit(UnsignedInteger.valueOf(2))
      .build();

    String json = "{\n" +
      "            \"account\": \"rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w\",\n" +
      "            \"binary\": false,\n" +
      "            \"forward\": false,\n" +
      "            \"ledger_index\": \"validated\",\n" +
      "            \"limit\": 2\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
