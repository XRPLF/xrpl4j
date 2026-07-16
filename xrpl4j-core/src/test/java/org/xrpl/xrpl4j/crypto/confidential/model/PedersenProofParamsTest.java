package org.xrpl.xrpl4j.crypto.confidential.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PedersenProofParams}, which mirrors the C struct {@code mpt_pedersen_proof_params} used to
 * generate a Pedersen linkage proof.
 */
class PedersenProofParamsTest {

  private static final Commitment COMMITMENT = Commitment.of(Strings.repeat("02", 33));
  private static final UnsignedLong AMOUNT = UnsignedLong.valueOf(500);
  private static final EncryptedAmount ENCRYPTED_AMOUNT = EncryptedAmount.of(Strings.repeat("03", 66));
  private static final BlindingFactor BLINDING_FACTOR = BlindingFactor.of(Strings.repeat("11", 32));

  @Test
  void builderExposesFields() {
    PedersenProofParams params = PedersenProofParams.builder()
      .pedersenCommitment(COMMITMENT.value())
      .amount(AMOUNT)
      .encryptedAmount(ENCRYPTED_AMOUNT)
      .blindingFactor(BLINDING_FACTOR)
      .build();

    assertThat(params.pedersenCommitment()).isEqualTo(COMMITMENT.value());
    assertThat(params.amount()).isEqualTo(AMOUNT);
    assertThat(params.encryptedAmount()).isEqualTo(ENCRYPTED_AMOUNT);
    assertThat(params.blindingFactor()).isEqualTo(BLINDING_FACTOR);
  }
}
