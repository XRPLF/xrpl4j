package com.ripple.xrpl4j.codec.binary;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class BinaryEncoderTest {

  public static final String SIMPLE_JSON = "{\"CloseResolution\":\"01\",\"Method\":\"02\"}";
  public static final String SINGLE_LEVEL_OBJECT_JSON = "{\"Memo\":{\"Memo\":{\"Method\":\"02\"}}}";
  public static final String MULTI_LEVEL_OBJECT_JSON =
      "{\"Memo\":{\"Memo\":{\"CloseResolution\":\"01\",\"Method\":\"02\"}}}";

  public static final String SIMPLE_HEX = "011001021002";
  public static final String SINGLE_OBJECT_HEX = "EAEA021002E1E1";
  public static final String MULTI_LEVEL_OBJECT_HEX = "EAEA011001021002E1E1";

  private XrplBinaryCodec encoder = new XrplBinaryCodec();

  @Test
  void encodeDecodeSimple() throws JsonProcessingException {
    assertThat(encoder.encode(SIMPLE_JSON)).isEqualTo(SIMPLE_HEX);
    assertThat(encoder.decode(SIMPLE_HEX)).isEqualTo(SIMPLE_JSON);
  }

  @Test
  void encodeDecodeSingleChildObject() throws JsonProcessingException {
    assertThat(encoder.encode(SINGLE_LEVEL_OBJECT_JSON)).isEqualTo(SINGLE_OBJECT_HEX);
    assertThat(encoder.decode(SINGLE_OBJECT_HEX)).isEqualTo(SINGLE_LEVEL_OBJECT_JSON);
  }

  @Test
  void encodeJsonWithMultipleEmbeddedObjects() throws JsonProcessingException {
    assertThat(encoder.encode(MULTI_LEVEL_OBJECT_JSON)).isEqualTo(MULTI_LEVEL_OBJECT_HEX);
    assertThat(encoder.decode(MULTI_LEVEL_OBJECT_HEX)).isEqualTo(MULTI_LEVEL_OBJECT_JSON);
  }

  @Test
  void encodeDecodeAmount() throws JsonProcessingException {
    String json = "{\"Fee\":\"100\"}";
    String hex = "684000000000000064";
    assertThat(encoder.encode(json)).isEqualTo(hex);
    assertThat(encoder.decode(hex)).isEqualTo(json);
  }

  @Test
  void encodeDecodeHash128() throws JsonProcessingException {
    String json = "{\"EmailHash\":\"11223344556677889900AABBCCDDEEFF\"}";
    String hex = "4111223344556677889900AABBCCDDEEFF";
    assertThat(encoder.encode(json)).isEqualTo(hex);
    assertThat(encoder.decode(hex)).isEqualTo(json);
  }

  @Test
  void encodeDecodeHash160() throws JsonProcessingException {
    String json = "{\"TakerPaysCurrency\":\"11223344556677889900AABBCCDDEEFF11223344\"}";
    String hex = "011111223344556677889900AABBCCDDEEFF11223344";
    assertThat(encoder.encode(json)).isEqualTo(hex);
    assertThat(encoder.decode(hex)).isEqualTo(json);
  }

  @Test
  void encodeDecodeHash256() throws JsonProcessingException {
    String json = "{\"LedgerHash\":\"11223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF\"}";
    String hex = "5111223344556677889900AABBCCDDEEFF11223344556677889900AABBCCDDEEFF";
    assertThat(encoder.encode(json)).isEqualTo(hex);
    assertThat(encoder.decode(hex)).isEqualTo(json);
  }

  @Test
  void encodeDecodeBlob() throws JsonProcessingException {
    String json = "{\"Domain\":\"1234\"}";
    String hex = "77021234";
    assertThat(encoder.encode(json)).isEqualTo(hex);
    assertThat(encoder.decode(hex)).isEqualTo(json);
  }

  @Test
  void encodeDecodeIssuedCurrency() throws JsonProcessingException {
    String json = "{\"Fee\":{\"value\":\"123\",\"currency\":\"USD\",\"issuer\":\"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"}}";
    String hex = "68D5045EADB112E00000000000000000000000000055534400000000005E7B112523F68D2F5E879DB4EAC51C6698A69304";
    assertThat(encoder.encode(json)).isEqualTo(hex);
    assertThat(encoder.decode(hex)).isEqualTo(json);
  }

}