package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

public class LedgerRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  @Deprecated
  public void oldLedgerHashStillWorks() throws JsonProcessingException, JSONException {
    LedgerRequestParams params = LedgerRequestParams.builder()
      .ledgerHash(Hash256.of("6B1011EF3BC3ED619B15979EF75C1C60D9181F3DDE641AD3019318D3900CEE2E"))
      .build();

    String json = "{\n" +
      "            \"ledger_hash\": \"6B1011EF3BC3ED619B15979EF75C1C60D9181F3DDE641AD3019318D3900CEE2E\",\n" +
      "            \"accounts\": false,\n" +
      "            \"full\": false,\n" +
      "            \"transactions\": false,\n" +
      "            \"expand\": true,\n" +
      "            \"binary\": false,\n" +
      "            \"queue\": false,\n" +
      "            \"owner_funds\": false\n" +
      "        }";
  }

  @Test
  @Deprecated
  public void oldLedgerIndexStillWorks() throws JsonProcessingException, JSONException {
    LedgerRequestParams params = LedgerRequestParams.builder()
      .ledgerIndex(LedgerIndex.VALIDATED)
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
  }

  @Test
  @Deprecated
  public void oldNumericalLedgerIndexStillWorks() throws JsonProcessingException, JSONException {
    LedgerRequestParams params = LedgerRequestParams.builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .build();

    String json = "{\n" +
      "            \"ledger_index\": 1,\n" +
      "            \"accounts\": false,\n" +
      "            \"full\": false,\n" +
      "            \"transactions\": false,\n" +
      "            \"expand\": true,\n" +
      "            \"binary\": false,\n" +
      "            \"queue\": false,\n" +
      "            \"owner_funds\": false\n" +
      "        }";
  }

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
