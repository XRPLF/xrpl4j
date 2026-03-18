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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

/**
 * Unit tests for {@link IssueType}.
 */
class IssueTypeTest {

  private final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();
  private static final String NO_ACCOUNT_MARKER = "0000000000000000000000000000000000000001";

  @Test
  void testEmptyIssueType() {
    IssueType issueType = new IssueType();
    assertThat(issueType.value().length()).isEqualTo(20);
  }

  @Test
  void testFromJsonWithXrp() throws JsonProcessingException {
    ObjectNode json = objectMapper.createObjectNode();
    json.put("currency", "XRP");

    IssueType issueType = new IssueType().fromJson(json);
    
    // XRP should be 20 bytes (currency only, no issuer)
    assertThat(issueType.value().length()).isEqualTo(20);
  }

  @Test
  void testFromJsonWithIou() throws JsonProcessingException {
    ObjectNode json = objectMapper.createObjectNode();
    json.put("currency", "USD");
    json.put("issuer", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");

    IssueType issueType = new IssueType().fromJson(json);
    
    // IOU should be 40 bytes (20 currency + 20 issuer)
    assertThat(issueType.value().length()).isEqualTo(40);
  }

  @Test
  void testFromJsonWithMptIssuanceId() throws JsonProcessingException {
    ObjectNode json = objectMapper.createObjectNode();
    // MPT issuance ID: 4 bytes sequence + 20 bytes issuer account (48 hex chars = 24 bytes)
    json.put("mpt_issuance_id", "00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8");

    IssueType issueType = new IssueType().fromJson(json);
    
    // MPT should be 44 bytes (20 issuer + 20 no-account marker + 4 sequence)
    assertThat(issueType.value().length()).isEqualTo(44);
    
    // Verify no-account marker is present at bytes 20-39
    String hex = issueType.toHex().toUpperCase();
    String noAccountInHex = hex.substring(40, 80); // Characters 40-79 = bytes 20-39
    assertThat(noAccountInHex).isEqualTo(NO_ACCOUNT_MARKER);
  }

  @Test
  void testFromJsonThrowsWhenMptIssuanceIdWrongLength() {
    ObjectNode json = objectMapper.createObjectNode();
    json.put("mpt_issuance_id", "00000001B5F762798A53D543A014CAF8B297CFF8"); // Too short

    assertThatThrownBy(() -> new IssueType().fromJson(json))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Invalid mpt_issuance_id length");
  }

  @Test
  void testFromJsonThrowsWhenNodeIsNotObject() {
    assertThatThrownBy(() -> new IssueType().fromJson(objectMapper.createArrayNode()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("node is not an object");
  }

  @Test
  void testFromParserWithXrp() {
    // XRP currency code (20 bytes of zeros)
    String hex = "0000000000000000000000000000000000000000";
    BinaryParser parser = new BinaryParser(hex);

    IssueType issueType = new IssueType().fromParser(parser);
    
    assertThat(issueType.value().length()).isEqualTo(20);
    assertThat(issueType.toHex().toUpperCase()).isEqualTo(hex.toUpperCase());
  }

  @Test
  void testFromParserWithIou() {
    // USD currency (20 bytes) + issuer account (20 bytes)
    String hex = "0000000000000000000000005553440000000000" +
                 "B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    BinaryParser parser = new BinaryParser(hex);

    IssueType issueType = new IssueType().fromParser(parser);
    
    assertThat(issueType.value().length()).isEqualTo(40);
    assertThat(issueType.toHex().toUpperCase()).isEqualTo(hex.toUpperCase());
  }

  @Test
  void testFromParserWithMpt() {
    // MPT: issuer account (20 bytes) + no-account marker (20 bytes) + sequence (4 bytes)
    String issuerAccount = "B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    String sequence = "01000000"; // Sequence 1 in little-endian
    String hex = issuerAccount + NO_ACCOUNT_MARKER + sequence;
    BinaryParser parser = new BinaryParser(hex);

    IssueType issueType = new IssueType().fromParser(parser);
    
    assertThat(issueType.value().length()).isEqualTo(44);
    assertThat(issueType.toHex().toUpperCase()).isEqualTo(hex.toUpperCase());
  }

  @Test
  void testToJsonWithXrp() throws JsonProcessingException {
    String hex = "0000000000000000000000000000000000000000";
    BinaryParser parser = new BinaryParser(hex);
    IssueType issueType = new IssueType().fromParser(parser);

    Issue issue = objectMapper.treeToValue(issueType.toJson(), Issue.class);

    assertThat(issue.currency()).isPresent();
    assertThat(issue.currency().get().asText()).isEqualTo("XRP");
    assertThat(issue.issuer()).isEmpty();
    assertThat(issue.mptIssuanceId()).isEmpty();
  }

  @Test
  void testToJsonWithIou() throws JsonProcessingException {
    String hex = "0000000000000000000000005553440000000000" +
                 "B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    BinaryParser parser = new BinaryParser(hex);
    IssueType issueType = new IssueType().fromParser(parser);

    Issue issue = objectMapper.treeToValue(issueType.toJson(), Issue.class);

    assertThat(issue.currency()).isPresent();
    assertThat(issue.currency().get().asText()).isEqualTo("USD");
    assertThat(issue.issuer()).isPresent();
    assertThat(issue.mptIssuanceId()).isEmpty();
  }

  @Test
  void testToJsonWithMpt() throws JsonProcessingException {
    // MPT: issuer account (20 bytes) + no-account marker (20 bytes) + sequence (4 bytes)
    String issuerAccount = "B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    String sequence = "01000000"; // Sequence 1 in little-endian
    String hex = issuerAccount + NO_ACCOUNT_MARKER + sequence;
    BinaryParser parser = new BinaryParser(hex);
    IssueType issueType = new IssueType().fromParser(parser);

    Issue issue = objectMapper.treeToValue(issueType.toJson(), Issue.class);

    assertThat(issue.mptIssuanceId()).isPresent();
    assertThat(issue.currency()).isEmpty();
    assertThat(issue.issuer()).isEmpty();

    // MPT issuance ID in JSON should be: sequence (big-endian) + issuer account
    String expectedMptId = "00000001" + issuerAccount;
    assertThat(issue.mptIssuanceId().get().asText().toUpperCase()).isEqualTo(expectedMptId);
  }

  @Test
  void testRoundTripWithXrp() throws JsonProcessingException {
    ObjectNode json = objectMapper.createObjectNode();
    json.put("currency", "XRP");

    IssueType issueType = new IssueType().fromJson(json);
    Issue issue = objectMapper.treeToValue(issueType.toJson(), Issue.class);

    assertThat(issue.currency()).isPresent();
    assertThat(issue.currency().get().asText()).isEqualTo("XRP");
  }

  @Test
  void testRoundTripWithIou() throws JsonProcessingException {
    ObjectNode json = objectMapper.createObjectNode();
    json.put("currency", "USD");
    json.put("issuer", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");

    IssueType issueType = new IssueType().fromJson(json);
    Issue issue = objectMapper.treeToValue(issueType.toJson(), Issue.class);

    assertThat(issue.currency()).isPresent();
    assertThat(issue.currency().get().asText()).isEqualTo("USD");
    assertThat(issue.issuer()).isPresent();
  }

  @Test
  void testRoundTripWithMpt() throws JsonProcessingException {
    ObjectNode json = objectMapper.createObjectNode();
    String mptId = "00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    json.put("mpt_issuance_id", mptId);

    IssueType issueType = new IssueType().fromJson(json);
    Issue issue = objectMapper.treeToValue(issueType.toJson(), Issue.class);

    assertThat(issue.mptIssuanceId()).isPresent();
    assertThat(issue.mptIssuanceId().get().asText().toUpperCase()).isEqualTo(mptId);
  }

  @Test
  void testMptByteOrderConversion() throws JsonProcessingException {
    // Test that sequence byte order is correctly converted between JSON and binary
    ObjectNode json = objectMapper.createObjectNode();
    // Sequence 256 (0x00000100) in big-endian JSON format
    String mptId = "00000100B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    json.put("mpt_issuance_id", mptId);

    IssueType issueType = new IssueType().fromJson(json);

    // In binary, sequence should be in little-endian: 0x00010000
    String hex = issueType.toHex().toUpperCase();
    String sequenceInBinary = hex.substring(80, 88); // Last 4 bytes (characters 80-87)
    assertThat(sequenceInBinary).isEqualTo("00010000");

    // Round-trip should preserve the original value
    Issue issue = objectMapper.treeToValue(issueType.toJson(), Issue.class);
    assertThat(issue.mptIssuanceId().get().asText().toUpperCase()).isEqualTo(mptId);
  }

  @Test
  void testBinaryRoundTripWithMpt() {
    String issuerAccount = "B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    String sequence = "64000000"; // Sequence 100 in little-endian
    String hex = issuerAccount + NO_ACCOUNT_MARKER + sequence;
    BinaryParser parser = new BinaryParser(hex);

    IssueType issueType = new IssueType().fromParser(parser);
    String roundTripHex = issueType.toHex();

    assertThat(roundTripHex.toUpperCase()).isEqualTo(hex.toUpperCase());
  }
}


