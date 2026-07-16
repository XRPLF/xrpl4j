package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.Commitment;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertBackProof;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

/**
 * Unit tests for the {@code temMALFORMED} preconditions of {@link ConfidentialMptConvertBack}.
 */
class ConfidentialMptConvertBackTest {

  private static final Address ACCOUNT = Address.of("rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c");
  // A valid 816-byte (1632 hex character) ConvertBack ZKProof.
  private static final ConfidentialMptConvertBackProof ZK_PROOF =
    ConfidentialMptConvertBackProof.fromHex(Strings.repeat("34", 816));

  @Test
  void transactionFlagsReturnsEmptyFlags() {
    ConfidentialMptConvertBack convertBack = baseBuilder().build();

    assertThat(convertBack.transactionFlags()).isEqualTo(convertBack.flags());
    assertThat(convertBack.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  void zkProofMustBeConvertBackLength() {
    // A ConfidentialMptConvertBackProof enforces its own 816-byte length at construction.
    assertThatThrownBy(() -> ConfidentialMptConvertBackProof.fromHex(Strings.repeat("34", 946))) // 946 bytes, not 816.
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("ConfidentialMptConvertBackProof must be 816 bytes");
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
  void validConvertBackDoesNotThrow() {
    ConfidentialMptConvertBack convertBack = baseBuilder()
      .auditorEncryptedAmount(EncryptedAmount.of(Strings.repeat("11", 66)))
      .build();

    assertThat(convertBack.auditorEncryptedAmount()).isPresent();
    assertThat(convertBack.zkProof().value().length()).isEqualTo(ConfidentialMptConvertBackProof.EXPECTED_SIZE);
  }

  /**
   * A builder pre-populated with the required fields.
   *
   * @return An {@link ImmutableConfidentialMptConvertBack.Builder}.
   */
  private ImmutableConfidentialMptConvertBack.Builder baseBuilder() {
    return ConfidentialMptConvertBack.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(135))
      .sequence(UnsignedInteger.valueOf(377))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000179C3493FFEB0869853DDEC0705800595424710FA7A"))
      .mptAmount(MpTokenNumericAmount.of(1000))
      .holderEncryptedAmount(EncryptedAmount.of(Strings.repeat("AB", 66)))
      .issuerEncryptedAmount(EncryptedAmount.of(Strings.repeat("CD", 66)))
      .blindingFactor(BlindingFactor.of(Strings.repeat("EF", 32)))
      .balanceCommitment(Commitment.of(Strings.repeat("02", 33)))
      .zkProof(ZK_PROOF);
  }
}
