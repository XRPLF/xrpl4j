package org.xrpl.xrpl4j.model.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;

class UnknownLedgerObjectTest extends AbstractJsonTest {

  @Test
  void deserializeUnknownDirectly() throws JsonProcessingException {
    String json = "{\n" +
      "      \"Flags\": 0,\n" +
      "      \"LedgerEntryType\": \"NFTokenPage2\",\n" +
      "      \"NFTokens\": [\n" +
      "        {\n" +
      "          \"NFToken\": {\n" +
      "            \"NFTokenID\": \"00000000C45132EF77F26FF18C2F0A60069BB5A269DFC8D10000099A00000000\",\n" +
      "            \"URI\": \"697066733A2F2F62616679626569676479727A74357366703775646D37687537367568377932366E6634646675796C71616266336F636C67747179353566627A6469\"\n" +
      "          }\n" +
      "        }\n" +
      "      ],\n" +
      "      \"PreviousTxnID\": \"A2F435594190FDE9078AF23083BEE0B9F7C7C8E0668FD2552AB691302976FFF8\",\n" +
      "      \"PreviousTxnLgrSeq\": 38845736,\n" +
      "      \"index\": \"C45132EF77F26FF18C2F0A60069BB5A269DFC8D1FFFFFFFFFFFFFFFFFFFFFFFF\"\n" +
      "    }";

    UnknownLedgerObject deserialized = objectMapper.readValue(json, UnknownLedgerObject.class);
    JsonNode asJsonNode = objectMapper.readValue(json, JsonNode.class);
    UnknownLedgerObject expected = UnknownLedgerObject.builder()
      .properties(asJsonNode)
      .build();

    assertThat(deserialized).isEqualTo(expected);
    assertThat(expected.ledgerEntryType()).isEqualTo("NFTokenPage2");
  }

  @Test
  void deserializeUnknownAsLedgerObject() throws JsonProcessingException {
    String json = "{\n" +
      "      \"Flags\": 0,\n" +
      "      \"LedgerEntryType\": \"NFTokenPage2\",\n" +
      "      \"NFTokens\": [\n" +
      "        {\n" +
      "          \"NFToken\": {\n" +
      "            \"NFTokenID\": \"00000000C45132EF77F26FF18C2F0A60069BB5A269DFC8D10000099A00000000\",\n" +
      "            \"URI\": \"697066733A2F2F62616679626569676479727A74357366703775646D37687537367568377932366E6634646675796C71616266336F636C67747179353566627A6469\"\n" +
      "          }\n" +
      "        }\n" +
      "      ],\n" +
      "      \"PreviousTxnID\": \"A2F435594190FDE9078AF23083BEE0B9F7C7C8E0668FD2552AB691302976FFF8\",\n" +
      "      \"PreviousTxnLgrSeq\": 38845736,\n" +
      "      \"index\": \"C45132EF77F26FF18C2F0A60069BB5A269DFC8D1FFFFFFFFFFFFFFFFFFFFFFFF\"\n" +
      "    }";

    LedgerObject deserialized = objectMapper.readValue(json, LedgerObject.class);
    JsonNode asJsonNode = objectMapper.readValue(json, JsonNode.class);
    UnknownLedgerObject expected = UnknownLedgerObject.builder()
      .properties(asJsonNode)
      .build();

    assertThat(deserialized).isEqualTo(expected);
    assertThat(expected.ledgerEntryType()).isEqualTo("NFTokenPage2");
  }
}