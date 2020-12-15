package org.xrpl.xrpl4j.model.client.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.assertj.core.util.Lists;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.path.PathAlternative;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.PathStep;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ServerInfoResultJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    ServerInfoResult result = ServerInfoResult.builder()
        .status("success")
        .info(ServerInfo.builder()
            .buildVersion("1.5.0-rc1")
            .completeLedgers("54300020-54300729")
            .hostId("trace")
            .ioLatencyMs(UnsignedLong.ONE)
            .jqTransOverflow("0")
            .lastClose(ServerInfoLastClose.builder()
                .convergeTimeSeconds(2d)
                .proposers(UnsignedInteger.valueOf(34))
                .build())
            .load(ServerInfoLoad.builder()
                .addJobTypes(
                    JobType.builder()
                        .jobType("ledgerRequest")
                        .peakTime(UnsignedInteger.valueOf(4))
                        .perSecond(UnsignedInteger.valueOf(4))
                        .build(),
                    JobType.builder()
                        .jobType("untrustedProposal")
                        .peakTime(UnsignedInteger.valueOf(5))
                        .perSecond(UnsignedInteger.valueOf(43))
                        .build(),
                    JobType.builder()
                        .jobType("ledgerData")
                        .peakTime(UnsignedInteger.valueOf(337))
                        .averageTime(UnsignedInteger.valueOf(14))
                        .build(),
                    JobType.builder()
                        .jobType("clientCommand")
                        .inProgress(UnsignedInteger.valueOf(1))
                        .perSecond(UnsignedInteger.valueOf(9))
                        .build(),
                    JobType.builder()
                        .jobType("transaction")
                        .peakTime(UnsignedInteger.valueOf(8))
                        .perSecond(UnsignedInteger.valueOf(8))
                        .build(),
                    JobType.builder()
                        .jobType("batch")
                        .peakTime(UnsignedInteger.valueOf(5))
                        .perSecond(UnsignedInteger.valueOf(6))
                        .build(),
                    JobType.builder()
                        .jobType("advanceLedger")
                        .peakTime(UnsignedInteger.valueOf(96))
                        .averageTime(UnsignedInteger.valueOf(6))
                        .build(),
                    JobType.builder()
                        .jobType("fetchTxnData")
                        .perSecond(UnsignedInteger.valueOf(14))
                        .build()
                )
                .threads(UnsignedLong.valueOf(6))
                .build())
            .loadFactor(UnsignedInteger.ONE)
            .peers(UnsignedInteger.valueOf(21))
            .publicKeyNode("n9KUjqxCr5FKThSNXdzb7oqN8rYwScB2dUnNqxQxbEA17JkaWy5x")
            .publicKeyValidator("nHBk5DPexBjinXV8qHn7SEKzoxh2W92FxSbNTPgGtQYBzEF4msn9")
            .serverState("proposing")
            .serverStateDurationUs("1850969666")
            .time(ZonedDateTime.parse("2020-Mar-24 01:27:42.147330 UTC",
                DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss.SSSSSS z")))
            .upTime(UnsignedLong.valueOf(1984))
            .validatedLedger(ServerInfoLedger.builder()
                .age(UnsignedInteger.valueOf(2))
                .hash(Hash256.of("0D2D30837E05995AAAAA117294BB45AB0699AB1219605FFD23318E050C7166E9"))
                .reserveBaseXrp(UnsignedInteger.valueOf(20))
                .reserveIncXrp(UnsignedInteger.valueOf(5))
                .sequence(LedgerIndex.of(UnsignedLong.valueOf(54300729)))
                .build())
            .validationQuorum(UnsignedInteger.valueOf(29))
            .build())
        .build();

    String json = "{\n" +
        "    \"info\": {\n" +
        "      \"build_version\": \"1.5.0-rc1\",\n" +
        "      \"amendment_blocked\": false,\n" +
        "      \"complete_ledgers\": \"54300020-54300729\",\n" +
        "      \"hostid\": \"trace\",\n" +
        "      \"io_latency_ms\": 1,\n" +
        "      \"jq_trans_overflow\": \"0\",\n" +
        "      \"last_close\": {\n" +
        "        \"converge_time_s\": 2,\n" +
        "        \"proposers\": 34\n" +
        "      },\n" +
        "      \"load\": {\n" +
        "        \"job_types\": [\n" +
        "          {\n" +
        "            \"job_type\": \"ledgerRequest\",\n" +
        "            \"peak_time\": 4,\n" +
        "            \"per_second\": 4\n" +
        "          },\n" +
        "          {\n" +
        "            \"job_type\": \"untrustedProposal\",\n" +
        "            \"peak_time\": 5,\n" +
        "            \"per_second\": 43\n" +
        "          },\n" +
        "          {\n" +
        "            \"avg_time\": 14,\n" +
        "            \"job_type\": \"ledgerData\",\n" +
        "            \"peak_time\": 337\n" +
        "          },\n" +
        "          {\n" +
        "            \"in_progress\": 1,\n" +
        "            \"job_type\": \"clientCommand\",\n" +
        "            \"per_second\": 9\n" +
        "          },\n" +
        "          {\n" +
        "            \"job_type\": \"transaction\",\n" +
        "            \"peak_time\": 8,\n" +
        "            \"per_second\": 8\n" +
        "          },\n" +
        "          {\n" +
        "            \"job_type\": \"batch\",\n" +
        "            \"peak_time\": 5,\n" +
        "            \"per_second\": 6\n" +
        "          },\n" +
        "          {\n" +
        "            \"avg_time\": 6,\n" +
        "            \"job_type\": \"advanceLedger\",\n" +
        "            \"peak_time\": 96\n" +
        "          },\n" +
        "          {\n" +
        "            \"job_type\": \"fetchTxnData\",\n" +
        "            \"per_second\": 14\n" +
        "          }" +
        "        ],\n" +
        "        \"threads\": 6\n" +
        "      },\n" +
        "      \"load_factor\": 1,\n" +
        "      \"peers\": 21,\n" +
        "      \"pubkey_node\": \"n9KUjqxCr5FKThSNXdzb7oqN8rYwScB2dUnNqxQxbEA17JkaWy5x\",\n" +
        "      \"pubkey_validator\": \"nHBk5DPexBjinXV8qHn7SEKzoxh2W92FxSbNTPgGtQYBzEF4msn9\",\n" +
        "      \"server_state\": \"proposing\",\n" +
        "      \"server_state_duration_us\": \"1850969666\",\n" +
        "      \"time\": \"2020-Mar-24 01:27:42.147330 UTC\",\n" +
        "      \"uptime\": 1984,\n" +
        "      \"validated_ledger\": {\n" +
        "        \"age\": 2,\n" +
        "        \"hash\": \"0D2D30837E05995AAAAA117294BB45AB0699AB1219605FFD23318E050C7166E9\",\n" +
        "        \"reserve_base_xrp\": 20,\n" +
        "        \"reserve_inc_xrp\": 5,\n" +
        "        \"seq\": 54300729\n" +
        "      },\n" +
        "      \"validation_quorum\": 29\n" +
        "    },\n" +
        "    \"status\": \"success\"\n" +
        "  }";

    assertCanSerializeAndDeserialize(result, json);
  }
}
