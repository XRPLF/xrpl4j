package org.xrpl.xrpl4j.crypto.core.keys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Unit tests for {@link Passphrase}.
 */
class PassphraseTest {

  private Passphrase passphrase1;
  private Passphrase passphrase2;

  @BeforeEach
  public void setUp() {
    passphrase1 = Passphrase.of("hello");
    passphrase2 = Passphrase.of("world");
  }

  @Test
  void constructWithBytes() {
    byte[] bytes = "hello world".getBytes(StandardCharsets.UTF_8);
    Passphrase passphrase = Passphrase.of(bytes);
    assertThat(passphrase.value()).isEqualTo(bytes);
  }

  @Test
  void passphraseWithNullBytes() {
    assertThrows(NullPointerException.class, () -> {
      byte[] nullBytes = null;
      Passphrase.of(nullBytes);
    });
  }

  @Test
  void passphraseWithNullString() {
    assertThrows(NullPointerException.class, () -> {
      String nullString = null;
      Passphrase.of(nullString);
    });
  }

  @Test
  void destroy() {
    assertThat(passphrase1.isDestroyed()).isFalse();
    passphrase1.destroy();
    assertThat(passphrase1.isDestroyed()).isTrue();
    assertThat(Arrays.equals(passphrase1.value(), new byte[5])).isTrue();
  }

  @Test
  void equals() {
    assertThat(passphrase1).isEqualTo(passphrase1);
    assertThat(passphrase1).isNotEqualTo(passphrase2);
    assertThat(passphrase2).isNotEqualTo(passphrase1);
    assertThat(passphrase1).isNotEqualTo(new Object());
  }

  @Test
  void hashcode() {
    assertThat(passphrase1.hashCode()).isEqualTo(passphrase1.hashCode());
    assertThat(passphrase2.hashCode()).isEqualTo(passphrase2.hashCode());
    assertThat(passphrase2.hashCode()).isNotEqualTo(passphrase1.hashCode());
  }

  @Test
  void testToString() {
    assertThat(passphrase1.toString()).isEqualTo(
      "Passphrase{value=[redacted], destroyed=false}"
    );
  }

}