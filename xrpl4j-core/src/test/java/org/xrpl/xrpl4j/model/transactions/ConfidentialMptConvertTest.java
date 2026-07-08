package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

/**
 * Unit tests for the field-combination preconditions of {@link ConfidentialMptConvert}.
 */
class ConfidentialMptConvertTest {

  // A valid 33-byte (66 hex character) compressed EC public key.
  private static final PublicKey HOLDER_ENCRYPTION_KEY = PublicKey.fromBase16EncodedPublicKey(
    "028D7500BFCD792B487E4E51664037AB543E76CEBACF0E7E17AD4B83057E1F2B30"
  );
  // A valid 64-byte (128 hex character) Schnorr proof of knowledge.
  private static final ZkProof ZK_PROOF = ZkProof.of(Strings.repeat("34", 64));

  @Test
  void transactionFlagsReturnsEmptyFlags() {
    ConfidentialMptConvert convert = baseBuilder().build();

    assertThat(convert.transactionFlags()).isEqualTo(convert.flags());
    assertThat(convert.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  void holderEncryptionKeyWithoutZkProofIsRejected() {
    // HolderEncryptionKey present but ZKProof absent -> temMALFORMED in rippled.
    assertThatThrownBy(() -> baseBuilder()
      .holderEncryptionKey(HOLDER_ENCRYPTION_KEY)
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("HolderEncryptionKey and ZKProof must both be present");
  }

  @Test
  void zkProofWithoutHolderEncryptionKeyIsRejected() {
    // ZKProof present but HolderEncryptionKey absent -> temMALFORMED in rippled.
    assertThatThrownBy(() -> baseBuilder()
      .zkProof(ZK_PROOF)
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("HolderEncryptionKey and ZKProof must both be present");
  }

  @Test
  void zkProofMustBeSchnorrLength() {
    // HolderEncryptionKey present with a ZKProof of the wrong length -> temMALFORMED in rippled.
    ZkProof wrongLengthProof = ZkProof.of(Strings.repeat("34", 32)); // 64 hex chars (32 bytes), not 128.
    assertThatThrownBy(() -> baseBuilder()
      .holderEncryptionKey(HOLDER_ENCRYPTION_KEY)
      .zkProof(wrongLengthProof)
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("ZKProof must be 64 bytes");
  }

  @Test
  void validConvertWithKeyAndProofDoesNotThrow() {
    ConfidentialMptConvert convert = baseBuilder()
      .holderEncryptionKey(HOLDER_ENCRYPTION_KEY)
      .zkProof(ZK_PROOF)
      .build();

    assertThat(convert.holderEncryptionKey()).contains(HOLDER_ENCRYPTION_KEY);
    assertThat(convert.zkProof()).contains(ZK_PROOF);
  }

  @Test
  void validConvertWithoutKeyAndProofDoesNotThrow() {
    ConfidentialMptConvert convert = baseBuilder().build();

    assertThat(convert.holderEncryptionKey()).isEmpty();
    assertThat(convert.zkProof()).isEmpty();
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
  void zeroMptAmountIsAllowed() {
    ConfidentialMptConvert convert = baseBuilder()
      .mptAmount(MpTokenNumericAmount.of(0))
      .build();

    assertThat(convert.mptAmount().value()).isEqualTo(UnsignedLong.ZERO);
  }

  /**
   * A builder pre-populated with the required fields (and no {@code HolderEncryptionKey}/{@code ZKProof}).
   *
   * @return An {@link ImmutableConfidentialMptConvert.Builder}.
   */
  private ImmutableConfidentialMptConvert.Builder baseBuilder() {
    return ConfidentialMptConvert.builder()
      .account(Address.of("rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c"))
      .fee(XrpCurrencyAmount.ofDrops(135))
      .sequence(UnsignedInteger.valueOf(377))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000179C3493FFEB0869853DDEC0705800595424710FA7A"))
      .mptAmount(MpTokenNumericAmount.of(1000))
      .holderEncryptedAmount(EncryptedAmount.of(Strings.repeat("AB", 66)))
      .issuerEncryptedAmount(EncryptedAmount.of(Strings.repeat("CD", 66)))
      .blindingFactor(BlindingFactor.of(Strings.repeat("12", 32)));
  }
}
