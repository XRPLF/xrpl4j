package org.xrpl.xrpl4j.model.client.transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public class TransactionRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  public void testMinimalJson() throws JsonProcessingException, JSONException {

    TransactionRequestParams params = TransactionRequestParams.builder()
        .transaction(Hash256.of("C53ECF838647FA5A4C780377025FEC7999AB4182590510CA461444B207AB74A9"))
        .build();

    String json = "{\n" +
        "            \"transaction\": \"C53ECF838647FA5A4C780377025FEC7999AB4182590510CA461444B207AB74A9\",\n" +
        "            \"binary\": false\n" +
        "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testFullJson() throws JsonProcessingException, JSONException {

    TransactionRequestParams params = TransactionRequestParams.builder()
        .transaction(Hash256.of("C53ECF838647FA5A4C780377025FEC7999AB4182590510CA461444B207AB74A9"))
        .minLedger(UnsignedLong.ZERO)
        .maxLedger(UnsignedLong.ONE)
        .build();

    String json = "{\n" +
        "            \"transaction\": \"C53ECF838647FA5A4C780377025FEC7999AB4182590510CA461444B207AB74A9\",\n" +
        "            \"min_ledger\": 0,\n" +
        "            \"max_ledger\": 1,\n" +
        "            \"binary\": false\n" +
        "        }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
