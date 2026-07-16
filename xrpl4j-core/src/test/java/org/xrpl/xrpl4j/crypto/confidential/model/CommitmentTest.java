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

  private static final String HEX = Strings.repeat("02", Commitment.LENGTH); // 33 bytes.

  @Test
  void ofUnsignedByteArrayExposesValueAndHex() {
    Commitment commitment = Commitment.of(UnsignedByteArray.fromHex(HEX));
    assertThat(commitment.value().length()).isEqualTo(Commitment.LENGTH);
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
}
