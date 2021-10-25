package org.xrpl.xrpl4j.model.ledger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.util.Locale;

public class NonUsLocaleLedgerHeaderJsonTests {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    Locale.setDefault(new Locale("de", "DE"));
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void deserializeWithNonEnUsLocale() {
    String json = "{\n" +
      "  \"ledger_index\" : 54300940,\n" +
      "  \"ledger_hash\" : \"3652D7FD0576BC452C0D2E9B747BDD733075971D1A9A1D98125055DEF428721A\",\n" +
      "  \"account_hash\" : \"B258A8BB4743FB74CBBD6E9F67E4A56C4432EA09E5805E4CC2DA26F2DBE8F3D1\",\n" +
      "  \"close_time\" : 638329271,\n" +
      "  \"close_time_human\" : \"2020-Mar-24 01:41:11.000000000 UTC\",\n" +
      "  \"closed\" : true,\n" +
      "  \"parent_hash\" : \"AE996778246BC81F85D5AF051241DAA577C23BCA04C034A7074F93700194520D\",\n" +
      "  \"parent_close_time\" : 638329270,\n" +
      "  \"total_coins\" : \"99991024049618156\",\n" +
      "  \"transaction_hash\" : \"FC6FFCB71B2527DDD630EE5409D38913B4D4C026AA6C3B14A3E9D4ED45CFE30D\",\n" +
      "  \"transactions\" : [ {\n" +
      "    \"Account\" : \"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe\",\n" +
      "    \"Fee\" : \"12\",\n" +
      "    \"Sequence\" : 2062124,\n" +
      "    \"LastLedgerSequence\" : 13010048,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"TxnSignature\" : \"3045022100E1F74E892839A9818D991F1E7B3D069ED499A5D412DD6C8C2634E87D0A37D375022014" +
      "1AF3DCE6DA4D134614E49C99FFB1E498C238B46FC47CF3F79A989C4A2053AC\",\n" +
      "    \"Flags\" : 2147483648,\n" +
      "    \"Amount\" : \"1000000000\",\n" +
      "    \"Destination\" : \"rBkoiq4sVF5N6zu4QwZPm9iVQht4BtxtM1\",\n" +
      "    \"TransactionType\" : \"Payment\",\n" +
      "    \"hash\" : \"E22068A818EA853DD3B7B574FF58C3A84D1F664495FF6ECD11D3B03B1D2FC2F7\",\n" +
      "    \"validated\" : false\n" +
      "  }, {\n" +
      "    \"Account\" : \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"Fee\" : \"12\",\n" +
      "    \"Sequence\" : 1,\n" +
      "    \"SourceTag\" : 11747,\n" +
      "    \"Flags\" : 2147483648,\n" +
      "    \"Amount\" : \"10000\",\n" +
      "    \"Destination\" : \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
      "    \"DestinationTag\" : 23480,\n" +
      "    \"CancelAfter\" : 533257958,\n" +
      "    \"FinishAfter\" : 533171558,\n" +
      "    \"TransactionType\" : \"EscrowCreate\",\n" +
      "    \"hash\" : \"E939C30F233E3E6B0A9F829BDDA258CB9DA38D11C0F66C7D60E38B9D9FA987B8\",\n" +
      "    \"validated\" : false\n" +
      "  } ],\n" +
      "  \"close_time_resolution\" : 10\n" +
      "}";

    assertDoesNotThrow(() -> objectMapper.readValue(json, LedgerHeader.class));
  }
}
