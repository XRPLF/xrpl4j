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
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.path.PathCurrency;
import org.xrpl.xrpl4j.model.ledger.CurrencyIssue;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.ledger.XrpIssue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.io.IOException;

/**
 * Unit tests for {@link PathCurrencyDeserializer}.
 */
class PathCurrencyDeserializerTest {

  private PathCurrencyDeserializer deserializer;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    deserializer = new PathCurrencyDeserializer();
    objectMapper = new ObjectMapper();
  }

  @Test
  void testDeserializeXrp() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("currency", "XRP");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    PathCurrency result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    assertThat(result.issue()).isInstanceOf(XrpIssue.class);
    XrpIssue xrpIssue = (XrpIssue) result.issue();
    assertThat(xrpIssue.currency()).isEqualTo("XRP");
  }

  @Test
  void testDeserializeIouWithIssuer() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("currency", "USD");
    node.put("issuer", "rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    PathCurrency result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    assertThat(result.issue()).isInstanceOf(IouIssue.class);
    IouIssue iouIssue = (IouIssue) result.issue();
    assertThat(iouIssue.currency()).isEqualTo("USD");
    assertThat(iouIssue.issuer()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"));
  }

  @Test
  void testDeserializeIouWithoutIssuer() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("currency", "EUR");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    PathCurrency result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    assertThat(result.issue()).isInstanceOf(CurrencyIssue.class);
    CurrencyIssue currencyIssue = (CurrencyIssue) result.issue();
    assertThat(currencyIssue.currency()).isEqualTo("EUR");
    assertThat(currencyIssue.issuer()).isEmpty();
  }

  @Test
  void testDeserializeMpt() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("mpt_issuance_id", "00000002430427B80BD2D09D36B70B969E12801065F22308");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    PathCurrency result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    assertThat(result.issue()).isInstanceOf(MptIssue.class);
    MptIssue mptIssue = (MptIssue) result.issue();
    assertThat(mptIssue.mptIssuanceId())
      .isEqualTo(MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308"));
  }

  @Test
  void testDeserializeHexCurrencyCode() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("currency", "015841551A748AD2C1F76FF6ECB0CCCD00000000");
    node.put("issuer", "rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    PathCurrency result = deserializer.deserialize(parser, mock(DeserializationContext.class));

    assertThat(result.issue()).isInstanceOf(IouIssue.class);
    IouIssue iouIssue = (IouIssue) result.issue();
    assertThat(iouIssue.currency()).isEqualTo("015841551A748AD2C1F76FF6ECB0CCCD00000000");
    assertThat(iouIssue.issuer()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"));
  }

  @Test
  void testDeserializeEmptyJsonThrowsException() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> deserializer.deserialize(parser, mock(DeserializationContext.class))
    );

    assertThat(exception.getMessage())
      .isEqualTo("PathCurrency JSON must have either 'mpt_issuance_id' or 'currency' field");
  }

  @Test
  void testDeserializeInvalidFieldThrowsException() throws IOException {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put("invalid_field", "some_value");

    JsonParser parser = objectMapper.treeAsTokens(node);
    parser.nextToken();

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> deserializer.deserialize(parser, mock(DeserializationContext.class))
    );

    assertThat(exception.getMessage())
      .isEqualTo("PathCurrency JSON must have either 'mpt_issuance_id' or 'currency' field");
  }
}

