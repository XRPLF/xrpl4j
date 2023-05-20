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
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo.ValidatedLedger;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/**
 * Unit tests for {@link ClioServerInfo}.
 */
public class ClioServerInfoTest extends AbstractJsonTest {

  @Test
  public void testClioServerInfoJson() throws JsonProcessingException {
    logger.info("Default Locale: {}", Locale.getDefault());
    ServerInfoResult clioResult = ServerInfoResult.builder()
      .status("success")
      .info(clioServerInfo("54300020-54300729"))
      .build();

    String json = "{\n" +
      "      \"status\":\"success\",\n" +
      "      \"info\":{\n" +
      "      \"clio_version\":\"1.5.0-rc1\",\n" +
      "        \"rippled_version\":\"1.5.0-rc1\",\n" +
      "        \"complete_ledgers\":\"54300020-54300729\",\n" +
      "        \"load_factor\":1,\n" +
      "        \"validated_ledger\":{\n" +
      "        \"age\":2,\n" +
      "          \"hash\":\"0D2D30837E05995AAAAA117294BB45AB0699AB1219605FFD23318E050C7166E9\",\n" +
      "          \"reserve_base_xrp\":20,\n" +
      "          \"reserve_inc_xrp\":5,\n" +
      "          \"seq\":54300729,\n" +
      "          \"base_fee_xrp\":0.000010\n" +
      "      },\n" +
      "      \"validation_quorum\":29\n" +
      "    }\n" +
      "  }";

    assertCanDeserialize(json, clioResult);
    assertThat(clioResult.info().map(($) -> false, ($) -> true, ($) -> false).booleanValue()).isTrue();

    boolean inRange = clioResult.info().map(
      ($) -> false,
      clioServerInfoCopy -> clioServerInfoCopy.isLedgerInCompleteLedgers(UnsignedLong.valueOf(54300025)),
      ($) -> false
    );
    assertThat(inRange).isTrue();

    boolean outOfRange = clioResult.info().map(
      ($) -> false,
      clioServerInfoCopy -> clioServerInfoCopy.isLedgerInCompleteLedgers(UnsignedLong.valueOf(54300019)),
      ($) -> false
    );
    assertThat(outOfRange).isFalse();
  }

