package org.xrpl.xrpl4j.model.client.mpt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Marker;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

class MptHoldersRequestParamsTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    MptHoldersRequestParams params = MptHoldersRequestParams.builder()
      .mpTokenIssuanceId(MpTokenIssuanceId.of("ABCD"))
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .marker(Marker.of("foo"))
      .limit(UnsignedInteger.valueOf(1))
      .build();

    String json = "{" +
                  "\"mpt_issuance_id\":\"ABCD\"," +
                  "\"ledger_index\":\"validated\"," +
                  "\"marker\":\"foo\"," +
                  "\"limit\":1" +
                  "}";

    assertCanSerializeAndDeserialize(params, json);
  }

}