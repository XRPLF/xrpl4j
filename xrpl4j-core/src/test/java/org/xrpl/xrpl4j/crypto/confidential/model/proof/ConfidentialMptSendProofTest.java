package org.xrpl.xrpl4j.crypto.confidential.model.proof;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Unit tests for {@link ConfidentialMptSendProof}, which wraps the 946-byte compact AND-composed sigma proof plus
 * aggregated Bulletproof used by a ConfidentialMPTSend.
 */
class ConfidentialMptSendProofTest {

  private static final String PROOF_HEX = Strings.repeat("CD", 946); // 946 bytes.

  @Test
  void ofExposesValueAndHex() {
    ConfidentialMptSendProof proof = ConfidentialMptSendProof.of(UnsignedByteArray.fromHex(PROOF_HEX));
    assertThat(proof.value().length()).isEqualTo(946);
    assertThat(proof.hexValue()).isEqualTo(PROOF_HEX);
  }

  @Test
  void fromHexRoundTrips() {
    assertThat(ConfidentialMptSendProof.fromHex(PROOF_HEX).hexValue()).isEqualTo(PROOF_HEX);
  }

  @Test
  void rejectsWrongLength() {
    assertThatThrownBy(() -> ConfidentialMptSendProof.fromHex(Strings.repeat("CD", 945)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("ConfidentialMptSendProof must be");
  }
}
