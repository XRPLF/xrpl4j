package org.xrpl.xrpl4j.model.client.ledger.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.ledger.LoanLedgerEntryParams;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public class LoanLedgerEntryParamsJsonTest extends AbstractJsonTest {

  @Test
  public void testLoanLedgerEntryParams() throws JSONException, JsonProcessingException {
    LoanLedgerEntryParams params = LoanLedgerEntryParams.builder()
      .loanBrokerId(
        Hash256.of("A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2")
      )
      .loanSeq(UnsignedInteger.valueOf(456))
      .build();

    String json = "{" +
      "  \"loan_broker_id\": " +
      "\"A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2\"," +
      "  \"loan_seq\": 456" +
      "}";

    assertCanSerializeAndDeserialize(params, json, LoanLedgerEntryParams.class);
  }
}