  @Test
  public void deserializeActualClioResponse() throws JsonProcessingException {
    String json = "{\n" +
      "    \"info\": {\n" +
      "      \"complete_ledgers\": \"32570-73271589\",\n" +
      "      \"counters\": {\n" +
      "        \"jsonrpc\": {\n" +
      "          \"gateway_balances\": {\n" +
      "            \"started\": \"5\",\n" +
      "            \"finished\": \"5\",\n" +
      "            \"errored\": \"0\",\n" +
      "            \"forwarded\": \"0\",\n" +
      "            \"duration_us\": \"589000\"\n" +
      "          },\n" +
      "          \"account_nfts\": {\n" +
      "            \"started\": \"6\",\n" +
      "            \"finished\": \"0\",\n" +
      "            \"errored\": \"6\",\n" +
      "            \"forwarded\": \"0\",\n" +
      "            \"duration_us\": \"0\"\n" +
      "          },\n" +
      "          \"account_info\": {\n" +
      "            \"started\": \"3\",\n" +
      "            \"finished\": \"1\",\n" +
      "            \"errored\": \"2\",\n" +
      "            \"forwarded\": \"1\",\n" +
      "            \"duration_us\": \"3433\"\n" +
      "          },\n" +
      "          \"fee\": {\n" +
      "            \"started\": \"154180\",\n" +
      "            \"finished\": \"154174\",\n" +
      "            \"errored\": \"6\",\n" +
      "            \"forwarded\": \"154180\",\n" +
      "            \"duration_us\": \"573261759\"\n" +
      "          },\n" +
      "          \"server_info\": {\n" +
      "            \"started\": \"173218\",\n" +
      "            \"finished\": \"173218\",\n" +
      "            \"errored\": \"0\",\n" +
      "            \"forwarded\": \"0\",\n" +
      "            \"duration_us\": \"872875177\"\n" +
      "          },\n" +
      "          \"subscribe\": {\n" +
      "            \"started\": \"136\",\n" +
      "            \"finished\": \"136\",\n" +
      "            \"errored\": \"0\",\n" +
      "            \"forwarded\": \"0\",\n" +
      "            \"duration_us\": \"424382\"\n" +
      "          },\n" +
      "          \"ledger\": {\n" +
      "            \"started\": \"216\",\n" +
      "            \"finished\": \"216\",\n" +
      "            \"errored\": \"0\",\n" +
      "            \"forwarded\": \"0\",\n" +
      "            \"duration_us\": \"703692\"\n" +
      "          },\n" +
      "          \"account_lines\": {\n" +
      "            \"started\": \"1\",\n" +
      "            \"finished\": \"1\",\n" +
      "            \"errored\": \"0\",\n" +
      "            \"forwarded\": \"0\",\n" +
      "            \"duration_us\": \"5457\"\n" +
      "          },\n" +
      "          \"account_tx\": {\n" +
      "            \"started\": \"175\",\n" +
      "            \"finished\": \"171\",\n" +
      "            \"errored\": \"4\",\n" +
      "            \"forwarded\": \"0\",\n" +
      "            \"duration_us\": \"5501225\"\n" +
      "          }\n" +
      "        },\n" +
      "        \"subscriptions\": {\n" +
      "          \"ledger\": 0,\n" +
      "          \"transactions\": 0,\n" +
      "          \"transactions_proposed\": 0,\n" +
      "          \"manifests\": 0,\n" +
      "          \"validations\": 0,\n" +
      "          \"account\": 0,\n" +
      "          \"accounts_proposed\": 0,\n" +
      "          \"books\": 0\n" +
      "        }\n" +
      "      },\n" +
      "      \"load_factor\": 1,\n" +
      "      \"clio_version\": \"0.3.0-b3\",\n" +
      "      \"validation_quorum\": 28,\n" +
      "      \"rippled_version\": \"1.9.1\",\n" +
      "      \"validated_ledger\": {\n" +
      "        \"age\": 4,\n" +
      "        \"hash\": \"CA4F5338B91688EAB0EF087E6A087DCC170E77E979C94CAF456FD7D9F9DD404E\",\n" +
      "        \"seq\": 73271589,\n" +
      "        \"base_fee_xrp\": 1E-5,\n" +
      "        \"reserve_base_xrp\": 1E1,\n" +
      "        \"reserve_inc_xrp\": 2E0\n" +
      "      }\n" +
      "    },\n" +
      "    \"cache\": {\n" +
      "      \"size\": 15898602,\n" +
      "      \"is_full\": true,\n" +
      "      \"latest_ledger_seq\": 73271589\n" +
      "    },\n" +
      "    \"etl\": {\n" +
      "      \"etl_sources\": [\n" +
      "        {\n" +
      "          \"validated_range\": \"73236224-73271589\",\n" +
      "          \"is_connected\": \"1\",\n" +
      "          \"ip\": \"34.217.210.125\",\n" +
      "          \"ws_port\": \"51233\",\n" +
      "          \"grpc_port\": \"50051\",\n" +
      "          \"last_msg_age_seconds\": \"0\"\n" +
      "        },\n" +
      "        {\n" +
      "          \"validated_range\": \"73239737-73271589\",\n" +
      "          \"is_connected\": \"1\",\n" +
      "          \"ip\": \"18.236.198.183\",\n" +
      "          \"ws_port\": \"51233\",\n" +
      "          \"grpc_port\": \"50051\",\n" +
      "          \"last_msg_age_seconds\": \"0\"\n" +
      "        },\n" +
      "        {\n" +
      "          \"validated_range\": \"73121158-73271589\",\n" +
      "          \"is_connected\": \"1\",\n" +
      "          \"ip\": \"34.209.3.47\",\n" +
      "          \"ws_port\": \"51233\",\n" +
      "          \"grpc_port\": \"50051\",\n" +
      "          \"last_msg_age_seconds\": \"0\"\n" +
      "        },\n" +
      "        {\n" +
      "          \"validated_range\": \"73086443-73271589\",\n" +
      "          \"is_connected\": \"1\",\n" +
      "          \"ip\": \"52.37.146.148\",\n" +
      "          \"ws_port\": \"51233\",\n" +
      "          \"grpc_port\": \"50051\",\n" +
      "          \"last_msg_age_seconds\": \"0\"\n" +
      "        }\n" +
      "      ],\n" +
      "      \"is_writer\": false,\n" +
      "      \"read_only\": true,\n" +
      "      \"last_publish_age_seconds\": \"2\"\n" +
      "    },\n" +
      "    \"validated\": true,\n" +
      "    \"status\": \"success\",\n" +
      "    \"warnings\": [\n" +
      "      \"This is a clio server. clio only serves validated data. If you want to talk to rippled, include" +
      "         'ledger_index':'current' in your request\"\n" +
      "    ]\n" +
      "  }";

    ClioServerInfo clioServerInfo = ClioServerInfo.builder()
      .completeLedgers(LedgerRangeUtils.completeLedgersToListOfRange("32570-73271589"))
      .loadFactor(BigDecimal.ONE)
      .clioVersion("0.3.0-b3")
      .validationQuorum(UnsignedInteger.valueOf(28))
      .rippledVersion("1.9.1")
      .validatedLedger(ValidatedLedger.builder()
        .age(UnsignedInteger.valueOf(4))
        .hash(Hash256.of("CA4F5338B91688EAB0EF087E6A087DCC170E77E979C94CAF456FD7D9F9DD404E"))
        .sequence(LedgerIndex.of(UnsignedInteger.valueOf(73271589)))
        .reserveBaseXrp(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(10)))
        .reserveIncXrp(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(2)))
        .baseFeeXrp(new BigDecimal("0.000010"))
        .build())
      .build();

    ServerInfoResult clioResult = ServerInfoResult.builder()
      .status("success")
      .info(clioServerInfo)
      .build();

    assertCanDeserialize(json, clioResult);
  }

  /**
   * Helper method to construct an instance of {@link ServerInfo} with {@code completeLedgers} in
   * {@link ClioServerInfo#completeLedgers()}.
   *
   * @param completeLedgers A {@link String} with the value of completeLedgers.
   *
   * @return An instance of {@link org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo}.
   */
  protected static ServerInfo clioServerInfo(final String completeLedgers) {
    Objects.requireNonNull(completeLedgers);

    return ClioServerInfo.builder()
      .clioVersion("1.5.0-rc1")
      .rippledVersion("1.5.0-rc1")
      .completeLedgers(LedgerRangeUtils.completeLedgersToListOfRange(completeLedgers)) // <-- use completeLedgers here.
      .loadFactor(BigDecimal.ONE)
      .validatedLedger(ValidatedLedger.builder()
        .age(UnsignedInteger.valueOf(2))
        .hash(Hash256.of("0D2D30837E05995AAAAA117294BB45AB0699AB1219605FFD23318E050C7166E9"))
        .reserveBaseXrp(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(20)))
        .reserveIncXrp(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(5)))
        .sequence(LedgerIndex.of(UnsignedInteger.valueOf(54300729)))
        .baseFeeXrp(new BigDecimal("0.000010"))
        .build())
      .validationQuorum(UnsignedInteger.valueOf(29))
      .build();
  }

}
