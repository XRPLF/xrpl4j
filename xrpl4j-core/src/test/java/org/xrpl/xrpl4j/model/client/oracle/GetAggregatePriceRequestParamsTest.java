package org.xrpl.xrpl4j.model.client.oracle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.OracleLedgerEntryParams;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.OracleDocumentId;

class GetAggregatePriceRequestParamsTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    GetAggregatePriceRequestParams params = GetAggregatePriceRequestParams.builder()
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .baseAsset("XRP")
      .quoteAsset("USD")
      .trim(UnsignedInteger.valueOf(20))
      .addOracles(
        OracleLedgerEntryParams.builder()
          .account(Address.of("rp047ow9WcPmnNpVHMQV5A4BF6vaL9Abm6"))
          .oracleDocumentId(OracleDocumentId.of(UnsignedInteger.valueOf(34)))
          .build()
      )
      .build();

    String json = "{\n" +
      "      \"ledger_index\": \"current\",\n" +
      "      \"base_asset\": \"XRP\",\n" +
      "      \"quote_asset\": \"USD\",\n" +
      "      \"trim\": 20,\n" +
      "      \"oracles\": [\n" +
      "        {\n" +
      "          \"account\": \"rp047ow9WcPmnNpVHMQV5A4BF6vaL9Abm6\",\n" +
      "          \"oracle_document_id\": 34\n" +
      "        }\n" +
      "      ]\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }
}