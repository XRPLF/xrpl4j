package org.xrpl.xrpl4j.model.client.mpt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.crypto.TestConstants.ED_ADDRESS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.MpTokenFlags;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenAmount;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenObjectAmount;

class MptHoldersResponseTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    MptHoldersResponse response = MptHoldersResponse.builder()
      .mpTokenIssuanceId(MpTokenIssuanceId.of("000004C463C52827307480341125DA0577DEFC38405B0E3E"))
      .limit(UnsignedInteger.ONE)
      .addMpTokens(
        MptHoldersMpToken.builder()
          .account(ED_ADDRESS)
          .flags(MpTokenFlags.UNSET)
          .mptAmount(MpTokenObjectAmount.of(20))
          .lockedAmount(MpTokenObjectAmount.of(1))
          .mpTokenIndex(Hash256.of("36D91DEE5EFE4A93119A8B84C944A528F2B444329F3846E49FE921040DE17E65"))
          .build()
      )
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .build();

    String json = "{\n" +
                  "    \"mpt_issuance_id\": \"000004C463C52827307480341125DA0577DEFC38405B0E3E\",\n" +
                  "    \"limit\":1,\n" +
                  "    \"ledger_index\": 1,\n" +
                  "    \"mptokens\": [{\n" +
                  "        \"account\": \"" + ED_ADDRESS + "\",\n" +
                  "        \"flags\": 0,\n" +
                  "        \"mpt_amount\": \"20\",\n" +
                  "        \"locked_amount\": \"1\",\n" +
                  "        \"mptoken_index\": \"36D91DEE5EFE4A93119A8B84C944A528F2B444329F3846E49FE921040DE17E65\"\n" +
                  "    }\n" +
                  "]\n" +
                  "}";

    assertCanSerializeAndDeserialize(response, json);
  }


}