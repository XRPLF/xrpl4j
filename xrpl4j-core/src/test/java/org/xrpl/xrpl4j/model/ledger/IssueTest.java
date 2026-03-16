package org.xrpl.xrpl4j.model.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

class IssueTest extends AbstractJsonTest {

  @Test
  void testXrp() {
    assertThat(((XrpIssue) Issue.XRP).currency()).isEqualTo("XRP");
  }

  @Test
  void testNonXrp() {
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
    IouIssue asset = IouIssue.builder()
      .currency(usd)
      .issuer(issuer)
      .build();
    String json = "{" +
      "    \"currency\": \"" + usd + "\"," +
      "    \"issuer\": \"" + asset.issuer().value() + "\"" +
      "}";

    assertCanSerializeAndDeserialize(asset, json, Issue.class);
  }

  @Test
  void testJsonForMpt() throws JSONException, JsonProcessingException {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    MptIssue mptIssue = MptIssue.of(mptId);

    String json = "{" +
      "    \"mpt_issuance_id\": \"00000002430427B80BD2D09D36B70B969E12801065F22308\"" +
      "}";

    assertCanSerializeAndDeserialize(mptIssue, json, Issue.class);
  }

  @Test
  void testDeserializeInvalidIssueThrowsException() {
    String invalidJson = "{" +
      "    \"invalid_field\": \"some_value\"" +
      "}";

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> objectMapper.readValue(invalidJson, Issue.class)
    );

    assertThat(exception.getMessage()).isEqualTo(
      "Invalid Issue JSON: must have either 'mpt_issuance_id' or 'currency' field"
    );
  }

  @Test
  void testDeserializeEmptyJsonThrowsException() {
    String emptyJson = "{}";

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> objectMapper.readValue(emptyJson, Issue.class)
    );

    assertThat(exception.getMessage()).isEqualTo(
      "Invalid Issue JSON: must have either 'mpt_issuance_id' or 'currency' field"
    );
  }

  @Test
  void testDeserializeIouWithoutIssuerThrowsException() {
    String iouWithoutIssuer = "{" +
      "    \"currency\": \"USD\"" +
      "}";

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> objectMapper.readValue(iouWithoutIssuer, Issue.class)
    );

    assertThat(exception.getMessage()).isEqualTo(
      "Invalid Issue JSON: IOU currency 'USD' must have an 'issuer' field"
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
  void testMapWithXrp() {
    String result = Issue.XRP.map(
      xi -> "XRP",
      ii -> "IOU",
      mi -> "MPT"
    );

    assertThat(result).isEqualTo("XRP");
  }

  @Test
  void testMapWithNullIouIssueMapper() {
    IouIssue iouIssue = IouIssue.builder()
      .currency("USD")
      .issuer(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
      .build();

    NullPointerException exception = assertThrows(
      NullPointerException.class,
      () -> iouIssue.map(xi -> "XRP", null, mi -> "MPT")
    );
  }

  @Test
  void testMapWithNullMptIssueMapper() {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    MptIssue mptIssue = MptIssue.of(mptId);

    NullPointerException exception = assertThrows(
      NullPointerException.class,
      () -> mptIssue.map(xi -> "XRP", ii -> "IOU", null)
    );
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
  void testHandleWithXrp() {
    final StringBuilder result = new StringBuilder();
    Issue.XRP.handle(
      xi -> result.append("XRP"),
      ii -> result.append("IOU"),
      mi -> result.append("MPT")
    );

    assertThat(result.toString()).isEqualTo("XRP");
  }

  @Test
  void testHandleWithNullIouIssueHandler() {
    IouIssue iouIssue = IouIssue.builder()
      .currency("USD")
      .issuer(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
      .build();

    NullPointerException exception = assertThrows(
      NullPointerException.class,
      () -> iouIssue.handle(xi -> { }, null, mi -> { })
    );
  }

  @Test
  void testHandleWithNullMptIssueHandler() {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    MptIssue mptIssue = MptIssue.of(mptId);

    NullPointerException exception = assertThrows(
      NullPointerException.class,
      () -> mptIssue.handle(xi -> { }, ii -> { }, null)
    );
  }

  @Test
  void testMapWithUnsupportedIssueType() {
    Issue unsupportedIssue = new Issue() {
      // Anonymous implementation that is neither XrpIssue, IouIssue, nor MptIssue
    };

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> unsupportedIssue.map(xi -> "XRP", ii -> "IOU", mi -> "MPT")
    );

    assertThat(exception.getMessage()).contains("Unsupported Issue Type");
    assertThat(exception.getMessage()).contains(unsupportedIssue.getClass().getName());
  }

  @Test
  void testHandleWithUnsupportedIssueType() {
    Issue unsupportedIssue = new Issue() {
      // Anonymous implementation that is neither XrpIssue, IouIssue, nor MptIssue
    };

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> unsupportedIssue.handle(xi -> { }, ii -> { }, mi -> { })
    );

    assertThat(exception.getMessage()).contains("Unsupported Issue Type");
    assertThat(exception.getMessage()).contains(unsupportedIssue.getClass().getName());
  }
}