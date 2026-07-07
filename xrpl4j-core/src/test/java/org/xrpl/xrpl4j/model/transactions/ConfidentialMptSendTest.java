package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

/**
 * Unit tests for the field-combination preconditions of {@link ConfidentialMptSend}.
 */
class ConfidentialMptSendTest {

  private static final Address ACCOUNT = Address.of("rJo2Wu7dymuFaL3QgYaEwgAEN3VcgN8e8c");
  private static final Address DESTINATION = Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
  private static final ZkProof ZK_PROOF = ZkProof.of(Strings.repeat("34", 946));

  @Test
  void transactionFlagsReturnsEmptyFlags() {
    ConfidentialMptSend send = baseBuilder().build();

    assertThat(send.transactionFlags()).isEqualTo(send.flags());
    assertThat(send.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  void cannotSendToSelf() {
    assertThatThrownBy(() -> baseBuilder()
      .destination(ACCOUNT)
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Account and Destination must not be the same");
  }

  @Test
  void zkProofMustBeSendLength() {
    // A ZKProof of the wrong length (Schnorr length, not send length) -> temMALFORMED in rippled.
    ZkProof wrongLengthProof = ZkProof.of(Strings.repeat("34", 64)); // 128 hex chars, not 1892.
    assertThatThrownBy(() -> baseBuilder()
      .zkProof(wrongLengthProof)
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("ZKProof must be 946 bytes");
  }

  @Test
  void rejectsMoreThanEightCredentialIds() {
    ImmutableConfidentialMptSend.Builder builder = baseBuilder();
    for (int i = 0; i < 9; i++) {
      builder.addCredentialIds(Hash256.of(Strings.repeat("0", 63) + Integer.toHexString(i)));
    }
    assertThatThrownBy(builder::build)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("CredentialIDs should have less than or equal to 8 items");
  }

  @Test
  void rejectsDuplicateCredentialIds() {
    Hash256 credential = Hash256.of(Strings.repeat("AB", 32));
    assertThatThrownBy(() -> baseBuilder()
      .addCredentialIds(credential)
      .addCredentialIds(credential)
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("CredentialIDs should have unique values");
  }

  @Test
  void validSendDoesNotThrow() {
    ConfidentialMptSend send = baseBuilder()
      .destinationTag(UnsignedInteger.valueOf(42))
      .addCredentialIds(Hash256.of(Strings.repeat("AB", 32)))
      .build();

    assertThat(send.destinationTag()).contains(UnsignedInteger.valueOf(42));
    assertThat(send.credentialIds()).hasSize(1);
  }

  /**
   * A builder pre-populated with the required fields.
   *
   * @return An {@link ImmutableConfidentialMptSend.Builder}.
   */
  private ImmutableConfidentialMptSend.Builder baseBuilder() {
    return ConfidentialMptSend.builder()
      .account(ACCOUNT)
      .fee(XrpCurrencyAmount.ofDrops(135))
      .sequence(UnsignedInteger.valueOf(377))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .destination(DESTINATION)
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000179C3493FFEB0869853DDEC0705800595424710FA7A"))
      .senderEncryptedAmount(EncryptedAmount.of(Strings.repeat("AB", 66)))
      .destinationEncryptedAmount(EncryptedAmount.of(Strings.repeat("CD", 66)))
      .issuerEncryptedAmount(EncryptedAmount.of(Strings.repeat("EF", 66)))
      .zkProof(ZK_PROOF)
      .amountCommitment(Commitment.of(Strings.repeat("02", 33)))
      .balanceCommitment(Commitment.of(Strings.repeat("03", 33)));
  }
}
