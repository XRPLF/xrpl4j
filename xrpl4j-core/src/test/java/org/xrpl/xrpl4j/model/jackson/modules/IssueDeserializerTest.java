package org.xrpl.xrpl4j.model.jackson.modules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.ledger.XrpIssue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

class IssueDeserializerTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void deserializeXrpIssue() throws JsonProcessingException {
    String json = "{\"currency\":\"XRP\"}";
    Issue issue = objectMapper.readValue(json, Issue.class);

    assertThat(issue).isInstanceOf(XrpIssue.class);
    assertThat(issue).isEqualTo(XrpIssue.XRP);
  }

  @Test
  void deserializeIouIssue() throws JsonProcessingException {
    String json = "{\"currency\":\"USD\",\"issuer\":\"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"}";
    Issue issue = objectMapper.readValue(json, Issue.class);

    assertThat(issue).isInstanceOf(IouIssue.class);
    IouIssue iouIssue = (IouIssue) issue;
    assertThat(iouIssue.currency()).isEqualTo("USD");
    assertThat(iouIssue.issuer()).isEqualTo(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"));
  }

  @Test
  void deserializeIouIssueWithHexCurrency() throws JsonProcessingException {
    String json = "{\"currency\":\"7872706C346A436F696E00000000000000000000\"," +
      "\"issuer\":\"rDeo7rDoYw6AUKGneWwfkHPsMJagxcGWy1\"}";
    Issue issue = objectMapper.readValue(json, Issue.class);

    assertThat(issue).isInstanceOf(IouIssue.class);
    IouIssue iouIssue = (IouIssue) issue;
    assertThat(iouIssue.currency()).isEqualTo("7872706C346A436F696E00000000000000000000");
    assertThat(iouIssue.issuer()).isEqualTo(Address.of("rDeo7rDoYw6AUKGneWwfkHPsMJagxcGWy1"));
  }

  @Test
  void deserializeMptIssue() throws JsonProcessingException {
    String json = "{\"mpt_issuance_id\":\"00000005E54ZDVGNGHAOPOPCGVTIQWNQ3DU5Y836\"}";
    Issue issue = objectMapper.readValue(json, Issue.class);

    assertThat(issue).isInstanceOf(MptIssue.class);
    MptIssue mptIssue = (MptIssue) issue;
    assertThat(mptIssue.mptIssuanceId()).isEqualTo(
      MpTokenIssuanceId.of("00000005E54ZDVGNGHAOPOPCGVTIQWNQ3DU5Y836")
    );
  }

  @Test
  void deserializeInvalidIssueThrows() {
    String json = "{\"unknown_field\":\"value\"}";
    assertThatThrownBy(() -> objectMapper.readValue(json, Issue.class))
      .isInstanceOf(JsonProcessingException.class)
      .hasMessageContaining("Cannot deserialize Issue");
  }

  @Test
  void deserializeEmptyObjectThrows() {
    String json = "{}";
    assertThatThrownBy(() -> objectMapper.readValue(json, Issue.class))
      .isInstanceOf(JsonProcessingException.class)
      .hasMessageContaining("Cannot deserialize Issue");
  }

}
