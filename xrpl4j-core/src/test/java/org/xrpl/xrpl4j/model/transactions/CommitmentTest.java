package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Commitment}.
 */
class CommitmentTest {

  private static final String VALID = Strings.repeat("AB", 33); // 66 hex chars = 33 bytes.

  @Test
  void constructsValidCommitment() {
    Commitment commitment = Commitment.of(VALID);
    assertThat(commitment.value()).isEqualTo(VALID);
    assertThat(commitment.toString()).isEqualTo(VALID);
  }

  @Test
  void rejectsTooShort() {
    assertThatThrownBy(() -> Commitment.of(Strings.repeat("AB", 32))) // 64 hex chars.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 33 bytes");
  }

  @Test
  void rejectsTooLong() {
    assertThatThrownBy(() -> Commitment.of(Strings.repeat("AB", 34))) // 68 hex chars.
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
  void rejectsNonHex() {
    assertThatThrownBy(() -> Commitment.of(Strings.repeat("ZZ", 33))) // 66 chars, not hex.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be encoded in hexadecimal");
  }

  @Test
  void equalsIsCaseInsensitive() {
    assertThat(Commitment.of(Strings.repeat("ab", 33)))
      .isEqualTo(Commitment.of(Strings.repeat("AB", 33)));
  }
}
