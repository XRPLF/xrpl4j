package com.ripple.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AccountIdTypeTest {

  public static final char DOUBLE_QUOTE = '"';
  private static final AccountIdType codec = new AccountIdType();

  @Test
  void decode() {
    assertThat(codec.fromHex("5E7B112523F68D2F5E879DB4EAC51C6698A69304").toJSON().asText())
        .isEqualTo("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59");
  }

  @Test
  void encode() {
    assertThat(codec.fromJSON(DOUBLE_QUOTE + "r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59" + DOUBLE_QUOTE).toHex())
        .isEqualTo("5E7B112523F68D2F5E879DB4EAC51C6698A69304");
  }

}
