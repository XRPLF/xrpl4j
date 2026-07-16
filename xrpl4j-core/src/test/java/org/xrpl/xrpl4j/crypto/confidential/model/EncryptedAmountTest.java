package org.xrpl.xrpl4j.crypto.confidential.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Unit tests for {@link EncryptedAmount}, the 66-byte ElGamal ciphertext (C1 || C2) of two compressed secp256k1 points.
 */
class EncryptedAmountTest {

  private static final String HEX = Strings.repeat("03", 66); // 66 bytes.

  @Test
  void ofUnsignedByteArrayExposesValueAndHex() {
    EncryptedAmount amount = EncryptedAmount.of(UnsignedByteArray.fromHex(HEX));
    assertThat(amount.value().length()).isEqualTo(66); // 66 bytes.
    assertThat(amount.hexValue()).isEqualTo(HEX);
  }

  @Test
  void ofHexAndFromBytesAgree() {
    EncryptedAmount fromHex = EncryptedAmount.of(HEX);
    EncryptedAmount fromBytes = EncryptedAmount.fromBytes(UnsignedByteArray.fromHex(HEX).toByteArray());
    assertThat(fromHex).isEqualTo(fromBytes);
    assertThat(fromBytes.hexValue()).isEqualTo(HEX);
  }

  @Test
  void rejectsWrongLength() {
    assertThatThrownBy(() -> EncryptedAmount.of(Strings.repeat("03", 65)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("EncryptedAmount must be");
  }
}
