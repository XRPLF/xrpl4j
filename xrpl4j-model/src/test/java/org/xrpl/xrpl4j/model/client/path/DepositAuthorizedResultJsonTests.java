package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * JSON validation tests for {@link DepositAuthorizedResult}.
 */
public class DepositAuthorizedResultJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    DepositAuthorizedResult result = DepositAuthorizedResult.builder()
      .sourceAccount(Address.of("rEfNaaEni2e67iNPTncZtGNq6z6BJGPCJM"))
      .destinationAccount(Address.of("rHwhrL91UBRLoSdKtajXPF2otfTncxKWwu"))
      .ledgerIndex(LedgerIndex.CURRENT)
      .ledgerCurrentIndex(LedgerIndex.of("9"))
      .status("success")
      .depositAuthorized(true)
      .validated(true)
      .build();

    String json = "{\n" +
      " \"deposit_authorized\":true," +
      " \"source_account\":\"rEfNaaEni2e67iNPTncZtGNq6z6BJGPCJM\"," +
      " \"destination_account\":\"rHwhrL91UBRLoSdKtajXPF2otfTncxKWwu\"," +
      " \"ledger_index\": \"current\"," +
      " \"ledger_current_index\":9," +
      " \"status\":\"success\"," +
      " \"deposit_authorized\": true," +
      " \"validated\":true" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }
}
