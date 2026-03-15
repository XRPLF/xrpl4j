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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;

/**
 * Unit tests for {@link IssueType}.
 */
class IssueTypeTest {

  private static final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();
  private final IssueType codec = new IssueType();

  /**
   * Test XRP Issue round-trip: JSON -> Binary -> JSON.
   */
  @Test
  void xrpRoundTrip() throws JsonProcessingException {
    JsonNode xrpIssue = objectMapper.createObjectNode().set("currency", new TextNode("XRP"));

    JsonNode result = codec.fromHex(codec.fromJson(xrpIssue).toHex()).toJson();

    assertThat(result).isEqualTo(xrpIssue);
  }

  /**
   * Test IOU Issue round-trip: JSON -> Binary -> JSON.
   */
  @Test
  void iouRoundTrip() throws JsonProcessingException {
    ObjectNode iouIssue = objectMapper.createObjectNode();
    iouIssue.set("currency", new TextNode("USD"));
    iouIssue.set("issuer", new TextNode("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"));

    JsonNode result = codec.fromHex(codec.fromJson(iouIssue).toHex()).toJson();

    assertThat(result).isEqualTo(iouIssue);
  }

  /**
   * Test MPT Issue round-trip: JSON -> Binary -> JSON.
   */
  @Test
  void mptRoundTrip() throws JsonProcessingException {
    JsonNode mptIssue = objectMapper.createObjectNode()
      .set("mpt_issuance_id", new TextNode("00000002AE123A8556F3CF91154711376AFB0F894F832B3D"));

    JsonNode result = codec.fromHex(codec.fromJson(mptIssue).toHex()).toJson();

    assertThat(result).isEqualTo(mptIssue);
  }

  /**
   * Test MPT issuance ID validation: invalid length.
   */
  @Test
  void mptIssuanceIdInvalidLength() {
    assertInvalidMptId("0000002AE123A8556F3CF91154711376AFB0F894F832B3D"); // 47 chars
    assertInvalidMptId("000000002AE123A8556F3CF91154711376AFB0F894F832B3D"); // 49 chars
    assertInvalidMptId(""); // empty
  }

  /**
   * Test MPT issuance ID validation: invalid characters.
   */
  @Test
  void mptIssuanceIdInvalidCharacters() {
    assertInvalidMptId("00000002GE123A8556F3CF91154711376AFB0F894F832B3D"); // 'G'
    assertInvalidMptId("00000002 E123A8556F3CF91154711376AFB0F894F832B3D"); // space
    assertInvalidMptId("00000002-E123A8556F3CF91154711376AFB0F894F832B3D"); // hyphen
  }

  /**
   * Test that invalid JSON (not an object) throws an exception.
   */
  @Test
  void invalidJsonNotObject() {
    assertThatThrownBy(() -> codec.fromJson(new TextNode("not an object")))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("node is not an object");
  }

  /**
   * Test Issue validation: cannot have both currency and mpt_issuance_id.
   */
  @Test
  void issueValidationCannotHaveBothCurrencyAndMpt() {
    assertThatThrownBy(() -> Issue.builder()
      .currency(new TextNode("USD"))
      .mptIssuanceId(new TextNode("00000002AE123A8556F3CF91154711376AFB0F894F832B3D"))
      .build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Issue cannot have both currency and mpt_issuance_id");
  }

  /**
   * Test Issue validation: XRP cannot have an issuer.
   */
  @Test
  void issueValidationXrpCannotHaveIssuer() {
    assertThatThrownBy(() -> Issue.builder()
      .currency(new TextNode("XRP"))
      .issuer(new TextNode("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
      .build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("If Issue is XRP, issuer must be empty");
  }

  /**
   * Test Issue validation: must have either currency or mpt_issuance_id.
   */
  @Test
  void issueValidationMustHaveCurrencyOrMpt() {
    assertThatThrownBy(() -> Issue.builder().build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Issue must have either currency or mpt_issuance_id");
  }

  /**
   * Test Issue validation: MPT cannot have an issuer.
   */
  @Test
  void issueValidationMptCannotHaveIssuer() {
    assertThatThrownBy(() -> Issue.builder()
      .mptIssuanceId(new TextNode("00000002AE123A8556F3CF91154711376AFB0F894F832B3D"))
      .issuer(new TextNode("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"))
      .build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("MPT Issue must not have an issuer");
  }

  private void assertInvalidMptId(String invalidId) {
    ObjectNode mptIssue = objectMapper.createObjectNode();
    mptIssue.set("mpt_issuance_id", new TextNode(invalidId));
    assertThatThrownBy(() -> codec.fromJson(mptIssue))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("mpt_issuance_id must be a 48-character hexadecimal string");
  }
}
