package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptClawbackProof;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

/**
 * Unit tests for the {@code temMALFORMED} / {@code temBAD_AMOUNT} preconditions of {@link ConfidentialMptClawback}.
 */
class ConfidentialMptClawbackTest {

  private static final Address ACCOUNT = Address.of("rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c");
  private static final Address HOLDER = Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
  private static final ConfidentialMptClawbackProof ZK_PROOF =
    ConfidentialMptClawbackProof.fromHex(Strings.repeat("34", 64));

  @Test
  void transactionFlagsReturnsEmptyFlags() {
    ConfidentialMptClawback clawback = baseBuilder().build();

    assertThat(clawback.transactionFlags()).isEqualTo(clawback.flags());
    assertThat(clawback.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  void cannotClawbackFromSelf() {
    assertThatThrownBy(() -> baseBuilder()
      .holder(ACCOUNT)
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Account and Holder must not be the same");
  }

  @Test
  void zeroMptAmountIsRejected() {
    assertThatThrownBy(() -> baseBuilder()
      .mptAmount(MpTokenNumericAmount.of(0))
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("MPTAmount must not be zero");
  }

  @Test
  void mptAmountExceedingMaxIsRejected() {
    UnsignedLong tooLarge = UnsignedLong.valueOf(0x7FFF_FFFF_FFFF_FFFFL).plus(UnsignedLong.ONE);
    assertThatThrownBy(() -> baseBuilder()
      .mptAmount(MpTokenNumericAmount.of(tooLarge))
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("MPTAmount must not exceed the maximum allowable supply");
  }

  @Test
  void zkProofMustBeClawbackLength() {
    // A ConfidentialMptClawbackProof enforces its own 64-byte length at construction.
    assertThatThrownBy(() -> ConfidentialMptClawbackProof.fromHex(Strings.repeat("34", 32))) // 32 bytes, not 64.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("ConfidentialMptClawbackProof must be 64 bytes");
  }

  @Test
  void validClawbackDoesNotThrow() {
    ConfidentialMptClawback clawback = baseBuilder().build();

    assertThat(clawback.holder()).isEqualTo(HOLDER);
    assertThat(clawback.zkProof().value().length()).isEqualTo(64); // 64 bytes.
  }

  /**
   * A builder pre-populated with the required fields.
   *
   * @return An {@link ImmutableConfidentialMptClawback.Builder}.
   */
  private ImmutableConfidentialMptClawback.Builder baseBuilder() {
    return ConfidentialMptClawback.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(135))
      .sequence(UnsignedInteger.valueOf(377))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000179C3493FFEB0869853DDEC0705800595424710FA7A"))
      .holder(HOLDER)
      .mptAmount(MpTokenNumericAmount.of(1000))
      .zkProof(ZK_PROOF);
  }
}
