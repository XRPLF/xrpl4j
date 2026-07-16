package org.xrpl.xrpl4j.crypto.confidential.util.jna;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.BlindingFactor;

/**
 * Unit tests for {@link JnaBlindingFactorGenerator} using a mocked {@link MptCryptoLibrary}. Verifies that the 32-byte
 * native blinding factor is returned as a wire {@link BlindingFactor} and that native errors are surfaced.
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
    when(lib.mpt_generate_blinding_factor(any())).thenAnswer(invocation -> {
      byte[] out = invocation.getArgument(0);
      java.util.Arrays.fill(out, (byte) 0x05);
      return 0;
    });

    BlindingFactor blindingFactor = generator.generate();

    assertThat(blindingFactor.value().length()).isEqualTo(32); // 32-byte scalar.
  }

  @Test
  void generateThrowsOnNativeError() {
    when(lib.mpt_generate_blinding_factor(any())).thenReturn(-1);

    assertThatThrownBy(() -> generator.generate())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("mpt_generate_blinding_factor failed");
  }
}
