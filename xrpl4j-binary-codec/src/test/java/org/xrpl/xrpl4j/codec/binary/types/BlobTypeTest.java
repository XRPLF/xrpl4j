package org.xrpl.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BlobType}.
 */
class BlobTypeTest {

  public static final char DOUBLE_QUOTE = '"';
  private final BlobType codec = new BlobType();

  @Test
  void decode() {
    int width = 1;
    assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
    width = 16;
    assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
    width = 32;
    assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
    width = 33;
    assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
    width = 64;
    assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
    width = 128;
    assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
    width = 256;
    assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
  }

  // TODO: Make empty buffers work
  @Test
  void decodeEmpty() {
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
      int width = 0;
      assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
    });
  }

  @Test
  void encode() {
    assertThat(codec.fromJSON(DOUBLE_QUOTE + bytes(16) + DOUBLE_QUOTE).toHex()).isEqualTo(bytes(16));
  }

  private String bytes(int size) {
    return Strings.repeat("0F", size);
  }

}
