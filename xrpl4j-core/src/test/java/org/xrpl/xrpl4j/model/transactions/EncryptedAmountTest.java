package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;

/**
 * Unit tests for {@link EncryptedAmount}.
 */
class EncryptedAmountTest {

  private static final String VALID = Strings.repeat("AB", 66); // 132 hex chars = 66 bytes.

  @Test
  void constructsValidEncryptedAmount() {
    EncryptedAmount amount = EncryptedAmount.of(VALID);
    assertThat(amount.value().length()).isEqualTo(66);
    assertThat(amount.hexValue()).isEqualTo(VALID);
  }

  @Test
  void fromBytesRoundTrips() {
    byte[] bytes = new byte[66];
    assertThat(EncryptedAmount.fromBytes(bytes).value().length()).isEqualTo(66);
  }

  @Test
  void rejectsTooShort() {
    assertThatThrownBy(() -> EncryptedAmount.of(Strings.repeat("AB", 65))) // 65 bytes.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 66 bytes");
  }

  @Test
  void rejectsTooLong() {
    assertThatThrownBy(() -> EncryptedAmount.of(Strings.repeat("AB", 67))) // 67 bytes.
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
  void equalsIsCaseInsensitive() {
    // fromHex normalizes case, so lower- and upper-case hex produce equal byte values.
    assertThat(EncryptedAmount.of(Strings.repeat("ab", 66)))
      .isEqualTo(EncryptedAmount.of(Strings.repeat("AB", 66)));
  }
}
