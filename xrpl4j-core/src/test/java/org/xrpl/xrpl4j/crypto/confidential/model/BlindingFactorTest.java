package org.xrpl.xrpl4j.crypto.confidential.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BlindingFactor}.
 */
class BlindingFactorTest {

  @Test
  void constructsValidBlindingFactor() {
    String hex = Strings.repeat("12", 32); // 64 hex chars = 32 bytes.
    BlindingFactor factor = BlindingFactor.of(hex);
    assertThat(factor.value().length()).isEqualTo(32);
    assertThat(factor.hexValue()).isEqualTo(hex);
  }

  @Test
  void fromBytesRoundTrips() {
    byte[] bytes = new byte[32];
    java.util.Arrays.fill(bytes, (byte) 0x12);
    assertThat(BlindingFactor.fromBytes(bytes).value().toByteArray()).isEqualTo(bytes);
  }

  @Test
  void rejectsTooShort() {
    assertThatThrownBy(() -> BlindingFactor.of(Strings.repeat("12", 31))) // 31 bytes.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 32 bytes");
  }

  @Test
  void rejectsTooLong() {
    assertThatThrownBy(() -> BlindingFactor.of(Strings.repeat("12", 33))) // 33 bytes.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 32 bytes");
  }

  @Test
  void rejectsEmpty() {
    assertThatThrownBy(() -> BlindingFactor.of(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must be 32 bytes");
  }

  @Test
  void equalsIsCaseInsensitive() {
    // fromHex normalizes case, so lower- and upper-case hex produce equal byte values.
    assertThat(BlindingFactor.of(Strings.repeat("ab", 32)))
      .isEqualTo(BlindingFactor.of(Strings.repeat("AB", 32)));
  }

  @Test
  void toStringRedactsSecretValue() {
    // An instance can't tell whether it plays the disclosed (Convert) or secret (Send) role, so toString() fails safe
    // and never renders the raw value.
    assertThat(BlindingFactor.of(Strings.repeat("12", 32)))
      .hasToString("BlindingFactor{value=[redacted], destroyed=false}");
  }

  @Test
  void hexValueThrowsAfterDestroy() {
    // destroy() empties the bytes, so without this guard a destroyed factor would silently serialize as an empty
    // BlindingFactor field (rejected on-ledger as tecBAD_PROOF) instead of failing here.
    BlindingFactor factor = BlindingFactor.of(Strings.repeat("12", 32));
    factor.destroy();

    assertThatThrownBy(factor::hexValue)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("BlindingFactor has been destroyed");
  }

  @Test
  void hexValueIsNotCachedAcrossDestroy() {
    // hexValue() must not be @Value.Lazy: a cached hex String would outlive destroy() and defeat Destroyable.
    BlindingFactor factor = BlindingFactor.of(Strings.repeat("12", 32));
    assertThat(factor.hexValue()).isEqualTo(Strings.repeat("12", 32));

    factor.destroy();

    assertThatThrownBy(factor::hexValue).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void destroyZeroesOutValueAndMarksDestroyed() {
    BlindingFactor factor = BlindingFactor.of(Strings.repeat("12", 32));
    assertThat(factor.isDestroyed()).isFalse();

    factor.destroy();

    assertThat(factor.isDestroyed()).isTrue();
    assertThat(factor.value().isDestroyed()).isTrue();
    assertThat(factor.value().toByteArray()).isEmpty();
  }
}
