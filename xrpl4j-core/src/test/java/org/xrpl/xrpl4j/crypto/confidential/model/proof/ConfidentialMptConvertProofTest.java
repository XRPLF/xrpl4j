package org.xrpl.xrpl4j.crypto.confidential.model.proof;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Unit tests for {@link ConfidentialMptConvertProof}, which wraps the 64-byte compact Schnorr proof-of-knowledge used
 * to register a holder's ElGamal key during a ConfidentialMPTConvert.
 */
class ConfidentialMptConvertProofTest {

  private static final String PROOF_HEX = Strings.repeat("CD", 64); // 64 bytes.

  @Test
  void ofExposesValueAndHex() {
    ConfidentialMptConvertProof proof = ConfidentialMptConvertProof.of(UnsignedByteArray.fromHex(PROOF_HEX));
    assertThat(proof.value().length()).isEqualTo(64);
    assertThat(proof.hexValue()).isEqualTo(PROOF_HEX);
  }

  @Test
  void fromHexRoundTrips() {
    assertThat(ConfidentialMptConvertProof.fromHex(PROOF_HEX).hexValue()).isEqualTo(PROOF_HEX);
  }

  @Test
  void rejectsWrongLength() {
    assertThatThrownBy(() -> ConfidentialMptConvertProof.fromHex(Strings.repeat("CD", 63)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("ConfidentialMptConvertProof must be");
  }
}
