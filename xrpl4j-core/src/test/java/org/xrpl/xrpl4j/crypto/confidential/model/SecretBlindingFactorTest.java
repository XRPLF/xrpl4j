package org.xrpl.xrpl4j.crypto.confidential.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link SecretBlindingFactor}, the blinding factor that must never leave the process. See
 * {@link BlindingFactorTest} for the disclosed counterpart that Convert and ConvertBack publish.
 */
class SecretBlindingFactorTest {

  @Test
  void constructsValidBlindingFactor() {
    SecretBlindingFactor factor = SecretBlindingFactor.of(Strings.repeat("12", 32)); // 64 hex chars = 32 bytes.
    assertThat(factor.value().length()).isEqualTo(32);
  }

  @Test
  void fromBytesRoundTrips() {
    byte[] bytes = new byte[32];
    java.util.Arrays.fill(bytes, (byte) 0x12);
    assertThat(SecretBlindingFactor.fromBytes(bytes).value().toByteArray()).isEqualTo(bytes);
  }

  @Test
  void fromBytesCopiesSoCallerCanScrub() {
    // JnaBlindingFactorGenerator relies on this: it zeroes the native buffer in a finally after fromBytes returns.
    byte[] bytes = new byte[32];
    java.util.Arrays.fill(bytes, (byte) 0x12);
    SecretBlindingFactor factor = SecretBlindingFactor.fromBytes(bytes);

    java.util.Arrays.fill(bytes, (byte) 0);

    assertThat(factor.value().toByteArray()).containsOnly((byte) 0x12);
  }

  @Test
  void rejectsTooShort() {
    assertThatThrownBy(() -> SecretBlindingFactor.of(Strings.repeat("12", 31))) // 31 bytes.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 32 bytes");
  }

  @Test
  void rejectsTooLong() {
    assertThatThrownBy(() -> SecretBlindingFactor.of(Strings.repeat("12", 33))) // 33 bytes.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 32 bytes");
  }

  @Test
  void rejectsEmpty() {
    assertThatThrownBy(() -> SecretBlindingFactor.of(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 32 bytes");
  }

  @Test
  void toStringRedactsValue() {
    // A secret factor must not reach a log.
    assertThat(SecretBlindingFactor.of(Strings.repeat("12", 32)))
      .hasToString("SecretBlindingFactor{value=[redacted], destroyed=false}");
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
  void doesNotSerializeInsideAContainer() {
    // The realistic leak path: a secret reaches Jackson nested in a serializable object. Before the type split
    // PedersenProofParams.blindingFactor() was a disclosed BlindingFactor, whose serializer wrote the hex out.
    PedersenProofParams params = PedersenProofParams.builder()
      .pedersenCommitment(UnsignedByteArray.fromHex(Strings.repeat("02", 33)))
      .amount(UnsignedLong.valueOf(70))
      .encryptedAmount(EncryptedAmount.of(Strings.repeat("03", 66)))
      .blindingFactor(SecretBlindingFactor.of(Strings.repeat("AA", 32)))
      .build();

    assertThatCode(() -> assertThat(ObjectMapperFactory.create().writeValueAsString(params))
      .doesNotContain(Strings.repeat("AA", 32))).doesNotThrowAnyException();
  }

  @Test
  void destroyZeroesOutValueAndMarksDestroyed() {
    SecretBlindingFactor factor = SecretBlindingFactor.of(Strings.repeat("12", 32));
    assertThat(factor.isDestroyed()).isFalse();

    factor.destroy();

    assertThat(factor.isDestroyed()).isTrue();
    assertThat(factor.value().isDestroyed()).isTrue();
    assertThat(factor.value().toByteArray()).isEmpty();
  }

  @Test
  void toStringStillRedactsAfterDestroy() {
    SecretBlindingFactor factor = SecretBlindingFactor.of(Strings.repeat("12", 32));
    factor.destroy();

    assertThat(factor).hasToString("SecretBlindingFactor{value=[redacted], destroyed=true}");
  }
}
