package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
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
      "            \"marker\": {\"marker\":\"1\"},\n" +
      "            \"limit\": 2\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithJsonWithLedgerIndex() throws JsonProcessingException, JSONException {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w"))
      .ledgerIndexMin(LedgerIndex.of("0"))
      .ledgerIndexMax(LedgerIndex.of("0"))
      .ledgerIndex(LedgerIndex.CURRENT)
      .limit(UnsignedInteger.valueOf(2))
      .build();

    String json = "{\n" +
      "            \"account\": \"rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w\",\n" +
      "            \"binary\": false,\n" +
      "            \"forward\": false,\n" +
      "            \"ledger_index_max\": 0,\n" +
      "            \"ledger_index_min\": 0,\n" +
      "            \"ledger_index\": \"current\",\n" +
      "            \"limit\": 2\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
