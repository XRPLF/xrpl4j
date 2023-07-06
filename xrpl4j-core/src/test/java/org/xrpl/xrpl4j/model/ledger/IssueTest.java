package org.xrpl.xrpl4j.model.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;

class IssueTest extends AbstractJsonTest {

  @Test
  void testXrp() {
    assertThat(Issue.XRP.currency()).isEqualTo("XRP");
    assertThat(Issue.XRP.issuer()).isEmpty();
  }

  @Test
  void testNonXrp() {
    String usd = "USD";
    Address issuer = Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn");
    Issue asset = Issue.builder()
      .currency(usd)
      .issuer(issuer)
      .build();

    assertThat(asset.currency()).isEqualTo(usd);
    assertThat(asset.issuer()).isNotEmpty().get().isEqualTo(issuer);
  }

  @Test
  void testJsonForXrp() throws JSONException, JsonProcessingException {
    String json = "{" +
      "    \"currency\": \"XRP\"" +
      "}";

    assertCanSerializeAndDeserialize(Issue.XRP, json, Issue.class);
  }

  @Test
  void testJsonForNonXrp() throws JSONException, JsonProcessingException {
    String usd = "USD";
    Address issuer = Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn");
    Issue asset = Issue.builder()
      .currency(usd)
      .issuer(issuer)
      .build();
    String json = "{" +
      "    \"currency\": \"" + usd + "\"," +
      "    \"issuer\": \"" + asset.issuer().get().value() + "\"" +
      "}";

    assertCanSerializeAndDeserialize(asset, json, Issue.class);
  }
}