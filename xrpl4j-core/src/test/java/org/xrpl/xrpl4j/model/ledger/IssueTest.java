package org.xrpl.xrpl4j.model.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

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

  @Test
  void handleXrpIssue() {
    Issue xrp = Issue.XRP;
    xrp.handle(
      $ -> assertThat($.currency()).isEqualTo("XRP"),
      $ -> fail(),
      $ -> fail()
    );
  }

  @Test
  void handleIouIssue() {
    Issue iou = IouIssue.builder()
      .currency("USD")
      .issuer(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
      .build();
    iou.handle(
      $ -> fail(),
      $ -> assertThat($.currency()).isEqualTo("USD"),
      $ -> fail()
    );
  }

  @Test
  void handleMptIssue() {
    Issue mpt = MptIssue.builder()
      .mptIssuanceId(MpTokenIssuanceId.of("00000001A407AF5856CFF3379945D823561023E8E5CED9C9"))
      .build();
    mpt.handle(
      $ -> fail(),
      $ -> fail(),
      $ -> assertThat($.mptIssuanceId()).isEqualTo(
        MpTokenIssuanceId.of("00000001A407AF5856CFF3379945D823561023E8E5CED9C9")
      )
    );
  }

  @Test
  void handleWithNulls() {
    Issue xrp = Issue.XRP;
    assertThrows(NullPointerException.class, () ->
      xrp.handle(null, $ -> { }, $ -> { })
    );
    assertThrows(NullPointerException.class, () ->
      xrp.handle($ -> { }, null, $ -> { })
    );
    assertThrows(NullPointerException.class, () ->
      xrp.handle($ -> { }, $ -> { }, null)
    );
  }

  @Test
  void mapXrpIssue() {
    Issue xrp = Issue.XRP;
    String result = xrp.map(
      $ -> "xrp",
      $ -> "iou",
      $ -> "mpt"
    );
    assertThat(result).isEqualTo("xrp");
  }

  @Test
  void mapIouIssue() {
    Issue iou = IouIssue.builder()
      .currency("USD")
      .issuer(Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"))
      .build();
    String result = iou.map(
      $ -> "xrp",
      $ -> "iou",
      $ -> "mpt"
    );
    assertThat(result).isEqualTo("iou");
  }

  @Test
  void mapMptIssue() {
    Issue mpt = MptIssue.builder()
      .mptIssuanceId(MpTokenIssuanceId.of("00000001A407AF5856CFF3379945D823561023E8E5CED9C9"))
      .build();
    String result = mpt.map(
      $ -> "xrp",
      $ -> "iou",
      $ -> "mpt"
    );
    assertThat(result).isEqualTo("mpt");
  }

  @Test
  void handleUnsupportedIssueType() {
    Issue unsupported = new Issue() {};
    assertThrows(IllegalStateException.class, () ->
      unsupported.handle($ -> { }, $ -> { }, $ -> { })
    );
  }

  @Test
  void mapUnsupportedIssueType() {
    Issue unsupported = new Issue() {};
    assertThrows(IllegalStateException.class, () ->
      unsupported.map($ -> "xrp", $ -> "iou", $ -> "mpt")
    );
  }

  @Test
  void mapWithNulls() {
    Issue xrp = Issue.XRP;
    assertThrows(NullPointerException.class, () ->
      xrp.map(null, $ -> "iou", $ -> "mpt")
    );
    assertThrows(NullPointerException.class, () ->
      xrp.map($ -> "xrp", null, $ -> "mpt")
    );
    assertThrows(NullPointerException.class, () ->
      xrp.map($ -> "xrp", $ -> "iou", null)
    );
  }

  @Test
  void testMapWithIouIssue() {
    IouIssue iouIssue = IouIssue.builder()
      .currency("USD")
      .issuer(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
      .build();

    String result = iouIssue.map(
      xi -> "XrpIssue",
      ii -> "IouIssue: " + ii.currency(),
      mi -> "MptIssue: " + mi.mptIssuanceId()
    );

    assertThat(result).isEqualTo("IouIssue: USD");
  }

  @Test
  void testMapWithMptIssue() {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    MptIssue mptIssue = MptIssue.of(mptId);

    String result = mptIssue.map(
      xi -> "XrpIssue",
      ii -> "IouIssue: " + ii.currency(),
      mi -> "MptIssue: " + mi.mptIssuanceId()
    );

    assertThat(result).isEqualTo("MptIssue: 00000002430427B80BD2D09D36B70B969E12801065F22308");
  }

  @Test
  void testHandleWithIouIssue() {
    IouIssue iouIssue = IouIssue.builder()
      .currency("EUR")
      .issuer(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
      .build();

    final StringBuilder result = new StringBuilder();
    iouIssue.handle(
      xi -> result.append("XRP"),
      ii -> result.append("IOU: ").append(ii.currency()),
      mi -> result.append("MPT: ").append(mi.mptIssuanceId())
    );

    assertThat(result.toString()).isEqualTo("IOU: EUR");
  }

  @Test
  void testHandleWithMptIssue() {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    MptIssue mptIssue = MptIssue.of(mptId);

    final StringBuilder result = new StringBuilder();
    mptIssue.handle(
      xi -> result.append("XRP"),
      ii -> result.append("IOU: ").append(ii.currency()),
      mi -> result.append("MPT: ").append(mi.mptIssuanceId())
    );

    assertThat(result.toString()).isEqualTo("MPT: 00000002430427B80BD2D09D36B70B969E12801065F22308");
  }

  @Test
  void testDeserializeInvalidIssueThrowsException() {
    String invalidJson = "{" +
      "    \"invalid_field\": \"some_value\"" +
      "}";

    assertThrows(
      Exception.class,
      () -> objectMapper.readValue(invalidJson, Issue.class)
    );
  }

  @Test
  void testDeserializeEmptyJsonThrowsException() {
    String emptyJson = "{}";

    assertThrows(
      Exception.class,
      () -> objectMapper.readValue(emptyJson, Issue.class)
    );
  }
}
