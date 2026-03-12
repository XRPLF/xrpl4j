package org.xrpl.xrpl4j.model.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

class IssueTest extends AbstractJsonTest {

  @Test
  void testXrp() {
    assertThat(Issue.XRP).isInstanceOf(XrpIssue.class);
    XrpIssue xrp = (XrpIssue) Issue.XRP;
    assertThat(xrp.currency()).isEqualTo("XRP");
  }

  @Test
  void testIou() {
    String usd = "USD";
    Address issuer = Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn");
    IouIssue asset = IouIssue.builder()
      .currency(usd)
      .issuer(issuer)
      .build();

    assertThat(asset.currency()).isEqualTo(usd);
    assertThat(asset.issuer()).isEqualTo(issuer);
  }

  @Test
  void testMpt() {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000001A407AF5856CFF3379945D823561023E8E5CED9C9");
    MptIssue asset = MptIssue.builder()
      .mptIssuanceId(mptId)
      .build();

    assertThat(asset.mptIssuanceId()).isEqualTo(mptId);
  }

  @Test
  void testJsonForXrp() throws JSONException, JsonProcessingException {
    String json = "{" +
      "    \"currency\": \"XRP\"" +
      "}";

    assertCanSerializeAndDeserialize(Issue.XRP, json, Issue.class);
  }

  @Test
  void testJsonForIou() throws JSONException, JsonProcessingException {
    String usd = "USD";
    Address issuer = Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn");
    IouIssue asset = IouIssue.builder()
      .currency(usd)
      .issuer(issuer)
      .build();
    String json = "{" +
      "    \"currency\": \"" + usd + "\"," +
      "    \"issuer\": \"" + issuer.value() + "\"" +
      "}";

    assertCanSerializeAndDeserialize(asset, json, Issue.class);
  }

  @Test
  void testJsonForMpt() throws JSONException, JsonProcessingException {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000001A407AF5856CFF3379945D823561023E8E5CED9C9");
    MptIssue asset = MptIssue.builder()
      .mptIssuanceId(mptId)
      .build();
    String json = "{" +
      "    \"mpt_issuance_id\": \"00000001A407AF5856CFF3379945D823561023E8E5CED9C9\"" +
      "}";

    assertCanSerializeAndDeserialize(asset, json, Issue.class);
  }
}
