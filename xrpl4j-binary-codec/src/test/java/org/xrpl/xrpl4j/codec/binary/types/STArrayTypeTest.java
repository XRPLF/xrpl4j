package org.xrpl.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link STArrayType}.
 */
@SuppressWarnings("AbbreviationAsWordInName")
class STArrayTypeTest {

  public static final String MEMO =
    "{\"Memo\":" +
      "{\"MemoType\":\"687474703A2F2F6578616D706C652E636F6D2F6D656D6F2F67656E65726963\"," +
      "\"MemoData\":\"72656E74\"}}";
  public static final String MEMO_HEX =
    "EA7C1F687474703A2F2F6578616D706C652E636F6D2F6D656D6F2F67656E657269637D0472656E74E1";
  private static final String JSON = "[" + MEMO + "," + MEMO + "]";
  private static final String HEX = MEMO_HEX + MEMO_HEX + STArrayType.ARRAY_END_MARKER_HEX;
  private final STArrayType codec = new STArrayType();

  @Test
  void decode() {
    assertThat(codec.fromHex(HEX).toJson().toString()).isEqualTo(JSON);
  }

  @Test
  void encode() {
    assertThat(codec.fromJson(JSON).toHex()).isEqualTo(HEX);
  }

}
