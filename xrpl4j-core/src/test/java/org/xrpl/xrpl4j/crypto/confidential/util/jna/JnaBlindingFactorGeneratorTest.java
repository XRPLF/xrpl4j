package org.xrpl.xrpl4j.crypto.confidential.util.jna;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.SecretBlindingFactor;

import java.util.Arrays;

/**
 * Unit tests for {@link JnaBlindingFactorGenerator} using a mocked {@link MptCryptoLibrary}. Verifies that the 32-byte
 * native scalar is returned as whichever kind the caller asked for -- a disclosed {@link BlindingFactor} or a
 * {@link SecretBlindingFactor} -- and that native errors are surfaced.
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
  void generateReturnsWireBlindingFactor() {
    byte[] expected = new byte[32]; // 32-byte scalar.
    Arrays.fill(expected, (byte) 0x05);
    when(lib.mpt_generate_blinding_factor(any())).thenAnswer(invocation -> {
      byte[] out = invocation.getArgument(0);
      System.arraycopy(expected, 0, out, 0, expected.length);
      return 0;
    });

    BlindingFactor blindingFactor = generator.generate();

    assertThat(blindingFactor.value().toByteArray()).isEqualTo(expected);
  }

  @Test
  void generateSecretBlindingFactorReturnsSecretBlindingFactor() {
    byte[] expected = new byte[32]; // 32-byte scalar.
    Arrays.fill(expected, (byte) 0x07);
    when(lib.mpt_generate_blinding_factor(any())).thenAnswer(invocation -> {
      byte[] out = invocation.getArgument(0);
      System.arraycopy(expected, 0, out, 0, expected.length);
      return 0;
    });

    SecretBlindingFactor blindingFactor = generator.generateSecretBlindingFactor();

    // Survives the finally that scrubs the native buffer, i.e. fromBytes really copied.
    assertThat(blindingFactor.value().toByteArray()).isEqualTo(expected);
    assertThat(blindingFactor.isDestroyed()).isFalse();
  }

  @Test
  void generateThrowsOnNativeError() {
    when(lib.mpt_generate_blinding_factor(any())).thenReturn(-1);

    assertThatThrownBy(() -> generator.generate())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("mpt_generate_blinding_factor failed");
  }

  @Test
  void generateSecretBlindingFactorThrowsOnNativeError() {
    when(lib.mpt_generate_blinding_factor(any())).thenReturn(-1);

    assertThatThrownBy(() -> generator.generateSecretBlindingFactor())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("mpt_generate_blinding_factor failed");
  }
}
