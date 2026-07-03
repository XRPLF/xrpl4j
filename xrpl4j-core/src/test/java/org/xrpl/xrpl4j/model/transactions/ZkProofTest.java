package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ZkProof}.
 *
 * <p>{@link ZkProof} enforces only that its value is valid hexadecimal; the required length varies by transaction and
 * is validated by each transaction.
 */
class ZkProofTest {

  @Test
  void constructsProofOfAnyEvenHexLength() {
    // Schnorr proof length (64 bytes).
    ZkProof schnorr = ZkProof.of(Strings.repeat("34", 64));
    assertThat(schnorr.value()).isEqualTo(Strings.repeat("34", 64));

    // A different length is also accepted at the wrapper level.
    ZkProof send = ZkProof.of(Strings.repeat("AB", 946)); // ConfidentialMPTSend proof length (946 bytes).
    assertThat(send.value()).hasSize(946 * 2);
  }

  @Test
  void rejectsNonHex() {
    assertThatThrownBy(() -> ZkProof.of("ZZZZ"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be encoded in hexadecimal");
  }

  @Test
  void rejectsOddLengthHex() {
    // Odd-length strings are not valid hex.
    assertThatThrownBy(() -> ZkProof.of("ABC"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be encoded in hexadecimal");
  }

  @Test
  void toStringReturnsValue() {
    String value = Strings.repeat("34", 64);
    assertThat(ZkProof.of(value).toString()).isEqualTo(value);
  }

  @Test
  void equalsIsCaseInsensitive() {
    assertThat(ZkProof.of(Strings.repeat("ab", 64)))
      .isEqualTo(ZkProof.of(Strings.repeat("AB", 64)));
  }
}
