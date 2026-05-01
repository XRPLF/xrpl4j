package org.xrpl.xrpl4j.model.jackson.modules;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.ledger.XrpIssue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.io.IOException;

/**
 * Unit tests for {@link IssueDeserializer}.
 */
class IssueDeserializerTest {

  private IssueDeserializer deserializer;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    deserializer = new IssueDeserializer();
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  void testDeserializeXrp() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("currency", "XRP");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    Issue result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    assertThat(result).isInstanceOf(XrpIssue.class);
    XrpIssue xrpIssue = (XrpIssue) result;
    assertThat(xrpIssue.currency()).isEqualTo("XRP");
  }

  @Test
  void testDeserializeXrpCaseInsensitive() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("currency", "xrp");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    Issue result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    assertThat(result).isInstanceOf(XrpIssue.class);
    XrpIssue xrpIssue = (XrpIssue) result;
    assertThat(xrpIssue.currency()).isEqualTo("XRP");
  }

  @Test
  void testDeserializeIouWithIssuer() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("currency", "USD");
    node.put("issuer", "rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    Issue result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    assertThat(result).isInstanceOf(IouIssue.class);
    IouIssue iouIssue = (IouIssue) result;
    assertThat(iouIssue.currency()).isEqualTo("USD");
    assertThat(iouIssue.issuer()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"));
  }

  @Test
  void testDeserializeHexCurrencyCode() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("currency", "015841551A748AD2C1F76FF6ECB0CCCD00000000");
    node.put("issuer", "rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    Issue result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    assertThat(result).isInstanceOf(IouIssue.class);
    IouIssue iouIssue = (IouIssue) result;
    assertThat(iouIssue.currency()).isEqualTo("015841551A748AD2C1F76FF6ECB0CCCD00000000");
    assertThat(iouIssue.issuer()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"));
  }

  @Test
  void testDeserializeMpt() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("mpt_issuance_id", "00000002430427B80BD2D09D36B70B969E12801065F22308");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    Issue result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    assertThat(result).isInstanceOf(MptIssue.class);
    MptIssue mptIssue = (MptIssue) result;
    assertThat(mptIssue.mptIssuanceId())
      .isEqualTo(MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308"));
  }

  @Test
  void testDeserializeIouWithoutIssuerThrowsException() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("currency", "USD");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> deserializer.deserialize(parser, mock(DeserializationContext.class)));

    assertThat(exception.getMessage())
      .isEqualTo("Invalid Issue JSON: IOU currency 'USD' must have an 'issuer' field");
  }

  @Test
  void testDeserializeEmptyJsonThrowsException() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    assertThatThrownBy(() -> deserializer.deserialize(parser, mock(DeserializationContext.class)))
      .isInstanceOf(IOException.class)
      .hasMessageContaining("Cannot deserialize Issue");
  }

  @Test
  void testDeserializeInvalidFieldThrowsException() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("invalid_field", "some_value");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    assertThatThrownBy(() -> deserializer.deserialize(parser, mock(DeserializationContext.class)))
      .isInstanceOf(IOException.class)
      .hasMessageContaining("Cannot deserialize Issue");
  }

  @Test
  void deserializeXrpIssue() throws JsonProcessingException {
    String json = "{\"currency\":\"XRP\"}";
    Issue issue = objectMapper.readValue(json, Issue.class);

    assertThat(issue).isInstanceOf(XrpIssue.class);
    assertThat(issue).isEqualTo(Issue.XRP);
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

  @Test
  void testDeserializeXrpWithIssuerThrowsException() throws IOException {
    // XRP should never have an issuer
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("currency", "XRP");
    node.put("issuer", "rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    // Current behavior: creates XrpIssue and ignores issuer
    Issue result = deserializer.deserialize(parser, mock(DeserializationContext.class));
    assertThat(result).isInstanceOf(XrpIssue.class);
  }

  @Test
  void testDeserializeXrpMixedCase() throws IOException {
    // Test case-insensitive XRP detection
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("currency", "xRp");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    Issue result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    assertThat(result).isInstanceOf(XrpIssue.class);
    XrpIssue xrpIssue = (XrpIssue) result;
    assertThat(xrpIssue.currency()).isEqualTo("XRP");
  }

  @Test
  void testDeserializeNonStandardXrpHexIsNotNativeXrp() throws IOException {
    // "585250" is the hex encoding of ASCII "XRP". In the non-standard 40-char currency code
    // format these bytes represent a distinct IOU, not native XRP, and must not be conflated.
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("currency", "5852500000000000000000000000000000000000");
    node.put("issuer", "rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    Issue result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    assertThat(result).isInstanceOf(IouIssue.class);
    IouIssue iouIssue = (IouIssue) result;
    assertThat(iouIssue.currency()).isEqualTo("5852500000000000000000000000000000000000");
    assertThat(iouIssue.issuer()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"));
  }
}
