package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * JSON validation tests for {@link DepositAuthorizedRequestParams}.
 */
public class DepositAuthorizedRequestParamsJsonTests extends AbstractJsonTest {

  private static final Address SOURCE_ACCOUNT = Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk58");
  private static final Address DESTINATION_ACCOUNT = Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59");

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    DepositAuthorizedRequestParams result = DepositAuthorizedRequestParams.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerIndex(LedgerIndex.VALIDATED)
      .build();

    String json = "{" +
      " \"source_account\":\"" + SOURCE_ACCOUNT.value() + "\"," +
      " \"destination_account\":\"" + DESTINATION_ACCOUNT.value() + "\"," +
      " \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }
}
