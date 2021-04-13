package org.xrpl.xrpl4j.model.client.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Range;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Unit tests for {@link ServerInfoResult}.
 */
public class ServerInfoResultTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    ServerInfoResult result = ServerInfoResult.builder()
      .status("success")
      .info(serverInfo("54300020-54300729"))
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
      "        \"base_fee_xrp\": 0.00001,\n" +
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

  @Test
  public void testJsonDeserialization() throws JsonProcessingException, JSONException {

    ServerInfoResult result = ServerInfoResult.builder().info(updatedServerInfo()).build();

    String json = "{\n" +
            "    \"info\": {\n" +
            "      \"build_version\": \"1.7.0\",\n" +
            "      \"amendment_blocked\": false,\n" +
            "      \"complete_ledgers\": \"61881385-62562429\",\n" +
            "      \"hostid\": \"LARD\",\n" +
            "      \"io_latency_ms\": 2,\n" +
            "      \"jq_trans_overflow\": \"0\",\n" +
            "      \"last_close\": {\n" +
            "        \"converge_time_s\": 3.002,\n" +
            "        \"proposers\": 38\n" +
            "      },\n" +
            "      \"load_factor\": 511.83203125,\n" +
            "      \"load_factor_server\": 1,\n" +
            "      \"peers\": 261,\n" +
            "      \"pubkey_node\": \"n9MozjnGB3tpULewtTsVtuudg5JqYFyV3QFdAtVLzJaxHcBaxuXD\",\n" +
            "      \"server_state\": \"full\",\n" +
            "      \"server_state_duration_us\": \"2274468435925\",\n" +
            "      \"time\": \"2021-Mar-30 15:37:51.486384 UTC\",\n" +
            "      \"uptime\": 2274704,\n" +
            "      \"validated_ledger\": {\n" +
            "        \"age\": 4,\n" +
            "        \"base_fee_xrp\": 0.00001,\n" +
            "        \"hash\": \"E5A958048D98D4EFEEDD2BC3F36D23893BBC1D9354CB3E739068D2DFDE3D1AA3\",\n" +
            "        \"reserve_base_xrp\": 20,\n" +
            "        \"reserve_inc_xrp\": 5,\n" +
            "        \"seq\": 62562429\n" +
            "      },\n" +
            "      \"validation_quorum\": 31\n" +
            "    }\n" +
            "  },\n" +
            "  \"status\": \"success\",\n" +
            "  \"type\": \"response\"\n" +
            "}";

    assertCanSerializeAndDeserialize(result, json);

  }

  @Test
  public void completeLedgersRanges() {
    ServerInfo serverInfo = serverInfo("empty");
    assertThat(serverInfo.completeLedgerRanges()).hasSize(0);

    serverInfo = serverInfo("");
    assertThat(serverInfo.completeLedgerRanges()).hasSize(0);

    serverInfo = serverInfo("foo");
    assertThat(serverInfo.completeLedgerRanges()).hasSize(0);

    serverInfo = serverInfo("foo100");
    assertThat(serverInfo.completeLedgerRanges()).hasSize(0);

    serverInfo = serverInfo("1--2");
    assertThat(serverInfo.completeLedgerRanges()).hasSize(0);

    serverInfo = serverInfo("0");
    List<Range<UnsignedLong>> ranges = serverInfo.completeLedgerRanges();
    assertThat(ranges).hasSize(1);
    assertThat(ranges.get(0).contains(UnsignedLong.ZERO)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.ONE)).isFalse();

    serverInfo = serverInfo("1");
    ranges = serverInfo.completeLedgerRanges();
    assertThat(ranges).hasSize(1);
    assertThat(ranges.get(0).contains(UnsignedLong.ZERO)).isFalse();
    assertThat(ranges.get(0).contains(UnsignedLong.ONE)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.valueOf(2L))).isFalse();

    serverInfo = serverInfo("1-2");
    ranges = serverInfo.completeLedgerRanges();
    assertThat(ranges).hasSize(1);
    assertThat(ranges.get(0).contains(UnsignedLong.ZERO)).isFalse();
    assertThat(ranges.get(0).contains(UnsignedLong.ONE)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.valueOf(2))).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = serverInfo("0-" + UnsignedLong.MAX_VALUE.toString());
    ranges = serverInfo.completeLedgerRanges();
    assertThat(ranges).hasSize(1);

    serverInfo = serverInfo("0-foo");
    ranges = serverInfo.completeLedgerRanges();
    assertThat(ranges).hasSize(0);

    serverInfo = serverInfo("foo-0");
    ranges = serverInfo.completeLedgerRanges();
    assertThat(ranges).hasSize(0);

    serverInfo = serverInfo("foo-0,bar-20");
    ranges = serverInfo.completeLedgerRanges();
    assertThat(ranges).hasSize(0);

    serverInfo = serverInfo("0-10,20-30");
    ranges = serverInfo.completeLedgerRanges();
    assertThat(ranges).hasSize(2);
    assertThat(ranges.get(0).contains(UnsignedLong.ZERO)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.ONE)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.valueOf(10L))).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.valueOf(11L))).isFalse();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(19L))).isFalse();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(20L))).isTrue();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(30L))).isTrue();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(31L))).isFalse();
    assertThat(ranges.get(1).contains(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = serverInfo("0-10, 20-30 "); // <-- Test the trim function
    ranges = serverInfo.completeLedgerRanges();
    assertThat(ranges).hasSize(2);
    assertThat(ranges.get(0).contains(UnsignedLong.ZERO)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.ONE)).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.valueOf(10L))).isTrue();
    assertThat(ranges.get(0).contains(UnsignedLong.valueOf(11L))).isFalse();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(19L))).isFalse();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(20L))).isTrue();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(30L))).isTrue();
    assertThat(ranges.get(1).contains(UnsignedLong.valueOf(31L))).isFalse();
    assertThat(ranges.get(1).contains(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = serverInfo(UnsignedLong.MAX_VALUE.toString());
    ranges = serverInfo.completeLedgerRanges();
    assertThat(ranges).hasSize(1);
    assertThat(ranges.get(0).contains(UnsignedLong.ZERO)).isFalse();
    assertThat(ranges.get(0).contains(UnsignedLong.ONE)).isFalse();
    assertThat(ranges.get(0).contains(UnsignedLong.MAX_VALUE)).isTrue();
  }

  @Test
  public void isLedgerInCompleteLedgers() {
    ServerInfo serverInfo = serverInfo("empty");
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ZERO)).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ONE)).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = serverInfo("");
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ZERO)).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ONE)).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = serverInfo("foo");
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ZERO)).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ONE)).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = serverInfo("foo100");
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ZERO)).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ONE)).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = serverInfo("1--2");
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ZERO)).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ONE)).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = serverInfo("0");
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ZERO)).isTrue();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ONE)).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = serverInfo("1");
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ZERO)).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ONE)).isTrue();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = serverInfo("1-2");
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ZERO)).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ONE)).isTrue();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = serverInfo("0-" + UnsignedLong.MAX_VALUE.toString());
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ZERO)).isTrue();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ONE)).isTrue();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.MAX_VALUE)).isTrue();

    serverInfo = serverInfo("0-10,20-30");
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ZERO)).isTrue();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ONE)).isTrue();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.valueOf(19))).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.valueOf(20))).isTrue();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.valueOf(21))).isTrue();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.valueOf(29))).isTrue();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.valueOf(30))).isTrue();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.valueOf(31))).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = serverInfo("0-10, 20-30 "); // <-- Test the trim function
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ZERO)).isTrue();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ONE)).isTrue();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.valueOf(19))).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.valueOf(20))).isTrue();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.valueOf(30))).isTrue();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.valueOf(31))).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.MAX_VALUE)).isFalse();

    serverInfo = serverInfo(UnsignedLong.MAX_VALUE.toString());
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ZERO)).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.ONE)).isFalse();
    assertThat(serverInfo.isLedgerInCompleteLedgers(UnsignedLong.MAX_VALUE)).isTrue();
  }

  /**
   * Helper method to construct an instance of {@link ServerInfo} with {@code completeLedgers} in {@link
   * ServerInfo#completeLedgers()}.
   *
   * @param completeLedgers A {@link String} with the value of completeLedgers.
   *
   * @return An instance of {@link ServerInfo}.
   */
  private ServerInfo serverInfo(final String completeLedgers) {
    Objects.requireNonNull(completeLedgers);

    return ServerInfo.builder()
      .buildVersion("1.5.0-rc1")
      .completeLedgers(completeLedgers) // <-- use completeLedgers here.
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
      .loadFactor(BigDecimal.ONE)
      .peers(UnsignedInteger.valueOf(21))
      .publicKeyNode("n9KUjqxCr5FKThSNXdzb7oqN8rYwScB2dUnNqxQxbEA17JkaWy5x")
      .publicKeyValidator("nHBk5DPexBjinXV8qHn7SEKzoxh2W92FxSbNTPgGtQYBzEF4msn9")
      .serverState("proposing")
      .serverStateDurationUs("1850969666")
      .time(ZonedDateTime.parse("2020-Mar-24 01:27:42.147330 UTC",
        DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss.SSSSSS z")).withZoneSameLocal(ZoneId.of("UTC")))
      .upTime(UnsignedLong.valueOf(1984))
      .validatedLedger(ServerInfoLedger.builder()
        .age(UnsignedInteger.valueOf(2))
        .hash(Hash256.of("0D2D30837E05995AAAAA117294BB45AB0699AB1219605FFD23318E050C7166E9"))
        .reserveBaseXrp(UnsignedInteger.valueOf(20))
        .reserveIncXrp(UnsignedInteger.valueOf(5))
        .sequence(LedgerIndex.of(UnsignedLong.valueOf(54300729)))
        .baseFeeXrp(new BigDecimal("0.00001"))
        .build())
      .validationQuorum(UnsignedInteger.valueOf(29))
      .build();
  }

  /**
   * Helper method to construct an instance of {@link ServerInfo} with {@code completeLedgers} in {@link
   * ServerInfo#completeLedgers()}.
   *
   * @return An instance of {@link ServerInfo}.
   */
  private ServerInfo updatedServerInfo() {

    return ServerInfo.builder()
      .buildVersion("1.7.0")
      .completeLedgers("61881385-62562429")
      .hostId("LARD")
      .ioLatencyMs(UnsignedLong.valueOf(2))
      .jqTransOverflow("0")
      .lastClose(ServerInfoLastClose.builder()
        .convergeTimeSeconds(3.002)
        .proposers(UnsignedInteger.valueOf(38))
        .build())
      .loadFactor(new BigDecimal("511.83203125"))
      .loadFactorServer(BigDecimal.ONE)
      .peers(UnsignedInteger.valueOf(261))
      .publicKeyNode("n9MozjnGB3tpULewtTsVtuudg5JqYFyV3QFdAtVLzJaxHcBaxuXD")
      .serverState("full")
      .serverStateDurationUs("2274468435925")
      .time(ZonedDateTime.parse("2021-Mar-30 15:37:51.486384 UTC",
        DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss.SSSSSS z")).withZoneSameLocal(ZoneId.of("UTC")))
      .upTime(UnsignedLong.valueOf(2274704))
      .validatedLedger(ServerInfoLedger.builder()
        .age(UnsignedInteger.valueOf(4))
        .hash(Hash256.of("E5A958048D98D4EFEEDD2BC3F36D23893BBC1D9354CB3E739068D2DFDE3D1AA3"))
        .reserveBaseXrp(UnsignedInteger.valueOf(20))
        .reserveIncXrp(UnsignedInteger.valueOf(5))
        .sequence(LedgerIndex.of(UnsignedLong.valueOf(62562429)))
        .baseFeeXrp(new BigDecimal("0.00001"))
        .build())
      .validationQuorum(UnsignedInteger.valueOf(31))
      .build();
  }
}
