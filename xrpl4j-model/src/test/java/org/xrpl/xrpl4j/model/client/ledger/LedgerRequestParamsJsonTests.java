package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerSpecifier;

public class LedgerRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {

    LedgerRequestParams params = LedgerRequestParams.builder()
        .ledgerSpecifier(LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED))
        .build();

    String json = "{\n" +
        "            \"ledger_index\": \"validated\",\n" +
        "            \"accounts\": false,\n" +
        "            \"full\": false,\n" +
        "            \"transactions\": false,\n" +
        "            \"expand\": true,\n" +
        "            \"binary\": false,\n" +
        "            \"queue\": false,\n" +
        "            \"owner_funds\": false\n" +
        "        }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
