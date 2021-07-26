package org.xrpl.xrpl4j.model.client.fees;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class FeeResultJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    FeeResult feeResult = FeeResult.builder()
        .currentLedgerSize(UnsignedInteger.valueOf(56))
        .currentQueueSize(UnsignedInteger.valueOf(11))
        .drops(
            FeeDrops.builder()
                .baseFee(XrpCurrencyAmount.ofDrops(10))
                .medianFee(XrpCurrencyAmount.ofDrops(10000))
                .minimumFee(XrpCurrencyAmount.ofDrops(10))
                .openLedgerFee(XrpCurrencyAmount.ofDrops(2653937))
                .build()
        )
        .expectedLedgerSize(UnsignedInteger.valueOf(55))
        .ledgerCurrentIndex(LedgerIndex.of(UnsignedLong.valueOf(26575101)))
        .levels(
            FeeLevels.builder()
                .medianLevel(XrpCurrencyAmount.ofDrops(256000))
                .minimumLevel(XrpCurrencyAmount.ofDrops(256))
                .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
                .referenceLevel(XrpCurrencyAmount.ofDrops(256))
                .build()
        )
        .maxQueueSize(UnsignedInteger.valueOf(1100))
        .status("success")
        .build();

    String json = "{\n" +
        "        \"current_ledger_size\": \"56\",\n" +
        "        \"current_queue_size\": \"11\",\n" +
        "        \"drops\": {\n" +
        "            \"base_fee\": \"10\",\n" +
        "            \"median_fee\": \"10000\",\n" +
        "            \"minimum_fee\": \"10\",\n" +
        "            \"open_ledger_fee\": \"2653937\"\n" +
        "        },\n" +
        "        \"expected_ledger_size\": \"55\",\n" +
        "        \"ledger_current_index\": 26575101,\n" +
        "        \"levels\": {\n" +
        "            \"median_level\": \"256000\",\n" +
        "            \"minimum_level\": \"256\",\n" +
        "            \"open_ledger_level\": \"67940792\",\n" +
        "            \"reference_level\": \"256\"\n" +
        "        },\n" +
        "        \"max_queue_size\": \"1100\",\n" +
        "        \"status\": \"success\"\n" +
        "    }";

    assertCanSerializeAndDeserialize(feeResult, json);
  }
}
