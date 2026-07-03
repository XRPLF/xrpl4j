package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EncryptedAmount}.
 */
class EncryptedAmountTest {

  private static final String VALID = Strings.repeat("AB", 66); // 132 hex chars = 66 bytes.

  @Test
  void constructsValidEncryptedAmount() {
    EncryptedAmount amount = EncryptedAmount.of(VALID);
    assertThat(amount.value()).isEqualTo(VALID);
    assertThat(amount.toString()).isEqualTo(VALID);
  }

  @Test
  void rejectsTooShort() {
    assertThatThrownBy(() -> EncryptedAmount.of(Strings.repeat("AB", 65))) // 130 hex chars.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 66 bytes");
  }

  @Test
  void rejectsTooLong() {
    assertThatThrownBy(() -> EncryptedAmount.of(Strings.repeat("AB", 67))) // 134 hex chars.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 66 bytes");
  }

  @Test
  void rejectsEmpty() {
    assertThatThrownBy(() -> EncryptedAmount.of(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 66 bytes");
  }

  @Test
  void rejectsNonHex() {
    assertThatThrownBy(() -> EncryptedAmount.of(Strings.repeat("ZZ", 66))) // 132 chars, not hex.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be encoded in hexadecimal");
  }

  @Test
  void equalsIsCaseInsensitive() {
    assertThat(EncryptedAmount.of(Strings.repeat("ab", 66)))
      .isEqualTo(EncryptedAmount.of(Strings.repeat("AB", 66)));
  }
}
