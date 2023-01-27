package org.xrpl.xrpl4j.crypto.keys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.Entropy;

import java.util.Arrays;

/**
 * Unit tests for {@link Entropy}.
 */
class EntropyTest {

  @Test
  void newInstance() {
    assertThat(Entropy.newInstance()).isNotNull();
  }

  @Test
  void invalidValue() {
    assertThrows(IllegalArgumentException.class, () -> {
      Entropy.of(new byte[0]);
    });
    assertThrows(IllegalArgumentException.class, () -> {
      Entropy.of(new byte[17]);
    });
  }

  @Test
  void value() {
    final byte[] entropyBytes = new byte[16];
    for (int i = 0; i < entropyBytes.length; i++) {
      entropyBytes[i] = 1;
    }
    Entropy entropy = Entropy.of(entropyBytes);
    assertThat(Arrays.equals(entropyBytes, entropy.value().toByteArray())).isTrue();
    assertThat(entropy.isDestroyed()).isFalse();
    entropy.destroy();
    assertThat(entropy.isDestroyed()).isTrue();
    assertThat(Arrays.equals(entropyBytes, entropy.value().toByteArray())).isFalse();

    entropy.destroy();
    assertThat(entropy.isDestroyed()).isTrue();
    Assertions.assertThat(Arrays.equals(entropyBytes, entropy.value().toByteArray())).isFalse();
  }

  @Test
  void testEquals() {
    final Entropy entropyZero = Entropy.of(new byte[16]);

    final byte[] entropyBytes = new byte[16];
    for (int i = 0; i < entropyBytes.length; i++) {
      entropyBytes[i] = 1;
    }
    Entropy entropyNonZero = Entropy.of(entropyBytes);

    assertThat(entropyNonZero).isEqualTo(entropyNonZero);
    assertThat(entropyNonZero).isNotEqualTo(entropyZero);
    assertThat(entropyZero).isNotEqualTo(entropyBytes);
  }

  @Test
  void testHashCode() {
    final Entropy entropyZero = Entropy.of(new byte[16]);

    final byte[] entropyBytes = new byte[16];
    for (int i = 0; i < entropyBytes.length; i++) {
      entropyBytes[i] = 1;
    }
    Entropy entropyNonZero = Entropy.of(entropyBytes);

    assertThat(entropyNonZero.hashCode()).isEqualTo(entropyNonZero.hashCode());
    assertThat(entropyNonZero.hashCode()).isNotEqualTo(entropyZero.hashCode());
    assertThat(entropyZero.hashCode()).isNotEqualTo(entropyBytes.hashCode());
  }

  @Test
  void testToString() {
    final Entropy entropy = Entropy.of(new byte[16]);
    assertThat(entropy.toString()).isEqualTo("Entropy{value=[redacted], destroyed=false}");
  }
}