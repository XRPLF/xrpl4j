package org.xrpl.xrpl4j.crypto.confidential.util.jna;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.SecretBlindingFactor;

import java.util.Arrays;

/**
 * Unit tests for {@link JnaBlindingFactorGenerator} using a mocked {@link MptCryptoLibrary}. Verifies that the 32-byte
 * native scalar is returned as a {@link SecretBlindingFactor} and that native errors are surfaced.
 */
class JnaBlindingFactorGeneratorTest {

  private MptCryptoLibrary lib;
  private JnaBlindingFactorGenerator generator;

  @BeforeEach
  void setUp() {
    lib = mock(MptCryptoLibrary.class);
    generator = new JnaBlindingFactorGenerator(lib);
  }

  @Test
  void generateReturnsSecretBlindingFactor() {
    byte[] expected = new byte[SecretBlindingFactor.LENGTH];
    Arrays.fill(expected, (byte) 0x05);
    stubNativeToWrite(expected);

    SecretBlindingFactor blindingFactor = generator.generate();

    // Survives the finally that scrubs the native buffer, i.e. fromBytes really copied.
    assertThat(blindingFactor.value().toByteArray()).isEqualTo(expected);
    assertThat(blindingFactor.isDestroyed()).isFalse();
  }

  @Test
  void generateReturnsADistinctInstanceEachCall() {
    // Callers destroy the factors they receive, so a shared instance would leave later callers holding a dead one.
    byte[] expected = new byte[SecretBlindingFactor.LENGTH];
    Arrays.fill(expected, (byte) 0x05);
    stubNativeToWrite(expected);

    SecretBlindingFactor first = generator.generate();
    SecretBlindingFactor second = generator.generate();
    first.destroy();

    assertThat(second.isDestroyed()).isFalse();
    assertThat(second.value().toByteArray()).isEqualTo(expected);
  }

  @Test
  void generateThrowsOnNativeError() {
    when(lib.mpt_generate_blinding_factor(any())).thenReturn(-1);

    assertThatThrownBy(() -> generator.generate())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("mpt_generate_blinding_factor failed");
  }

  @Test
  void rejectsNullLibrary() {
    assertThatThrownBy(() -> new JnaBlindingFactorGenerator(null))
      .isInstanceOf(NullPointerException.class);
  }

  private void stubNativeToWrite(final byte[] scalar) {
    when(lib.mpt_generate_blinding_factor(any())).thenAnswer(invocation -> {
      byte[] out = invocation.getArgument(0);
      System.arraycopy(scalar, 0, out, 0, scalar.length);
      return 0;
    });
  }
}
