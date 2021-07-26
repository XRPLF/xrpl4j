package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.ledger.LedgerHeader;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class LedgerResultJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    LedgerResult result = LedgerResult.builder()
        .status("success")
        .ledgerHash(Hash256.of("3652D7FD0576BC452C0D2E9B747BDD733075971D1A9A1D98125055DEF428721A"))
        .ledgerIndex(LedgerIndex.of(UnsignedLong.valueOf(54300940)))
        .validated(true)
        .ledger(
            LedgerHeader.builder()
                .accountHash(Hash256.of("B258A8BB4743FB74CBBD6E9F67E4A56C4432EA09E5805E4CC2DA26F2DBE8F3D1"))
                .closeTime(UnsignedLong.valueOf(638329271))
                .closeTimeHuman(ZonedDateTime.parse("2020-Mar-24 01:41:11.000000000 UTC",
                    DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss.SSSSSSSSS z"))
                    .withZoneSameLocal(ZoneId.of("UTC")))
                .closeTimeResolution(UnsignedInteger.valueOf(10))
                .closed(true)
                .ledgerHash(Hash256.of("3652D7FD0576BC452C0D2E9B747BDD733075971D1A9A1D98125055DEF428721A"))
                .ledgerIndex(LedgerIndex.of("54300940"))
                .parentCloseTime(UnsignedLong.valueOf(638329270))
                .parentHash(Hash256.of("AE996778246BC81F85D5AF051241DAA577C23BCA04C034A7074F93700194520D"))
                .totalCoins(XrpCurrencyAmount.ofDrops(99991024049618156L))
                .transactionHash(Hash256.of("FC6FFCB71B2527DDD630EE5409D38913B4D4C026AA6C3B14A3E9D4ED45CFE30D"))
                .build()
        )
        .build();

    String json = "{\n" +
        "    \"ledger\": {\n" +
        "      \"account_hash\": \"B258A8BB4743FB74CBBD6E9F67E4A56C4432EA09E5805E4CC2DA26F2DBE8F3D1\",\n" +
        "      \"close_time\": 638329271,\n" +
        "      \"close_time_human\": \"2020-Mar-24 01:41:11.000000000 UTC\",\n" +
        "      \"close_time_resolution\": 10,\n" +
        "      \"closed\": true,\n" +
        "      \"ledger_hash\": \"3652D7FD0576BC452C0D2E9B747BDD733075971D1A9A1D98125055DEF428721A\",\n" +
        "      \"ledger_index\": 54300940,\n" +
        "      \"parent_close_time\": 638329270,\n" +
        "      \"parent_hash\": \"AE996778246BC81F85D5AF051241DAA577C23BCA04C034A7074F93700194520D\",\n" +
        "      \"total_coins\": \"99991024049618156\",\n" +
        "      \"transaction_hash\": \"FC6FFCB71B2527DDD630EE5409D38913B4D4C026AA6C3B14A3E9D4ED45CFE30D\"\n" +
        "    },\n" +
        "    \"ledger_hash\": \"3652D7FD0576BC452C0D2E9B747BDD733075971D1A9A1D98125055DEF428721A\",\n" +
        "    \"ledger_index\": 54300940,\n" +
        "    \"status\": \"success\",\n" +
        "    \"validated\": true\n" +
        "  }";

    assertCanSerializeAndDeserialize(result, json);

  }

  @Test
  public void testCurrentJson() throws JsonProcessingException, JSONException {
    LedgerResult ledgerResult = LedgerResult.builder()
      .ledger(
        LedgerHeader.builder()
          .closed(false)
          .ledgerIndex(LedgerIndex.of("4"))
          .parentHash(Hash256.of("D2A87A6CA50F96068D0532C00EED693C631BFC03DD11D880E6157999A1C06538"))
          .build()
      )
      .ledgerCurrentIndex(LedgerIndex.of("4"))
      .status("success")
      .validated(false)
      .build();

    String json = "{\n" +
      "  \"ledger\" : {\n" +
      "    \"closed\" : false,\n" +
      "    \"ledger_index\" : 4,\n" +
      "    \"parent_hash\" : \"D2A87A6CA50F96068D0532C00EED693C631BFC03DD11D880E6157999A1C06538\"\n" +
      "  },\n" +
      "  \"ledger_current_index\" : 4,\n" +
      "  \"status\" : \"success\",\n" +
      "  \"validated\" : false\n" +
      "}";
    assertCanSerializeAndDeserialize(ledgerResult, json);
  }
}
