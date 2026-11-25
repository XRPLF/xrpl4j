package org.xrpl.xrpl4j.codec.binary.types;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: binary-codec
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;

/**
 * Unit tests for {@link Hop}.
 */
class HopTest {

  private final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();

  @Test
  void testEmptyHop() {
    Hop hop = Hop.builder().build();
    
    assertThat(hop.account()).isEmpty();
    assertThat(hop.currency()).isEmpty();
    assertThat(hop.issuer()).isEmpty();
    assertThat(hop.mptIssuanceId()).isEmpty();
  }

  @Test
  void testHopWithAccount() {
    JsonNode accountNode = new TextNode("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    Hop hop = Hop.builder()
      .account(accountNode)
      .build();
    
    assertThat(hop.account()).isPresent();
    assertThat(hop.account().get()).isEqualTo(accountNode);
    assertThat(hop.currency()).isEmpty();
    assertThat(hop.issuer()).isEmpty();
    assertThat(hop.mptIssuanceId()).isEmpty();
  }

  @Test
  void testHopWithCurrency() {
    JsonNode currencyNode = new TextNode("USD");
    Hop hop = Hop.builder()
      .currency(currencyNode)
      .build();
    
    assertThat(hop.currency()).isPresent();
    assertThat(hop.currency().get()).isEqualTo(currencyNode);
    assertThat(hop.account()).isEmpty();
    assertThat(hop.issuer()).isEmpty();
    assertThat(hop.mptIssuanceId()).isEmpty();
  }

  @Test
  void testHopWithIssuer() {
    JsonNode issuerNode = new TextNode("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    Hop hop = Hop.builder()
      .issuer(issuerNode)
      .build();
    
    assertThat(hop.issuer()).isPresent();
    assertThat(hop.issuer().get()).isEqualTo(issuerNode);
    assertThat(hop.account()).isEmpty();
    assertThat(hop.currency()).isEmpty();
    assertThat(hop.mptIssuanceId()).isEmpty();
  }

  @Test
  void testHopWithMptIssuanceId() {
    JsonNode mptIdNode = new TextNode("00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8");
    Hop hop = Hop.builder()
      .mptIssuanceId(mptIdNode)
      .build();
    
    assertThat(hop.mptIssuanceId()).isPresent();
    assertThat(hop.mptIssuanceId().get()).isEqualTo(mptIdNode);
    assertThat(hop.account()).isEmpty();
    assertThat(hop.currency()).isEmpty();
    assertThat(hop.issuer()).isEmpty();
  }

  @Test
  void testHopWithCurrencyAndIssuer() {
    JsonNode currencyNode = new TextNode("USD");
    JsonNode issuerNode = new TextNode("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    
    Hop hop = Hop.builder()
      .currency(currencyNode)
      .issuer(issuerNode)
      .build();
    
    assertThat(hop.currency()).isPresent();
    assertThat(hop.currency().get()).isEqualTo(currencyNode);
    assertThat(hop.issuer()).isPresent();
    assertThat(hop.issuer().get()).isEqualTo(issuerNode);
    assertThat(hop.account()).isEmpty();
    assertThat(hop.mptIssuanceId()).isEmpty();
  }

  @Test
  void testHopWithAccountCurrencyAndIssuer() {
    JsonNode accountNode = new TextNode("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    JsonNode currencyNode = new TextNode("USD");
    JsonNode issuerNode = new TextNode("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY");
    
    Hop hop = Hop.builder()
      .account(accountNode)
      .currency(currencyNode)
      .issuer(issuerNode)
      .build();
    
    assertThat(hop.account()).isPresent();
    assertThat(hop.account().get()).isEqualTo(accountNode);
    assertThat(hop.currency()).isPresent();
    assertThat(hop.currency().get()).isEqualTo(currencyNode);
    assertThat(hop.issuer()).isPresent();
    assertThat(hop.issuer().get()).isEqualTo(issuerNode);
    assertThat(hop.mptIssuanceId()).isEmpty();
  }

  @Test
  void testJsonSerializationWithMptIssuanceId() throws JsonProcessingException {
    String mptId = "00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    Hop hop = Hop.builder()
      .mptIssuanceId(new TextNode(mptId))
      .build();

    String json = objectMapper.writeValueAsString(hop);
    assertThat(json).contains("mpt_issuance_id");
    assertThat(json).contains(mptId);
  }

  @Test
  void testJsonDeserializationWithMptIssuanceId() throws JsonProcessingException {
    String json = "{\"mpt_issuance_id\":\"00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8\"}";

    Hop hop = objectMapper.readValue(json, Hop.class);

    assertThat(hop.mptIssuanceId()).isPresent();
    assertThat(hop.mptIssuanceId().get().asText()).isEqualTo("00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8");
    assertThat(hop.account()).isEmpty();
    assertThat(hop.currency()).isEmpty();
    assertThat(hop.issuer()).isEmpty();
  }

  @Test
  void testJsonSerializationWithCurrencyAndIssuer() throws JsonProcessingException {
    Hop hop = Hop.builder()
      .currency(new TextNode("USD"))
      .issuer(new TextNode("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
      .build();

    String json = objectMapper.writeValueAsString(hop);
    assertThat(json).contains("currency");
    assertThat(json).contains("issuer");
    assertThat(json).contains("USD");
    assertThat(json).contains("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
  }

  @Test
  void testJsonDeserializationWithCurrencyAndIssuer() throws JsonProcessingException {
    String json = "{\"currency\":\"USD\",\"issuer\":\"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk\"}";

    Hop hop = objectMapper.readValue(json, Hop.class);

    assertThat(hop.currency()).isPresent();
    assertThat(hop.currency().get().asText()).isEqualTo("USD");
    assertThat(hop.issuer()).isPresent();
    assertThat(hop.issuer().get().asText()).isEqualTo("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    assertThat(hop.account()).isEmpty();
    assertThat(hop.mptIssuanceId()).isEmpty();
  }

  @Test
  void testJsonDeserializationWithAccount() throws JsonProcessingException {
    String json = "{\"account\":\"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk\"}";

    Hop hop = objectMapper.readValue(json, Hop.class);

    assertThat(hop.account()).isPresent();
    assertThat(hop.account().get().asText()).isEqualTo("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    assertThat(hop.currency()).isEmpty();
    assertThat(hop.issuer()).isEmpty();
    assertThat(hop.mptIssuanceId()).isEmpty();
  }

  @Test
  void testJsonRoundTripWithMptIssuanceId() throws JsonProcessingException {
    String mptId = "00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    Hop originalHop = Hop.builder()
      .mptIssuanceId(new TextNode(mptId))
      .build();

    String json = objectMapper.writeValueAsString(originalHop);
    Hop deserializedHop = objectMapper.readValue(json, Hop.class);

    assertThat(deserializedHop.mptIssuanceId()).isEqualTo(originalHop.mptIssuanceId());
    assertThat(deserializedHop.mptIssuanceId().get().asText()).isEqualTo(mptId);
  }

  @Test
  void testJsonRoundTripWithAllFields() throws JsonProcessingException {
    Hop originalHop = Hop.builder()
      .account(new TextNode("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk"))
      .currency(new TextNode("USD"))
      .issuer(new TextNode("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .build();

    String json = objectMapper.writeValueAsString(originalHop);
    Hop deserializedHop = objectMapper.readValue(json, Hop.class);

    assertThat(deserializedHop.account()).isEqualTo(originalHop.account());
    assertThat(deserializedHop.currency()).isEqualTo(originalHop.currency());
    assertThat(deserializedHop.issuer()).isEqualTo(originalHop.issuer());
    assertThat(deserializedHop.mptIssuanceId()).isEqualTo(originalHop.mptIssuanceId());
  }
}

