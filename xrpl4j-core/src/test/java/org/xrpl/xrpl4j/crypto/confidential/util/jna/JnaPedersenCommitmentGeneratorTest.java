package org.xrpl.xrpl4j.crypto.confidential.util.jna;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.Commitment;

/**
 * Unit tests for {@link JnaPedersenCommitmentGenerator} using a mocked {@link MptCryptoLibrary}. Verifies that the
 * 33-byte native commitment is returned as a {@link Commitment} and that error handling and null guards are enforced.
 */
class JnaPedersenCommitmentGeneratorTest {

  private static final UnsignedLong AMOUNT = UnsignedLong.valueOf(1000);
  private static final BlindingFactor BLINDING_FACTOR = BlindingFactor.of(Strings.repeat("11", 32));

  private MptCryptoLibrary lib;
  private JnaPedersenCommitmentGenerator generator;

  @BeforeEach
  void setUp() {
    lib = mock(MptCryptoLibrary.class);
    generator = new JnaPedersenCommitmentGenerator(lib);
  }

  @Test
  void generateCommitmentReturnsCommitment() {
    when(lib.mpt_get_pedersen_commitment(anyLong(), any(), any())).thenAnswer(invocation -> {
      byte[] out = invocation.getArgument(2);
      java.util.Arrays.fill(out, (byte) 0x02);
      return 0;
    });

    Commitment commitment = generator.generateCommitment(AMOUNT, BLINDING_FACTOR);

    assertThat(commitment.value().length()).isEqualTo(Commitment.LENGTH);
  }

  @Test
  void generateCommitmentThrowsOnNativeError() {
    when(lib.mpt_get_pedersen_commitment(anyLong(), any(), any())).thenReturn(-1);

    assertThatThrownBy(() -> generator.generateCommitment(AMOUNT, BLINDING_FACTOR))
      .isInstanceOf(IllegalStateException.class).hasMessageContaining("mpt_get_pedersen_commitment failed");
  }

  @Test
  void generateCommitmentRejectsNullArguments() {
    assertThatThrownBy(() -> generator.generateCommitment(null, BLINDING_FACTOR))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("amount");
    assertThatThrownBy(() -> generator.generateCommitment(AMOUNT, null))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("blindingFactor");
  }
}
