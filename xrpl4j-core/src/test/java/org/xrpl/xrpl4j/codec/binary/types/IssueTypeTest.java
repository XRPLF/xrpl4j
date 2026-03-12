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
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

/**
 * Unit tests for {@link IssueType}.
 */
class IssueTypeTest {

  private static final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();
  private final IssueType codec = new IssueType();

  /**
   * Test XRP Issue encoding and decoding.
   * XRP is represented as 20 bytes of zeros (currency only, no issuer).
   */
  @Test
  void encodeDecodeXrp() throws JsonProcessingException {
    // XRP Issue JSON
    ObjectNode xrpIssue = objectMapper.createObjectNode();
    xrpIssue.set("currency", new TextNode("XRP"));

    // Encode to hex
    IssueType encoded = codec.fromJson(xrpIssue);
    String hex = encoded.toHex();

    // XRP should be 20 bytes (40 hex chars) of zeros
    assertThat(hex).isEqualTo("0000000000000000000000000000000000000000");
    assertThat(encoded.value().length()).isEqualTo(20);

    // Decode back to JSON
    IssueType decoded = codec.fromHex(hex);
    assertThat(decoded.toJson().get("currency").asText()).isEqualTo("XRP");
    assertThat(decoded.toJson().has("issuer")).isFalse();
  }

