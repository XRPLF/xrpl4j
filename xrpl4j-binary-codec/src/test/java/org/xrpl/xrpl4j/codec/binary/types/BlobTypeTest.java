package org.xrpl.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

class BlobTypeTest {

  public static final char DOUBLE_QUOTE = '"';
  private final BlobType codec = new BlobType();

  @Test
  void decode() {
    int width = 16;
    assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
  }

  @Test
  void encode() {
    assertThat(codec.fromJSON(DOUBLE_QUOTE + bytes(16) + DOUBLE_QUOTE).toHex()).isEqualTo(bytes(16));
  }

  private String bytes(int size) {
    return Strings.repeat("0F", size);
  }

}
