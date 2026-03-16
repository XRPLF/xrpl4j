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
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

/**
 * Unit tests for {@link HopType}.
 */
class HopTypeTest {

  private final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();

  @Test
  void testTypeConstants() {
    assertThat(HopType.TYPE_ACCOUNT).isEqualTo((byte) 0x01);
    assertThat(HopType.TYPE_CURRENCY).isEqualTo((byte) 0x10);
    assertThat(HopType.TYPE_ISSUER).isEqualTo((byte) 0x20);
    assertThat(HopType.TYPE_MPT).isEqualTo((byte) 0x40);
  }

  @Test
  void testEmptyHopType() {
    HopType hopType = new HopType();
    assertThat(hopType.value()).isEqualTo(UnsignedByteArray.empty());
  }

  @Test
  void testFromJsonWithAccount() throws JsonProcessingException {
    ObjectNode json = objectMapper.createObjectNode();
    json.put("account", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");

    HopType hopType = new HopType().fromJson(json);

    assertThat(hopType.value().get(0).asInt() & HopType.TYPE_ACCOUNT).isEqualTo(HopType.TYPE_ACCOUNT);
    assertThat(hopType.value().length()).isEqualTo(21);
  }

  @Test
  void testFromJsonWithCurrency() throws JsonProcessingException {
    ObjectNode json = objectMapper.createObjectNode();
    json.put("currency", "USD");

    HopType hopType = new HopType().fromJson(json);

    assertThat(hopType.value().get(0).asInt() & HopType.TYPE_CURRENCY).isEqualTo(HopType.TYPE_CURRENCY);
    assertThat(hopType.value().length()).isEqualTo(21);
  }

  @Test
  void testFromJsonWithIssuer() throws JsonProcessingException {
    ObjectNode json = objectMapper.createObjectNode();
    json.put("issuer", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");

    HopType hopType = new HopType().fromJson(json);

    assertThat(hopType.value().get(0).asInt() & HopType.TYPE_ISSUER).isEqualTo(HopType.TYPE_ISSUER);
    assertThat(hopType.value().length()).isEqualTo(21);
  }

  @Test
  void testFromJsonWithMptIssuanceId() throws JsonProcessingException {
    ObjectNode json = objectMapper.createObjectNode();
    json.put("mpt_issuance_id", "00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8");

    HopType hopType = new HopType().fromJson(json);
    
    assertThat(hopType.value().get(0).asInt() & HopType.TYPE_MPT).isEqualTo(HopType.TYPE_MPT);
    assertThat(hopType.value().length()).isEqualTo(25);
  }

  @Test
  void testFromJsonWithCurrencyAndIssuer() throws JsonProcessingException {
    ObjectNode json = objectMapper.createObjectNode();
    json.put("currency", "USD");
    json.put("issuer", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");

    HopType hopType = new HopType().fromJson(json);

    int typeByte = hopType.value().get(0).asInt();
    assertThat(typeByte & HopType.TYPE_CURRENCY).isEqualTo(HopType.TYPE_CURRENCY);
    assertThat(typeByte & HopType.TYPE_ISSUER).isEqualTo(HopType.TYPE_ISSUER);
    assertThat(hopType.value().length()).isEqualTo(41);
  }

  @Test
  void testFromJsonWithAccountCurrencyAndIssuer() throws JsonProcessingException {
    ObjectNode json = objectMapper.createObjectNode();
    json.put("account", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
    json.put("currency", "USD");
    json.put("issuer", "rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY");

    HopType hopType = new HopType().fromJson(json);

    int typeByte = hopType.value().get(0).asInt();
    assertThat(typeByte & HopType.TYPE_ACCOUNT).isEqualTo(HopType.TYPE_ACCOUNT);
    assertThat(typeByte & HopType.TYPE_CURRENCY).isEqualTo(HopType.TYPE_CURRENCY);
    assertThat(typeByte & HopType.TYPE_ISSUER).isEqualTo(HopType.TYPE_ISSUER);
    assertThat(hopType.value().length()).isEqualTo(61);
  }

  @Test
  void testFromJsonThrowsWhenCurrencyAndMptBothPresent() {
    // This validation is appropriate for fromJson (user-initiated encoding)
    // because we control what we send and should prevent invalid data.
    // However, fromParser should be permissive (see testFromParserWithBothCurrencyAndMptFlags).
    ObjectNode json = objectMapper.createObjectNode();
    json.put("currency", "USD");
    json.put("mpt_issuance_id", "00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8");

    assertThatThrownBy(() -> new HopType().fromJson(json))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Currency and mpt_issuance_id are mutually exclusive");
  }

  @Test
  void testFromJsonThrowsWhenNodeIsNotObject() {
    assertThatThrownBy(() -> new HopType().fromJson(objectMapper.createArrayNode()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("node is not an object");
  }

  @Test
  void testFromParserWithMptIssuanceId() {
    String hex = "40" + "00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    BinaryParser parser = new BinaryParser(hex);

    HopType hopType = new HopType().fromParser(parser);

    assertThat(hopType.toHex().toUpperCase()).isEqualTo(hex.toUpperCase());
  }

  @Test
  void testFromParserWithBothCurrencyAndMptFlags() {
    // Type byte: 0x50 (TYPE_CURRENCY | TYPE_MPT)
    // This is technically invalid per spec, but we should handle it gracefully
    // Current behavior: reads CURRENCY (20 bytes) due to else-if precedence
    String currency = "0000000000000000000000005553440000000000";
    String hex = "50" + currency;
    BinaryParser parser = new BinaryParser(hex);

    // Should not throw - codec should be permissive when reading ledger data
    HopType hopType = new HopType().fromParser(parser);

    // Verify it read the currency (precedence rule: CURRENCY over MPT)
    assertThat(hopType.value().length()).isEqualTo(21); // 1 byte type + 20 bytes currency
  }

  @Test
  void testToJsonWithMptIssuanceId() throws JsonProcessingException {
    String mptId = "00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    String hex = "40" + mptId;
    BinaryParser parser = new BinaryParser(hex);
    HopType hopType = new HopType().fromParser(parser);

    Hop hop = objectMapper.treeToValue(hopType.toJson(), Hop.class);

    assertThat(hop.mptIssuanceId()).isPresent();
    assertThat(hop.mptIssuanceId().get().asText().toUpperCase()).isEqualTo(mptId.toUpperCase());
    assertThat(hop.account()).isEmpty();
    assertThat(hop.currency()).isEmpty();
    assertThat(hop.issuer()).isEmpty();
  }

  @Test
  void testRoundTripWithMptIssuanceId() throws JsonProcessingException {
    ObjectNode json = objectMapper.createObjectNode();
    json.put("mpt_issuance_id", "00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8");

    HopType hopType = new HopType().fromJson(json);
    Hop hop = objectMapper.treeToValue(hopType.toJson(), Hop.class);

    assertThat(hop.mptIssuanceId()).isPresent();
    assertThat(hop.mptIssuanceId().get().asText().toUpperCase())
      .isEqualTo("00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8");
  }

  @Test
  void testMptWithIssuer() throws JsonProcessingException {
    ObjectNode json = objectMapper.createObjectNode();
    json.put("mpt_issuance_id", "00000001B5F762798A53D543A014CAF8B297CFF8F2F937E8");
    json.put("issuer", "rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");

    HopType hopType = new HopType().fromJson(json);

    int typeByte = hopType.value().get(0).asInt();
    assertThat(typeByte & HopType.TYPE_MPT).isEqualTo(HopType.TYPE_MPT);
    assertThat(typeByte & HopType.TYPE_ISSUER).isEqualTo(HopType.TYPE_ISSUER);
    assertThat(hopType.value().length()).isEqualTo(45);
  }

  @Test
  void testFromParserWithInvalidTypeByteDoesNotThrow() {
    // Test that fromParser is permissive and doesn't throw on unexpected type bytes
    // Type byte: 0x00 (no flags set - technically invalid but should be handled gracefully)
    String hex = "00";
    BinaryParser parser = new BinaryParser(hex);

    // Should not throw - codec should be permissive when reading ledger data
    HopType hopType = new HopType().fromParser(parser);

    // Verify it created a hop with just the type byte
    assertThat(hopType.value().length()).isEqualTo(1);
    assertThat(hopType.value().get(0).asInt()).isEqualTo(0x00);
  }

  @Test
  void testFromParserWithAllFlagsSet() {
    // Type byte: 0x71 (TYPE_ACCOUNT | TYPE_CURRENCY | TYPE_ISSUER | TYPE_MPT)
    // This is invalid per spec (CURRENCY and MPT are mutually exclusive)
    // But fromParser should handle it gracefully
    String account = "B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    String currency = "0000000000000000000000005553440000000000";
    String issuer = "B5F762798A53D543A014CAF8B297CFF8F2F937E8";
    String hex = "71" + account + currency + issuer;
    BinaryParser parser = new BinaryParser(hex);

    // Should not throw - codec should be permissive
    HopType hopType = new HopType().fromParser(parser);

    // Due to else-if precedence, it reads ACCOUNT + CURRENCY + ISSUER (not MPT)
    // 1 byte type + 20 bytes account + 20 bytes currency + 20 bytes issuer = 61 bytes
    assertThat(hopType.value().length()).isEqualTo(61);
  }

  @Test
  void testFromParserWithTruncatedData() {
    // Type byte indicates ACCOUNT (20 bytes) but only 10 bytes provided
    // This simulates corrupted or truncated ledger data
    String hex = "01" + "B5F762798A53D543A014CA"; // Only 10 bytes instead of 20
    BinaryParser parser = new BinaryParser(hex);

    // This will throw StringIndexOutOfBoundsException because BinaryParser.read() will fail
    // when there's not enough data. This demonstrates that the codec WILL fail on truly
    // corrupted data, but it's a low-level parsing error, not a validation error.
    assertThatThrownBy(() -> new HopType().fromParser(parser))
      .isInstanceOf(StringIndexOutOfBoundsException.class)
      .hasMessageContaining("begin 24, end 26, length 24");
  }
}

