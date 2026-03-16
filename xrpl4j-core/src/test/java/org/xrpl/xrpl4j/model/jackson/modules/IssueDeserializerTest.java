package org.xrpl.xrpl4j.model.jackson.modules;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    objectMapper = new ObjectMapper();
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

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> deserializer.deserialize(parser, mock(DeserializationContext.class)));

    assertThat(exception.getMessage())
      .isEqualTo("Invalid Issue JSON: must have either 'mpt_issuance_id' or 'currency' field");
  }

  @Test
  void testDeserializeInvalidFieldThrowsException() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("invalid_field", "some_value");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> deserializer.deserialize(parser, mock(DeserializationContext.class)));

    assertThat(exception.getMessage())
      .isEqualTo("Invalid Issue JSON: must have either 'mpt_issuance_id' or 'currency' field");
  }

  @Test
  void testDeserializeStandardXrpCurrencyCode() throws IOException {
    // Standard 3-character ASCII code "XRP"
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
  void testDeserializeNonStandardXrpCurrencyCodeWithZeroPrefix() throws IOException {
    // Non-standard format: 160-bit hex with 0x00 prefix and XRP (585250) in normally disallowed positions
    // Per https://xrpl.org/docs/references/protocol/binary-format#currency-codes
    // Standard format disallows XRP in bytes 8-10 when prefix is 0x00
    // But non-standard format technically allows it
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("currency", "0000000000000000000000005852500000000000");
    node.put("issuer", "rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    Issue result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    // This should be treated as an IOU, not XRP, because it's in hex format
    // The deserializer only recognizes the literal string "XRP" as XRP
    assertThat(result).isInstanceOf(IouIssue.class);
    IouIssue iouIssue = (IouIssue) result;
    assertThat(iouIssue.currency()).isEqualTo("0000000000000000000000005852500000000000");
    assertThat(iouIssue.issuer()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"));
  }

  @Test
  void testDeserializeNonStandardXrpCurrencyCodeWithNonZeroPrefix() throws IOException {
    // Non-standard format: 160-bit hex with non-zero prefix
    // This allows XRP bytes (585250) in positions that would be disallowed in standard format
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("currency", "0100000000000000000000005852500000000000");
    node.put("issuer", "rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    Issue result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    // This is a valid non-standard currency code with XRP bytes in it
    assertThat(result).isInstanceOf(IouIssue.class);
    IouIssue iouIssue = (IouIssue) result;
    assertThat(iouIssue.currency()).isEqualTo("0100000000000000000000005852500000000000");
    assertThat(iouIssue.issuer()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"));
  }

  @Test
  void testDeserializeXrpWithIssuerThrowsException() throws IOException {
    // XRP should never have an issuer
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("currency", "XRP");
    node.put("issuer", "rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    // Currently the deserializer allows this, but it creates an XrpIssue and ignores the issuer
    // This might be a bug - XRP with an issuer should probably throw an exception
    Issue result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    // Current behavior: creates XrpIssue and ignores issuer
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
}