  /**
   * Test IOU Issue encoding and decoding.
   * IOU is represented as currency (20 bytes) + issuer (20 bytes) = 40 bytes total.
   */
  @Test
  void encodeDecodeIou() throws JsonProcessingException {
    // IOU Issue JSON
    ObjectNode iouIssue = objectMapper.createObjectNode();
    iouIssue.set("currency", new TextNode("USD"));
    iouIssue.set("issuer", new TextNode("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"));

    // Encode to hex
    IssueType encoded = codec.fromJson(iouIssue);
    String hex = encoded.toHex();

    // IOU should be 40 bytes (80 hex chars): currency (20) + issuer (20)
    assertThat(hex).hasSize(80);
    assertThat(encoded.value().length()).isEqualTo(40);

    // Decode back to JSON
    IssueType decoded = codec.fromHex(hex);
    assertThat(decoded.toJson().get("currency").asText()).isEqualTo("USD");
    assertThat(decoded.toJson().get("issuer").asText()).isEqualTo("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
  }

  /**
   * Test IOU with custom (non-standard) currency code.
   */
  @Test
  void encodeDecodeIouCustomCurrency() throws JsonProcessingException {
    // IOU with hex currency code
    ObjectNode iouIssue = objectMapper.createObjectNode();
    iouIssue.set("currency", new TextNode("0123456789ABCDEF01230123456789ABCDEF0123"));
    iouIssue.set("issuer", new TextNode("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"));

    // Encode to hex
    IssueType encoded = codec.fromJson(iouIssue);
    String hex = encoded.toHex();

    // Should be 40 bytes
    assertThat(hex).hasSize(80);
    assertThat(encoded.value().length()).isEqualTo(40);

    // Decode back to JSON
    IssueType decoded = codec.fromHex(hex);
    assertThat(decoded.toJson().get("currency").asText()).isEqualTo("0123456789ABCDEF01230123456789ABCDEF0123");
    assertThat(decoded.toJson().get("issuer").asText()).isEqualTo("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
  }

  /**
   * Test MPT Issue encoding and decoding.
   * MPT is represented as issuer (20 bytes) + NO_ACCOUNT sentinel (20 bytes) + sequence (4 bytes) = 44 bytes total.
   */
  @Test
  void encodeDecodeMpt() throws JsonProcessingException {
    // MPT Issue JSON with mpt_issuance_id
    // MPTID format: sequence (4 bytes BE) + issuer (20 bytes) = 24 bytes = 48 hex chars
    ObjectNode mptIssue = objectMapper.createObjectNode();
    mptIssue.set("mpt_issuance_id", new TextNode("00000002AE123A8556F3CF91154711376AFB0F894F832B3D"));

    // Encode to hex
    IssueType encoded = codec.fromJson(mptIssue);
    String hex = encoded.toHex();

    // MPT should be 44 bytes (88 hex chars): issuer (20) + NO_ACCOUNT (20) + sequence (4)
    assertThat(hex).hasSize(88);
    assertThat(encoded.value().length()).isEqualTo(44);

    // Verify structure: issuer + NO_ACCOUNT + sequence (LE)
    // Issuer from MPTID (bytes 4-24): AE123A8556F3CF91154711376AFB0F894F832B3D
    assertThat(hex.substring(0, 40)).isEqualTo("AE123A8556F3CF91154711376AFB0F894F832B3D");
    // NO_ACCOUNT sentinel: 19 zeros + 01
    assertThat(hex.substring(40, 80)).isEqualTo("0000000000000000000000000000000000000001");
    // Sequence in little-endian: 00000002 (BE) -> 02000000 (LE)
    assertThat(hex.substring(80, 88)).isEqualTo("02000000");

    // Decode back to JSON
    IssueType decoded = codec.fromHex(hex);
    assertThat(decoded.toJson().get("mpt_issuance_id").asText())
      .isEqualTo("00000002AE123A8556F3CF91154711376AFB0F894F832B3D");
  }

  /**
   * Test round-trip: JSON -> Binary -> JSON for XRP.
   */
  @Test
  void roundTripXrp() throws JsonProcessingException {
    ObjectNode original = objectMapper.createObjectNode();
    original.set("currency", new TextNode("XRP"));

    IssueType encoded = codec.fromJson(original);
    IssueType decoded = codec.fromHex(encoded.toHex());

    assertThat(decoded.toJson().get("currency").asText()).isEqualTo("XRP");
  }

  /**
   * Test round-trip: JSON -> Binary -> JSON for IOU.
   */
  @Test
  void roundTripIou() throws JsonProcessingException {
    ObjectNode original = objectMapper.createObjectNode();
    original.set("currency", new TextNode("USD"));
    original.set("issuer", new TextNode("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"));

    IssueType encoded = codec.fromJson(original);
    IssueType decoded = codec.fromHex(encoded.toHex());

    assertThat(decoded.toJson().get("currency").asText()).isEqualTo("USD");
    assertThat(decoded.toJson().get("issuer").asText()).isEqualTo("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
  }

  /**
   * Test round-trip: JSON -> Binary -> JSON for MPT.
   */
  @Test
  void roundTripMpt() throws JsonProcessingException {
    ObjectNode original = objectMapper.createObjectNode();
    original.set("mpt_issuance_id", new TextNode("00000002AE123A8556F3CF91154711376AFB0F894F832B3D"));

    IssueType encoded = codec.fromJson(original);
    IssueType decoded = codec.fromHex(encoded.toHex());

    assertThat(decoded.toJson().get("mpt_issuance_id").asText())
      .isEqualTo("00000002AE123A8556F3CF91154711376AFB0F894F832B3D");
  }

  /**
   * Test round-trip: Binary -> JSON -> Binary for XRP.
   */
  @Test
  void roundTripBinaryXrp() throws JsonProcessingException {
    String originalHex = "0000000000000000000000000000000000000000";

    IssueType decoded = codec.fromHex(originalHex);
    IssueType reEncoded = codec.fromJson(decoded.toJson());

    assertThat(reEncoded.toHex()).isEqualTo(originalHex);
  }

  /**
   * Test round-trip: Binary -> JSON -> Binary for IOU.
   */
  @Test
  void roundTripBinaryIou() throws JsonProcessingException {
    // USD with specific issuer
    String originalHex = "0000000000000000000000005553440000000000B5F762798A53D543A014CAF8B297CFF8F2F937E8";

    IssueType decoded = codec.fromHex(originalHex);
    IssueType reEncoded = codec.fromJson(decoded.toJson());

    assertThat(reEncoded.toHex()).isEqualToIgnoringCase(originalHex);
  }

  /**
   * Test round-trip: Binary -> JSON -> Binary for MPT.
   */
  @Test
  void roundTripBinaryMpt() throws JsonProcessingException {
    // MPT: issuer + NO_ACCOUNT + sequence (LE)
    String originalHex = "AE123A8556F3CF91154711376AFB0F894F832B3D000000000000000000000000000000000000000102000000";

    IssueType decoded = codec.fromHex(originalHex);
    IssueType reEncoded = codec.fromJson(decoded.toJson());

    assertThat(reEncoded.toHex()).isEqualToIgnoringCase(originalHex);
  }

  /**
   * Test BinaryParser for XRP.
   */
  @Test
  void parseXrp() {
    String hex = "0000000000000000000000000000000000000000";
    BinaryParser parser = new BinaryParser(hex);

    IssueType parsed = codec.fromParser(parser);

    assertThat(parsed.toHex()).isEqualTo(hex);
    assertThat(parsed.toJson().get("currency").asText()).isEqualTo("XRP");
  }

  /**
   * Test BinaryParser for IOU.
   */
  @Test
  void parseIou() {
    String hex = "0000000000000000000000005553440000000000B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    BinaryParser parser = new BinaryParser(hex);

    IssueType parsed = codec.fromParser(parser);

    assertThat(parsed.toHex()).isEqualToIgnoringCase(hex);
    assertThat(parsed.toJson().get("currency").asText()).isEqualTo("USD");
    assertThat(parsed.toJson().has("issuer")).isTrue();
  }

  /**
   * Test BinaryParser for MPT.
   */
  @Test
  void parseMpt() {
    // MPT: issuer + NO_ACCOUNT + sequence (LE)
    String hex = "AE123A8556F3CF91154711376AFB0F894F832B3D000000000000000000000000000000000000000102000000";
    BinaryParser parser = new BinaryParser(hex);

    IssueType parsed = codec.fromParser(parser);

    assertThat(parsed.toHex()).isEqualToIgnoringCase(hex);
    assertThat(parsed.toJson().has("mpt_issuance_id")).isTrue();
    assertThat(parsed.toJson().get("mpt_issuance_id").asText())
      .isEqualTo("00000002AE123A8556F3CF91154711376AFB0F894F832B3D");
  }

  /**
   * Test MPT with different sequence values to verify endianness conversion.
   */
  @Test
  void mptSequenceEndianness() throws JsonProcessingException {
    // Test sequence = 1 (0x00000001 BE -> 0x01000000 LE)
    ObjectNode mptIssue1 = objectMapper.createObjectNode();
    mptIssue1.set("mpt_issuance_id", new TextNode("00000001AE123A8556F3CF91154711376AFB0F894F832B3D"));

    IssueType encoded1 = codec.fromJson(mptIssue1);
    String hex1 = encoded1.toHex();

    // Verify sequence is little-endian: 01000000
    assertThat(hex1.substring(80, 88)).isEqualTo("01000000");

    // Test sequence = 256 (0x00000100 BE -> 0x00010000 LE)
    ObjectNode mptIssue256 = objectMapper.createObjectNode();
    mptIssue256.set("mpt_issuance_id", new TextNode("00000100AE123A8556F3CF91154711376AFB0F894F832B3D"));

    IssueType encoded256 = codec.fromJson(mptIssue256);
    String hex256 = encoded256.toHex();

    // Verify sequence is little-endian: 00010000
    assertThat(hex256.substring(80, 88)).isEqualTo("00010000");

    // Test sequence = 0x12345678 (BE) -> 0x78563412 (LE)
    ObjectNode mptIssueComplex = objectMapper.createObjectNode();
    mptIssueComplex.set("mpt_issuance_id", new TextNode("12345678AE123A8556F3CF91154711376AFB0F894F832B3D"));

    IssueType encodedComplex = codec.fromJson(mptIssueComplex);
    String hexComplex = encodedComplex.toHex();

    // Verify sequence is little-endian: 78563412
    assertThat(hexComplex.substring(80, 88)).isEqualTo("78563412");
  }

  /**
   * Test that invalid JSON throws an exception.
   */
  @Test
  void invalidJsonNotObject() {
    TextNode notAnObject = new TextNode("not an object");

    assertThatThrownBy(() -> codec.fromJson(notAnObject))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("node is not an object");
  }

  /**
   * Test byte length validation.
   */
  @Test
  void byteLengthValidation() throws JsonProcessingException {
    // XRP: 20 bytes
    ObjectNode xrp = objectMapper.createObjectNode();
    xrp.set("currency", new TextNode("XRP"));
    assertThat(codec.fromJson(xrp).value().length()).isEqualTo(20);

    // IOU: 40 bytes
    ObjectNode iou = objectMapper.createObjectNode();
    iou.set("currency", new TextNode("USD"));
    iou.set("issuer", new TextNode("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"));
    assertThat(codec.fromJson(iou).value().length()).isEqualTo(40);

    // MPT: 44 bytes
    ObjectNode mpt = objectMapper.createObjectNode();
    mpt.set("mpt_issuance_id", new TextNode("00000002AE123A8556F3CF91154711376AFB0F894F832B3D"));
    assertThat(codec.fromJson(mpt).value().length()).isEqualTo(44);
  }

  /**
   * Test NO_ACCOUNT sentinel detection in parser.
   */
  @Test
  void noAccountSentinelDetection() {
    // Create a binary with NO_ACCOUNT sentinel (should be detected as MPT)
    String mptHex = "AE123A8556F3CF91154711376AFB0F894F832B3D000000000000000000000000000000000000000102000000";
    BinaryParser parser = new BinaryParser(mptHex);

    IssueType parsed = codec.fromParser(parser);

    // Should be recognized as MPT, not IOU
    assertThat(parsed.toJson().has("mpt_issuance_id")).isTrue();
    assertThat(parsed.toJson().has("currency")).isFalse();
    assertThat(parsed.toJson().has("issuer")).isFalse();
  }

  /**
   * Test that regular IOU (non-NO_ACCOUNT) is correctly distinguished from MPT.
   */
  @Test
  void iouVsMptDistinction() {
    // IOU with a regular issuer (not NO_ACCOUNT)
    String iouHex = "0000000000000000000000005553440000000000B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    BinaryParser iouParser = new BinaryParser(iouHex);

    IssueType iouParsed = codec.fromParser(iouParser);

    // Should be recognized as IOU
    assertThat(iouParsed.toJson().has("currency")).isTrue();
    assertThat(iouParsed.toJson().has("issuer")).isTrue();
    assertThat(iouParsed.toJson().has("mpt_issuance_id")).isFalse();

    // MPT with NO_ACCOUNT sentinel
    String mptHex = "AE123A8556F3CF91154711376AFB0F894F832B3D000000000000000000000000000000000000000102000000";
    BinaryParser mptParser = new BinaryParser(mptHex);

    IssueType mptParsed = codec.fromParser(mptParser);

    // Should be recognized as MPT
    assertThat(mptParsed.toJson().has("mpt_issuance_id")).isTrue();
    assertThat(mptParsed.toJson().has("currency")).isFalse();
    assertThat(mptParsed.toJson().has("issuer")).isFalse();
  }

  /**
   * Test MPT with maximum sequence value.
   */
  @Test
  void mptMaxSequence() throws JsonProcessingException {
    // Max uint32: 0xFFFFFFFF (BE) -> 0xFFFFFFFF (LE, same because symmetric)
    ObjectNode mptIssue = objectMapper.createObjectNode();
    mptIssue.set("mpt_issuance_id", new TextNode("FFFFFFFFAE123A8556F3CF91154711376AFB0F894F832B3D"));

    IssueType encoded = codec.fromJson(mptIssue);
    String hex = encoded.toHex();

    // Verify sequence is little-endian: FFFFFFFF
    assertThat(hex.substring(80, 88)).isEqualTo("FFFFFFFF");

    // Round-trip
    IssueType decoded = codec.fromHex(hex);
    assertThat(decoded.toJson().get("mpt_issuance_id").asText())
      .isEqualTo("FFFFFFFFAE123A8556F3CF91154711376AFB0F894F832B3D");
  }
}
