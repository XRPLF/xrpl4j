package org.xrpl.xrpl4j.crypto.confidential.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import javax.security.auth.Destroyable;

/**
 * Unit tests for {@link BlindingFactor}, the disclosed blinding factor that Convert and ConvertBack publish on the
 * ledger. See {@link SecretBlindingFactorTest} for its secret counterpart.
 */
class BlindingFactorTest {

  private static final String HEX = Strings.repeat("12", 32); // 64 hex chars = 32 bytes.

  @Test
  void constructsFromHexBytesAndUnsignedByteArray() {
    byte[] bytes = new byte[BlindingFactor.LENGTH];
    java.util.Arrays.fill(bytes, (byte) 0x12);

    assertThat(BlindingFactor.of(HEX).hexValue()).isEqualTo(HEX);
    assertThat(BlindingFactor.fromBytes(bytes).value().toByteArray()).isEqualTo(bytes);
    assertThat(BlindingFactor.of(UnsignedByteArray.fromHex(HEX)).value().length()).isEqualTo(BlindingFactor.LENGTH);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 31, 33})
  void rejectsWrongLength(int byteLength) {
    assertThatThrownBy(() -> BlindingFactor.of(Strings.repeat("12", byteLength)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 32 bytes");
  }

  @Test
  void rejectsNullValue() {
    assertThatThrownBy(() -> BlindingFactor.of((UnsignedByteArray) null))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void ofCopiesSoCallerCannotMutate() {
    UnsignedByteArray caller = UnsignedByteArray.fromHex(HEX);
    BlindingFactor factor = BlindingFactor.of(caller);

    caller.destroy();

    assertThat(factor.hexValue()).isEqualTo(HEX.toUpperCase());
  }

  @Test
  void valueReturnsDefensiveCopy() {
    BlindingFactor factor = BlindingFactor.of(HEX);

    factor.value().destroy();

    assertThat(factor.hexValue()).isEqualTo(HEX.toUpperCase());
  }

  @Test
  void equalsAndHashCode() {
    BlindingFactor factor = BlindingFactor.of(HEX);
    // fromHex normalizes case, so lower- and upper-case hex produce equal byte values.
    BlindingFactor sameValue = BlindingFactor.of(HEX.toUpperCase());
    BlindingFactor otherValue = BlindingFactor.of(Strings.repeat("34", 32));

    assertThat(factor).isEqualTo(factor);
    assertThat(factor).isEqualTo(sameValue);
    assertThat(factor).hasSameHashCodeAs(sameValue);
    assertThat(factor).isNotEqualTo(otherValue);
    assertThat(factor).isNotEqualTo(null);
    assertThat(factor).isNotEqualTo("not a blinding factor");
  }

  @Test
  void toStringRendersValue() {
    // An on-ledger field, so there is nothing to hide -- contrast SecretBlindingFactorTest.
    assertThat(BlindingFactor.of(HEX)).hasToString("BlindingFactor{value=" + HEX + "}");
  }

  @Test
  void isNotDestroyable() {
    // Zeroing a factor a pending transaction still carries would serialize an empty field, rejected as tecBAD_PROOF.
    assertThat(BlindingFactor.of(HEX)).isNotInstanceOf(Destroyable.class);
  }
}
