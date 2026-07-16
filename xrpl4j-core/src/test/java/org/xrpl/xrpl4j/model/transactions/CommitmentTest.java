package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.Commitment;

/**
 * Unit tests for {@link Commitment}.
 */
class CommitmentTest {

  private static final String VALID = Strings.repeat("AB", 33); // 66 hex chars = 33 bytes.

  @Test
  void constructsValidCommitment() {
    Commitment commitment = Commitment.of(VALID);
    assertThat(commitment.value().length()).isEqualTo(33);
    assertThat(commitment.hexValue()).isEqualTo(VALID);
  }

  @Test
  void fromBytesRoundTrips() {
    byte[] bytes = new byte[33];
    assertThat(Commitment.fromBytes(bytes).value().length()).isEqualTo(33);
  }

  @Test
  void rejectsTooShort() {
    assertThatThrownBy(() -> Commitment.of(Strings.repeat("AB", 32))) // 32 bytes.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 33 bytes");
  }

  @Test
  void rejectsTooLong() {
    assertThatThrownBy(() -> Commitment.of(Strings.repeat("AB", 34))) // 34 bytes.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 33 bytes");
  }

  @Test
  void rejectsEmpty() {
    assertThatThrownBy(() -> Commitment.of(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 33 bytes");
  }

  @Test
  void equalsIsCaseInsensitive() {
    // fromHex normalizes case, so lower- and upper-case hex produce equal byte values.
    assertThat(Commitment.of(Strings.repeat("ab", 33)))
      .isEqualTo(Commitment.of(Strings.repeat("AB", 33)));
  }
}
