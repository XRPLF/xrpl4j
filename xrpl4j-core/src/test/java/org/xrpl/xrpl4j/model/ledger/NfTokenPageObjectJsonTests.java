package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;

class NfTokenPageObjectJsonTests extends AbstractJsonTest {

  @Test
  void testFullJson() throws JSONException, JsonProcessingException {
    String json = "{\n" +
      "  \"LedgerEntryType\": \"NFTokenPage\",\n" +
      "  \"PreviousPageMin\":\n" +
      "    \"8A244DD75DAF4AC1EEF7D99253A7B83D2297818B2297818B70E264D2000002F2\",\n" +
      "  \"NextPageMin\":\n" +
      "    \"8A244DD75DAF4AC1EEF7D99253A7B83D2297818B2297818BE223B0AE0000010B\",\n" +
      "  \"PreviousTxnID\":\n" +
      "    \"95C8761B22894E328646F7A70035E9DFBECC90EDD83E43B7B973F626D21A0822\",\n" +
      "  \"PreviousTxnLgrSeq\":\n" +
      "    42891441,\n" +
      "  \"NFTokens\": [\n" +
      "    {\n" +
      "      \"NFToken\": {\n" +
      "        \"NFTokenID\":\n" +
      "            \"000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65\",\n" +
      "          \"URI\": \"697066733A2F2F62616679626569676479727A74357366703775646D37687537367568377932366E66346" +
      "46675796C71616266336F636C67747179353566627A6469\"\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"NFToken\": {\n" +
      "        \"NFTokenID\":\n" +
      "            \"00082134C4E16036D649C037D2DE7C58780DE1D985EEB98638FD4D1000001FE4\"\n" +
      "      }\n" +
      "    }\n" +
      "  ],\n" +
      "  \"index\": \"8A244DD75DAF4AC1EEF7D99253A7B83D2297818B2297818BAD5307A70000030B\"" +
      "}";

    NfTokenPageObject page = NfTokenPageObject.builder()
      .previousPageMin(Hash256.of("8A244DD75DAF4AC1EEF7D99253A7B83D2297818B2297818B70E264D2000002F2"))
      .nextPageMin(Hash256.of("8A244DD75DAF4AC1EEF7D99253A7B83D2297818B2297818BE223B0AE0000010B"))
      .previousTransactionId(Hash256.of("95C8761B22894E328646F7A70035E9DFBECC90EDD83E43B7B973F626D21A0822"))
      .previousTransactionLedgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(42891441)))
      .addNfTokens(
        NfTokenWrapper.of(
          NfToken.builder()
            .nfTokenId(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65"))
            .uri(NfTokenUri.of("697066733A2F2F62616679626569676479727A74357366703775646D37687537367568377932366" +
              "E6634646675796C71616266336F636C67747179353566627A6469"))
            .build()
        ),
        NfTokenWrapper.of(
          NfToken.builder()
            .nfTokenId(NfTokenId.of("00082134C4E16036D649C037D2DE7C58780DE1D985EEB98638FD4D1000001FE4"))
            .build()
        )
      )
      .index(Hash256.of("8A244DD75DAF4AC1EEF7D99253A7B83D2297818B2297818BAD5307A70000030B"))
      .build();

    assertCanSerializeAndDeserialize(page, json);
  }

  @Test
  void testMinimalJson() throws JSONException, JsonProcessingException {
    String json = "{\n" +
      "  \"LedgerEntryType\": \"NFTokenPage\",\n" +
      "  \"NFTokens\": [\n" +
      "    {\n" +
      "      \"NFToken\": {\n" +
      "        \"NFTokenID\":\n" +
      "            \"000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65\",\n" +
      "          \"URI\": \"697066733A2F2F62616679626569676479727A74357366703775646D37687537367568377932366E66346" +
      "46675796C71616266336F636C67747179353566627A6469\"\n" +
      "      }\n" +
      "    },\n" +
      "    {\n" +
      "      \"NFToken\": {\n" +
      "        \"NFTokenID\":\n" +
      "            \"00082134C4E16036D649C037D2DE7C58780DE1D985EEB98638FD4D1000001FE4\"\n" +
      "      }\n" +
      "    }\n" +
      "  ],\n" +
      "  \"index\": \"8A244DD75DAF4AC1EEF7D99253A7B83D2297818B2297818BAD5307A70000030B\"" +
      "}";

    NfTokenPageObject page = NfTokenPageObject.builder()
      .addNfTokens(
        NfTokenWrapper.of(
          NfToken.builder()
            .nfTokenId(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65"))
            .uri(NfTokenUri.of("697066733A2F2F62616679626569676479727A74357366703775646D376875373675683779323" +
              "66E6634646675796C71616266336F636C67747179353566627A6469"))
            .build()
        ),
        NfTokenWrapper.of(
          NfToken.builder()
            .nfTokenId(NfTokenId.of("00082134C4E16036D649C037D2DE7C58780DE1D985EEB98638FD4D1000001FE4"))
            .build()
        )
      )
      .index(Hash256.of("8A244DD75DAF4AC1EEF7D99253A7B83D2297818B2297818BAD5307A70000030B"))
      .build();

    assertCanSerializeAndDeserialize(page, json);
  }
}