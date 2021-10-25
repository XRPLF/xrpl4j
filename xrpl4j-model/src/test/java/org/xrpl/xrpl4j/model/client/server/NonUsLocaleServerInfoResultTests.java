package org.xrpl.xrpl4j.model.client.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.util.Locale;

/**
 * These tests set the default {@link Locale} to "de_DE" to ensure that {@link ServerInfo#time()} can be deserialized
 * when the default {@link Locale} is not {@link Locale#US}.
 *
 * <p>These tests must be separate from {@link ServerInfoResultTests} so that the {@link ObjectMapper} that is
 * statically created uses the non "US" Locale.</p>
 */
public class NonUsLocaleServerInfoResultTests {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    Locale.setDefault(new Locale("de", "DE"));
    objectMapper = ObjectMapperFactory.create();
  }

  /**
   * This test deserializes a sample JSON blob containing a time formatted in the "en_US" locale, when the
   * JVM default locale is set to "de_DE". Before explicitly adding the locale to {@link ServerInfo#time()},
   * this would have failed (which was reported in issue 156).
   *
   * @see "https://github.com/XRPLF/xrpl4j/issues/156"
   */
  @Test
  void deserializeResultWithNonEnUsLocaleSet() {
    String json = "{\n" +
      "  \"info\" : {\n" +
      "    \"amendment_blocked\" : false,\n" +
      "    \"build_version\" : \"1.5.0-rc1\",\n" +
      "    \"complete_ledgers\" : \"54300020-54300729\",\n" +
      "    \"hostid\" : \"trace\",\n" +
      "    \"io_latency_ms\" : 1,\n" +
      "    \"jq_trans_overflow\" : \"0\",\n" +
      "    \"last_close\" : {\n" +
      "      \"converge_time_s\" : 2.0,\n" +
      "      \"proposers\" : 34\n" +
      "    },\n" +
      "    \"load\" : {\n" +
      "      \"job_types\" : [ {\n" +
      "        \"job_type\" : \"ledgerRequest\",\n" +
      "        \"peak_time\" : 4,\n" +
      "        \"per_second\" : 4\n" +
      "      }, {\n" +
      "        \"job_type\" : \"untrustedProposal\",\n" +
      "        \"peak_time\" : 5,\n" +
      "        \"per_second\" : 43\n" +
      "      }, {\n" +
      "        \"job_type\" : \"ledgerData\",\n" +
      "        \"peak_time\" : 337,\n" +
      "        \"avg_time\" : 14\n" +
      "      }, {\n" +
      "        \"job_type\" : \"clientCommand\",\n" +
      "        \"in_progress\" : 1,\n" +
      "        \"per_second\" : 9\n" +
      "      }, {\n" +
      "        \"job_type\" : \"transaction\",\n" +
      "        \"peak_time\" : 8,\n" +
      "        \"per_second\" : 8\n" +
      "      }, {\n" +
      "        \"job_type\" : \"batch\",\n" +
      "        \"peak_time\" : 5,\n" +
      "        \"per_second\" : 6\n" +
      "      }, {\n" +
      "        \"job_type\" : \"advanceLedger\",\n" +
      "        \"peak_time\" : 96,\n" +
      "        \"avg_time\" : 6\n" +
      "      }, {\n" +
      "        \"job_type\" : \"fetchTxnData\",\n" +
      "        \"per_second\" : 14\n" +
      "      } ],\n" +
      "      \"threads\" : 6\n" +
      "    },\n" +
      "    \"load_factor\" : 1,\n" +
      "    \"peers\" : 21,\n" +
      "    \"pubkey_node\" : \"n9KUjqxCr5FKThSNXdzb7oqN8rYwScB2dUnNqxQxbEA17JkaWy5x\",\n" +
      "    \"pubkey_validator\" : \"nHBk5DPexBjinXV8qHn7SEKzoxh2W92FxSbNTPgGtQYBzEF4msn9\",\n" +
      "    \"server_state\" : \"proposing\",\n" +
      "    \"server_state_duration_us\" : \"1850969666\",\n" +
      "    \"time\" : \"2021-Sep-27 11:43:47.464662 UTC\",\n" +
      "    \"uptime\" : 1984,\n" +
      "    \"validated_ledger\" : {\n" +
      "      \"age\" : 2,\n" +
      "      \"hash\" : \"0D2D30837E05995AAAAA117294BB45AB0699AB1219605FFD23318E050C7166E9\",\n" +
      "      \"reserve_base_xrp\" : 20,\n" +
      "      \"reserve_inc_xrp\" : 5,\n" +
      "      \"seq\" : 54300729,\n" +
      "      \"base_fee_xrp\" : 0.00001\n" +
      "    },\n" +
      "    \"validation_quorum\" : 29\n" +
      "  }\n" +
      "}";

    this.objectMapper = ObjectMapperFactory.create();
    assertDoesNotThrow(() -> objectMapper.readValue(json, ServerInfoResult.class));
  }
}
