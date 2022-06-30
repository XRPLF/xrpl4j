package org.xrpl.xrpl4j.model.client.serverinfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

public class ClioServerInfoTest extends AbstractJsonTest {

  @Test
  public void testClioServerInfoJson() throws JsonProcessingException, JSONException {
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

    assertCanSerializeAndDeserialize(clioResult, json);
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
      .completeLedgers(completeLedgers) // <-- use completeLedgers here.
      .loadFactor(BigDecimal.ONE)
      .validatedLedger(ServerInfoLedger.builder()
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
