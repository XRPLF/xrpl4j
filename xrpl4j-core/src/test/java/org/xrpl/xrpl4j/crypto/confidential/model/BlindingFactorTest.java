package org.xrpl.xrpl4j.crypto.confidential.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BlindingFactor}.
 */
class BlindingFactorTest {

  @Test
  void constructsValidBlindingFactor() {
    String hex = Strings.repeat("12", 32); // 64 hex chars = 32 bytes.
    BlindingFactor factor = BlindingFactor.of(hex);
    assertThat(factor.value().length()).isEqualTo(32);
    assertThat(factor.hexValue()).isEqualTo(hex);
  }

  @Test
  void fromBytesRoundTrips() {
    byte[] bytes = new byte[32];
    java.util.Arrays.fill(bytes, (byte) 0x12);
    assertThat(BlindingFactor.fromBytes(bytes).value().toByteArray()).isEqualTo(bytes);
  }

  @Test
  void rejectsTooShort() {
    assertThatThrownBy(() -> BlindingFactor.of(Strings.repeat("12", 31))) // 31 bytes.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 32 bytes");
  }

  @Test
  void rejectsTooLong() {
    assertThatThrownBy(() -> BlindingFactor.of(Strings.repeat("12", 33))) // 33 bytes.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 32 bytes");
  }

  @Test
  void rejectsEmpty() {
    assertThatThrownBy(() -> BlindingFactor.of(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 32 bytes");
  }

  @Test
  void equalsIsCaseInsensitive() {
    // fromHex normalizes case, so lower- and upper-case hex produce equal byte values.
    assertThat(BlindingFactor.of(Strings.repeat("ab", 32)))
      .isEqualTo(BlindingFactor.of(Strings.repeat("AB", 32)));
  }
}
