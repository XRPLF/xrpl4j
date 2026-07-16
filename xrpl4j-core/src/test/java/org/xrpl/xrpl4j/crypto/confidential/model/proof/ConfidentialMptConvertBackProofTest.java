package org.xrpl.xrpl4j.crypto.confidential.model.proof;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Unit tests for {@link ConfidentialMptConvertBackProof}, which wraps the 816-byte compact AND-composed sigma proof
 * plus single Bulletproof used by a ConfidentialMPTConvertBack.
 */
class ConfidentialMptConvertBackProofTest {

  private static final int EXPECTED_SIZE = 816;

  private static final String PROOF_HEX =
    Strings.repeat("CD", EXPECTED_SIZE); // 816 bytes.

  @Test
  void ofExposesValueAndHex() {
    ConfidentialMptConvertBackProof proof = ConfidentialMptConvertBackProof.of(UnsignedByteArray.fromHex(PROOF_HEX));
    assertThat(proof.value().length()).isEqualTo(EXPECTED_SIZE);
    assertThat(proof.hexValue()).isEqualTo(PROOF_HEX);
  }

  @Test
  void fromHexRoundTrips() {
    assertThat(ConfidentialMptConvertBackProof.fromHex(PROOF_HEX).hexValue()).isEqualTo(PROOF_HEX);
  }

  @Test
  void rejectsWrongLength() {
    assertThatThrownBy(() -> ConfidentialMptConvertBackProof.fromHex(Strings.repeat("CD", 815)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("ConfidentialMptConvertBackProof must be");
  }
}
