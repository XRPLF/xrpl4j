package org.xrpl.xrpl4j.crypto.confidential.model.proof;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Unit tests for {@link ConfidentialMptClawbackProof}, which wraps the 64-byte compact Schnorr (equality-plaintext)
 * sigma proof used by a ConfidentialMPTClawback.
 */
class ConfidentialMptClawbackProofTest {

  private static final int EXPECTED_SIZE = 64;

  private static final String PROOF_HEX =
    Strings.repeat("CD", EXPECTED_SIZE); // 64 bytes.

  @Test
  void ofExposesValueAndHex() {
    ConfidentialMptClawbackProof proof = ConfidentialMptClawbackProof.of(UnsignedByteArray.fromHex(PROOF_HEX));
    assertThat(proof.value().length()).isEqualTo(EXPECTED_SIZE);
    assertThat(proof.hexValue()).isEqualTo(PROOF_HEX);
  }

  @Test
  void fromHexRoundTrips() {
    assertThat(ConfidentialMptClawbackProof.fromHex(PROOF_HEX).hexValue()).isEqualTo(PROOF_HEX);
  }

  @Test
  void rejectsWrongLength() {
    assertThatThrownBy(() -> ConfidentialMptClawbackProof.fromHex(Strings.repeat("CD", 63)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("ConfidentialMptClawbackProof must be");
  }
}
