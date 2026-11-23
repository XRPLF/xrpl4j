package org.xrpl.xrpl4j.crypto.confidential.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link SecretBlindingFactor}, the blinding factor that must never leave the process. See
 * {@link BlindingFactorTest} for the disclosed counterpart that Convert and ConvertBack publish.
 */
class SecretBlindingFactorTest {

  private static final String HEX = Strings.repeat("12", 32); // 64 hex chars = 32 bytes.

  @Test
  void constructsFromHexBytesAndUnsignedByteArray() {
    byte[] bytes = new byte[SecretBlindingFactor.LENGTH];
    java.util.Arrays.fill(bytes, (byte) 0x12);

    assertThat(SecretBlindingFactor.of(HEX).value().toByteArray()).isEqualTo(bytes);
    assertThat(SecretBlindingFactor.fromBytes(bytes).value().toByteArray()).isEqualTo(bytes);
    assertThat(SecretBlindingFactor.of(UnsignedByteArray.fromHex(HEX)).value().length())
      .isEqualTo(SecretBlindingFactor.LENGTH);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 31, 33})
  void rejectsWrongLength(int byteLength) {
    assertThatThrownBy(() -> SecretBlindingFactor.of(Strings.repeat("12", byteLength)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 32 bytes");
  }

  @Test
  void rejectsNullValue() {
    assertThatThrownBy(() -> SecretBlindingFactor.of((UnsignedByteArray) null))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void ofCopiesSoCallerCannotMutate() {
    UnsignedByteArray caller = UnsignedByteArray.fromHex(HEX);
    SecretBlindingFactor factor = SecretBlindingFactor.of(caller);

    caller.destroy();

    assertThat(factor.value().toByteArray()).containsOnly((byte) 0x12);
  }

  @Test
  void fromBytesCopiesSoCallerCanScrub() {
    // JnaBlindingFactorGenerator relies on this: it zeroes the native buffer in a finally after fromBytes returns.
    byte[] bytes = new byte[SecretBlindingFactor.LENGTH];
    java.util.Arrays.fill(bytes, (byte) 0x12);
    SecretBlindingFactor factor = SecretBlindingFactor.fromBytes(bytes);

    java.util.Arrays.fill(bytes, (byte) 0);

    assertThat(factor.value().toByteArray()).containsOnly((byte) 0x12);
  }

  @Test
  void valueReturnsDefensiveCopy() {
    SecretBlindingFactor factor = SecretBlindingFactor.of(HEX);

    factor.value().destroy();

    assertThat(factor.isDestroyed()).isFalse();
    assertThat(factor.value().toByteArray()).containsOnly((byte) 0x12);
  }

  @Test
  void destroyZeroesValueAndIsIdempotent() {
    SecretBlindingFactor factor = SecretBlindingFactor.of(HEX);
    assertThat(factor.isDestroyed()).isFalse();

    factor.destroy();
    factor.destroy(); // compareAndSet makes the second call a no-op rather than re-zeroing.

    assertThat(factor.isDestroyed()).isTrue();
    // value() refuses rather than handing back zeroed bytes, so a caller that forgot to check isDestroyed() fails
    // where the mistake is instead of silently operating on garbage.
    assertThatThrownBy(factor::value)
      .isInstanceOf(IllegalStateException.class).hasMessageContaining("already been destroyed");
  }

  @Test
  void toBlindingFactorDisclosesAnIndependentCopy() {
    SecretBlindingFactor factor = SecretBlindingFactor.of(HEX);

    BlindingFactor disclosed = factor.toBlindingFactor();
    factor.destroy();

    // The disclosed copy outlives the secret, which is what lets Convert/ConvertBack scrub theirs after building.
    assertThat(disclosed.hexValue()).isEqualTo(HEX.toUpperCase());
  }

  @Test
  void toBlindingFactorThrowsAfterDestroy() {
    SecretBlindingFactor factor = SecretBlindingFactor.of(HEX);
    factor.destroy();

    assertThatThrownBy(factor::toBlindingFactor)
      .isInstanceOf(IllegalStateException.class).hasMessageContaining("already been destroyed");
  }

  @Test
  void equalsAndHashCode() {
    SecretBlindingFactor factor = SecretBlindingFactor.of(HEX);
    SecretBlindingFactor sameValue = SecretBlindingFactor.of(HEX.toUpperCase());
    SecretBlindingFactor otherValue = SecretBlindingFactor.of(Strings.repeat("34", 32));

    assertThat(factor).isEqualTo(factor);
    assertThat(factor).isEqualTo(sameValue);
    assertThat(factor).hasSameHashCodeAs(sameValue);
    assertThat(factor).isNotEqualTo(otherValue);
    assertThat(factor).isNotEqualTo(null);
    assertThat(factor).isNotEqualTo(BlindingFactor.of(HEX));
  }

  @Test
  void toStringRedactsValueBeforeAndAfterDestroy() {
    SecretBlindingFactor factor = SecretBlindingFactor.of(HEX);
    assertThat(factor).hasToString("SecretBlindingFactor{value=[redacted], destroyed=false}");

    factor.destroy();

    assertThat(factor).hasToString("SecretBlindingFactor{value=[redacted], destroyed=true}");
  }

  @Test
  void hasNoWireRepresentation() {
    // The point of the type: no hexValue() and no Jackson serializer, so no route to a transaction field. Asserted so
    // that adding either is a deliberate act.
    assertThat(SecretBlindingFactor.class.getDeclaredAnnotations())
      .noneMatch(annotation -> annotation.annotationType().getSimpleName().startsWith("Json"));
    assertThatThrownBy(() -> SecretBlindingFactor.class.getMethod("hexValue"))
      .isInstanceOf(NoSuchMethodException.class);
  }

  @Test
  void doesNotSerializeInsideAContainer() throws Exception {
    // The realistic leak path: a secret reaches Jackson nested in a serializable object. Before the type split
    // PedersenProofParams.blindingFactor() was a disclosed BlindingFactor, whose serializer wrote the hex out.
    PedersenProofParams params = PedersenProofParams.builder()
      .pedersenCommitment(UnsignedByteArray.fromHex(Strings.repeat("02", 33)))
      .amount(UnsignedLong.valueOf(70))
      .encryptedAmount(EncryptedAmount.of(Strings.repeat("03", 66)))
      .blindingFactor(SecretBlindingFactor.of(Strings.repeat("AA", 32)))
      .build();

    assertThat(ObjectMapperFactory.create().writeValueAsString(params))
      .doesNotContain(Strings.repeat("AA", 32));
  }
}
