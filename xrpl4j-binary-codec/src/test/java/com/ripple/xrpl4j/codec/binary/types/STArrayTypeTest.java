package com.ripple.xrpl4j.codec.binary.types;

import static com.ripple.xrpl4j.codec.binary.types.STArrayType.ARRAY_END_MARKER_HEX;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class STArrayTypeTest {

  private final STArrayType codec = new STArrayType();

  public static final String MEMO =
      "{\"Memo\":{\"MemoType\":\"687474703A2F2F6578616D706C652E636F6D2F6D656D6F2F67656E65726963\",\"MemoData\":\"72656E74\"}}";
  private static final String JSON = "[" + MEMO + "," + MEMO + "]";

  public static final String MEMO_HEX = "EA7C1F687474703A2F2F6578616D706C652E636F6D2F6D656D6F2F67656E657269637D0472656E74E1";
  private static final String HEX = MEMO_HEX + MEMO_HEX + ARRAY_END_MARKER_HEX;

  @Test
  void decode() {
    assertThat(codec.fromHex(HEX).toJSON().toString()).isEqualTo(JSON);
  }

  @Test
  void encode() {
    assertThat(codec.fromJSON(JSON).toHex()).isEqualTo(HEX);
  }

}
