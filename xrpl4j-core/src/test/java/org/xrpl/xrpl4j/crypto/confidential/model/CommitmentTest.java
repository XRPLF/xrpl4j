package org.xrpl.xrpl4j.crypto.confidential.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Unit tests for {@link Commitment}, the 33-byte compressed secp256k1 Pedersen commitment point.
 */
class CommitmentTest {

  private static final String HEX = Strings.repeat("02", 33); // 33 bytes.

  @Test
  void ofUnsignedByteArrayExposesValueAndHex() {
    Commitment commitment = Commitment.of(UnsignedByteArray.fromHex(HEX));
    assertThat(commitment.value().length()).isEqualTo(33); // 33 bytes.
    assertThat(commitment.hexValue()).isEqualTo(HEX);
  }

  @Test
  void ofHexAndFromBytesAgree() {
    Commitment fromHex = Commitment.of(HEX);
    Commitment fromBytes = Commitment.fromBytes(UnsignedByteArray.fromHex(HEX).toByteArray());
    assertThat(fromHex).isEqualTo(fromBytes);
    assertThat(fromBytes.hexValue()).isEqualTo(HEX);
  }

  @Test
  void rejectsWrongLength() {
    assertThatThrownBy(() -> Commitment.of(Strings.repeat("02", 32)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Commitment must be");
  }

  @Test
  void rejectsTooLong() {
    assertThatThrownBy(() -> Commitment.of(Strings.repeat("02", 34)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Commitment must be");
  }

  @Test
  void rejectsEmpty() {
    assertThatThrownBy(() -> Commitment.of(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Commitment must be");
  }

  @Test
  void equalsIsCaseInsensitive() {
    // fromHex normalizes case, so lower- and upper-case hex produce equal byte values.
    assertThat(Commitment.of(Strings.repeat("ab", 33)))
      .isEqualTo(Commitment.of(Strings.repeat("AB", 33)));
  }
}
