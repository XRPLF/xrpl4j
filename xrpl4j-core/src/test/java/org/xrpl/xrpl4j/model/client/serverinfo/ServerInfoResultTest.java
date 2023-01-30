package org.xrpl.xrpl4j.model.client.serverinfo;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo.LastClose;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo.ValidatedLedger;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Unit tests for {@link ServerInfoResult}.
 */
public class ServerInfoResultTest extends AbstractJsonTest {

  @Test
  void serverInfoResultTest() throws JSONException, JsonProcessingException {
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
      "        \"reserve_base_xrp\": 20.1,\n" +
      "        \"reserve_inc_xrp\": 5.0,\n" +
      "        \"seq\": 62562429\n" +
      "      },\n" +
      "      \"validation_quorum\": 31\n" +
      "  },\n" +
      "  \"status\": \"success\"\n" +
      "}";

    ServerInfo serverInfo = RippledServerInfo.builder()
      .buildVersion("1.7.0")
      .completeLedgers(LedgerRangeUtils.completeLedgersToListOfRange("61881385-62562429"))
      .hostId("LARD")
      .ioLatencyMs(UnsignedLong.valueOf(2))
      .jqTransOverflow("0")
      .lastClose(LastClose.builder()
        .convergeTimeSeconds(BigDecimal.valueOf(3.002))
        .proposers(UnsignedInteger.valueOf(38))
        .build())
      .loadFactor(new BigDecimal("511.83203125"))
      .loadFactorServer(BigDecimal.ONE)
      .peers(UnsignedInteger.valueOf(261))
      .publicKeyNode("n9MozjnGB3tpULewtTsVtuudg5JqYFyV3QFdAtVLzJaxHcBaxuXD")
      .serverState("full")
      .serverStateDurationUs("2274468435925")
      .time(ZonedDateTime.parse("2021-Mar-30 15:37:51.486384 UTC",
        DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss.SSSSSS z", Locale.US)).withZoneSameLocal(ZoneId.of("UTC")))
      .upTime(UnsignedLong.valueOf(2274704))
      .validatedLedger(ValidatedLedger.builder()
        .age(UnsignedInteger.valueOf(4))
        .hash(Hash256.of("E5A958048D98D4EFEEDD2BC3F36D23893BBC1D9354CB3E739068D2DFDE3D1AA3"))
        .reserveBaseXrp(XrpCurrencyAmount.ofDrops(20100000))
        .reserveIncXrp(XrpCurrencyAmount.ofDrops(5000000))
        .sequence(LedgerIndex.of(UnsignedInteger.valueOf(62562429)))
        .baseFeeXrp(new BigDecimal("0.000010"))
        .build())
      .validationQuorum(UnsignedInteger.valueOf(31))
      .build();
    ImmutableServerInfoResult.Builder resultBuilder = ServerInfoResult.builder()
      .info(serverInfo)
      .status("success");

    ServerInfoResult result = Assertions.assertDoesNotThrow(() -> resultBuilder.build());

    assertCanDeserialize(json, result);
    assertThat(result.info()).isEqualTo(serverInfo);
  }
}
