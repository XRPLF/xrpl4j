package com.ripple.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

class CurrencyTypeTest {

  public static final char DOUBLE_QUOTE = '"';
  private final CurrencyType codec = new CurrencyType();

  @Test
  void decodeISO3() {
    assertThat(codec.fromHex("0000000000000000000000000000000000000000").toJSON().asText()).isEqualTo("XRP");
    assertThat(codec.fromHex("0000000000000000000000005553440000000000").toJSON().asText()).isEqualTo("USD");
  }

  @Test
  void encodeISO3() {
    assertThat(codec.fromJSON(DOUBLE_QUOTE + "XRP" + DOUBLE_QUOTE).toHex())
        .isEqualTo("0000000000000000000000000000000000000000");
    assertThat(codec.fromJSON(DOUBLE_QUOTE + "USD" + DOUBLE_QUOTE).toHex())
        .isEqualTo("0000000000000000000000005553440000000000");
  }

  @Test
  void decodeCustom() {
    String customCode = Strings.repeat("11", 20);
    assertThat(codec.fromHex(customCode).toJSON().asText()).isEqualTo(customCode);
  }

  @Test
  void encodeCustom() {
    String customCode = Strings.repeat("11", 20);
    assertThat(codec.fromJSON(DOUBLE_QUOTE + customCode + DOUBLE_QUOTE).toHex())
        .isEqualTo(customCode);
  }

}
