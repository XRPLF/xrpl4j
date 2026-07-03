package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BlindingFactor}.
 */
class BlindingFactorTest {

  private static final String VALID = Strings.repeat("12", 32); // 64 hex chars = 32 bytes.

  @Test
  void constructsValidBlindingFactor() {
    BlindingFactor factor = BlindingFactor.of(VALID);
    assertThat(factor.value()).isEqualTo(VALID);
    assertThat(factor.toString()).isEqualTo(VALID);
  }

  @Test
  void rejectsTooShort() {
    assertThatThrownBy(() -> BlindingFactor.of(Strings.repeat("12", 31))) // 62 hex chars.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 32 bytes");
  }

  @Test
  void rejectsTooLong() {
    assertThatThrownBy(() -> BlindingFactor.of(Strings.repeat("12", 33))) // 66 hex chars.
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
  void rejectsNonHex() {
    assertThatThrownBy(() -> BlindingFactor.of(Strings.repeat("ZZ", 32))) // 64 chars, not hex.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be encoded in hexadecimal");
  }

  @Test
  void equalsIsCaseInsensitive() {
    assertThat(BlindingFactor.of(Strings.repeat("ab", 32)))
      .isEqualTo(BlindingFactor.of(Strings.repeat("AB", 32)));
  }
}
