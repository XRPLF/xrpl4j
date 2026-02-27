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
    assertThat(((CurrencyIssue) Issue.XRP).currency()).isEqualTo("XRP");
    assertThat(((CurrencyIssue) Issue.XRP).issuer()).isEmpty();
  }

  @Test
  void testNonXrp() {
    String usd = "USD";
    Address issuer = Address.of("rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn");
    CurrencyIssue asset = CurrencyIssue.builder()
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
    CurrencyIssue asset = CurrencyIssue.builder()
      .currency(usd)
      .issuer(issuer)
      .build();
    String json = "{" +
      "    \"currency\": \"" + usd + "\"," +
      "    \"issuer\": \"" + asset.issuer().get().value() + "\"" +
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
  void testMapWithCurrencyIssue() {
    CurrencyIssue currencyIssue = CurrencyIssue.builder()
      .currency("USD")
      .issuer(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
      .build();

    String result = currencyIssue.map(
      ci -> "CurrencyIssue: " + ci.currency(),
      mi -> "MptIssue: " + mi.mptIssuanceId()
    );

    assertThat(result).isEqualTo("CurrencyIssue: USD");
  }

  @Test
  void testMapWithMptIssue() {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    MptIssue mptIssue = MptIssue.of(mptId);

    String result = mptIssue.map(
      ci -> "CurrencyIssue: " + ci.currency(),
      mi -> "MptIssue: " + mi.mptIssuanceId()
    );

    assertThat(result).isEqualTo("MptIssue: 00000002430427B80BD2D09D36B70B969E12801065F22308");
  }

  @Test
  void testMapWithXrp() {
    String result = Issue.XRP.map(
      ci -> "XRP",
      mi -> "MPT"
    );

    assertThat(result).isEqualTo("XRP");
  }

  @Test
  void testMapWithNullCurrencyIssueMapper() {
    CurrencyIssue currencyIssue = CurrencyIssue.builder()
      .currency("USD")
      .issuer(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
      .build();

    NullPointerException exception = assertThrows(
      NullPointerException.class,
      () -> currencyIssue.map(null, mi -> "MPT")
    );
  }

  @Test
  void testMapWithNullMptIssueMapper() {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    MptIssue mptIssue = MptIssue.of(mptId);

    NullPointerException exception = assertThrows(
      NullPointerException.class,
      () -> mptIssue.map(ci -> "Currency", null)
    );
  }

  @Test
  void testHandleWithCurrencyIssue() {
    CurrencyIssue currencyIssue = CurrencyIssue.builder()
      .currency("EUR")
      .issuer(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
      .build();

    final StringBuilder result = new StringBuilder();
    currencyIssue.handle(
      ci -> result.append("Currency: ").append(ci.currency()),
      mi -> result.append("MPT: ").append(mi.mptIssuanceId())
    );

    assertThat(result.toString()).isEqualTo("Currency: EUR");
  }

  @Test
  void testHandleWithMptIssue() {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    MptIssue mptIssue = MptIssue.of(mptId);

    final StringBuilder result = new StringBuilder();
    mptIssue.handle(
      ci -> result.append("Currency: ").append(ci.currency()),
      mi -> result.append("MPT: ").append(mi.mptIssuanceId())
    );

    assertThat(result.toString()).isEqualTo("MPT: 00000002430427B80BD2D09D36B70B969E12801065F22308");
  }

  @Test
  void testHandleWithXrp() {
    final StringBuilder result = new StringBuilder();
    Issue.XRP.handle(
      ci -> result.append("XRP"),
      mi -> result.append("MPT")
    );

    assertThat(result.toString()).isEqualTo("XRP");
  }

  @Test
  void testHandleWithNullCurrencyIssueHandler() {
    CurrencyIssue currencyIssue = CurrencyIssue.builder()
      .currency("USD")
      .issuer(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
      .build();

    NullPointerException exception = assertThrows(
      NullPointerException.class,
      () -> currencyIssue.handle(null, mi -> {})
    );
  }

  @Test
  void testHandleWithNullMptIssueHandler() {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    MptIssue mptIssue = MptIssue.of(mptId);

    NullPointerException exception = assertThrows(
      NullPointerException.class,
      () -> mptIssue.handle(ci -> {}, null)
    );
  }

  @Test
  void testMapWithUnsupportedIssueType() {
    Issue unsupportedIssue = new Issue() {
      // Anonymous implementation that is neither CurrencyIssue nor MptIssue
    };

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> unsupportedIssue.map(ci -> "Currency", mi -> "MPT")
    );

    assertThat(exception.getMessage()).contains("Unsupported Issue Type");
    assertThat(exception.getMessage()).contains(unsupportedIssue.getClass().getName());
  }

  @Test
  void testHandleWithUnsupportedIssueType() {
    Issue unsupportedIssue = new Issue() {
      // Anonymous implementation that is neither CurrencyIssue nor MptIssue
    };

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> unsupportedIssue.handle(ci -> {}, mi -> {})
    );

    assertThat(exception.getMessage()).contains("Unsupported Issue Type");
    assertThat(exception.getMessage()).contains(unsupportedIssue.getClass().getName());
  }
}