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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

/**
 * Unit tests for MPT support in PathSet serialization.
 */
class PathSetMptTest {

  private static final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();

  @Test
  void testSingleMptHop() throws JsonProcessingException {
    String mptIssuanceId = "00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    
    // Create path with single MPT hop
    ArrayNode path = objectMapper.createArrayNode();
    ObjectNode hop = objectMapper.createObjectNode();
    hop.put("mpt_issuance_id", mptIssuanceId);
    path.add(hop);
    
    ArrayNode pathSet = objectMapper.createArrayNode();
    pathSet.add(path);
    
    // Serialize
    PathSetType pathSetType = new PathSetType();
    PathSetType serialized = pathSetType.fromJson(pathSet);
    
    // Expected: 0x40 (MPT type) + 24-byte MPT ID + 0x00 (end marker)
    String expectedHex = "40" + mptIssuanceId + "00";
    assertThat(serialized.toHex().toUpperCase()).isEqualTo(expectedHex.toUpperCase());
    
    // Verify round-trip
    assertThat(serialized.toJson()).isEqualTo(pathSet);
    
    // Verify deserialization
    BinaryParser parser = new BinaryParser(expectedHex);
    PathSetType deserialized = pathSetType.fromParser(parser);
    assertThat(deserialized.toJson()).isEqualTo(pathSet);
  }

  @Test
  void testTwoMptHops() throws JsonProcessingException {
    String mptId1 = "00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    String mptId2 = "000004C463C52827307480341125DA0577DEFC38405B0E3E";

    // Create path set with two MPT hops
    ArrayNode path1 = objectMapper.createArrayNode();
    ObjectNode hop1 = objectMapper.createObjectNode();
    hop1.put("mpt_issuance_id", mptId1);
    path1.add(hop1);
    
    ArrayNode path2 = objectMapper.createArrayNode();
    ObjectNode hop2 = objectMapper.createObjectNode();
    hop2.put("mpt_issuance_id", mptId2);
    path2.add(hop2);

    ArrayNode pathSet = objectMapper.createArrayNode();
    pathSet.add(path1);
    pathSet.add(path2);
    
    // Serialize
    PathSetType pathSetType = new PathSetType();
    PathSetType serialized = pathSetType.fromJson(pathSet);
    
    // Expected: 0x40 + MPT1 + 0xFF (separator) + 0x40 + MPT2 + 0x00 (end)
    String expectedHex = "40" + mptId1 + "FF" + "40" + mptId2 + "00";
    assertThat(serialized.toHex().toUpperCase()).isEqualTo(expectedHex.toUpperCase());
    
    // Verify round-trip
    assertThat(serialized.toJson()).isEqualTo(pathSet);
  }

  @Test
  void testMptAndCurrencyInDifferentHops() throws JsonProcessingException {
    String mptIssuanceId = "00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    String currency = "USD";
    
    // Create path with MPT hop followed by currency hop
    ArrayNode path = objectMapper.createArrayNode();
    
    ObjectNode mptHop = objectMapper.createObjectNode();
    mptHop.put("mpt_issuance_id", mptIssuanceId);
    path.add(mptHop);
    
    ObjectNode currencyHop = objectMapper.createObjectNode();
    currencyHop.put("currency", currency);
    path.add(currencyHop);
    
    ArrayNode pathSet = objectMapper.createArrayNode();
    pathSet.add(path);
    
    // Serialize
    PathSetType pathSetType = new PathSetType();
    PathSetType serialized = pathSetType.fromJson(pathSet);
    
    // Verify it serializes without error
    assertThat(serialized.toHex()).isNotEmpty();
    
    // Verify round-trip
    assertThat(serialized.toJson()).isEqualTo(pathSet);
  }

  @Test
  void testMptAndIssuerInDifferentHops() throws JsonProcessingException {
    String mptIssuanceId = "00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    String issuer = "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh";
    
    // Create path with MPT hop followed by issuer hop
    ArrayNode path = objectMapper.createArrayNode();
    
    ObjectNode mptHop = objectMapper.createObjectNode();
    mptHop.put("mpt_issuance_id", mptIssuanceId);
    path.add(mptHop);
    
    ObjectNode issuerHop = objectMapper.createObjectNode();
    issuerHop.put("issuer", issuer);
    path.add(issuerHop);
    
    ArrayNode pathSet = objectMapper.createArrayNode();
    pathSet.add(path);
    
    // Serialize
    PathSetType pathSetType = new PathSetType();
    PathSetType serialized = pathSetType.fromJson(pathSet);
    
    // Verify it serializes without error
    assertThat(serialized.toHex()).isNotEmpty();
    
    // Verify round-trip
    assertThat(serialized.toJson()).isEqualTo(pathSet);
  }

  @Test
  void testCurrencyTakesPrecedenceOverMptWhenBothPresent() throws JsonProcessingException {
    // When both currency and mpt_issuance_id appear in a hop, currency takes precedence (permissive behavior).
    ObjectNode hop = objectMapper.createObjectNode();
    hop.put("currency", "USD");
    hop.put("mpt_issuance_id", "00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8");

    HopType hopType = new HopType().fromJson(hop);
    // TYPE_CURRENCY bit (0x10) must be set; TYPE_MPT bit (0x40) must not
    int typeByte = hopType.value().get(0).asInt();
    assertThat(typeByte & HopType.TYPE_CURRENCY).isEqualTo((int) HopType.TYPE_CURRENCY);
    assertThat(typeByte & HopType.TYPE_MPT).isEqualTo(0);
  }

  @Test
  void testMptTypeConstant() {
    // Verify TYPE_MPT constant is 0x40
    assertThat(HopType.TYPE_MPT).isEqualTo((byte) 0x40);
  }

  @Test
  void testUInt192TypeWidth() {
    // Verify UInt192Type has correct width (24 bytes)
    assertThat(UInt192Type.WIDTH_BYTES).isEqualTo(24);
  }
}

