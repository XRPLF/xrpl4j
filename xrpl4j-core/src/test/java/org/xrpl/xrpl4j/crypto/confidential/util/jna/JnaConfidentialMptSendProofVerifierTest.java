package org.xrpl.xrpl4j.crypto.confidential.util.jna;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.Commitment;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.MptConfidentialParty;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptSendProof;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link JnaConfidentialMptSendProofVerifier} using a mocked {@link MptCryptoLibrary}, verifying that
 * the native return code maps to a boolean and that preconditions are enforced.
 */
class JnaConfidentialMptSendProofVerifierTest {

  private static final PublicKey PUBLIC_KEY =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("send-proof-verify")).deriveKeyPair().publicKey();
  private static final EncryptedAmount CIPHERTEXT = EncryptedAmount.of(Strings.repeat("03", 66));
  private static final Commitment AMOUNT_COMMITMENT = Commitment.of(Strings.repeat("02", 33));
  private static final Commitment BALANCE_COMMITMENT = Commitment.of(Strings.repeat("04", 33));
  private static final ConfidentialMptSendProof PROOF =
    ConfidentialMptSendProof.fromHex(Strings.repeat("CD", ConfidentialMptSendProof.EXPECTED_SIZE));
  private static final ConfidentialMptSendContext CONTEXT =
    ConfidentialMptSendContext.fromHex(Strings.repeat("AB", 32));
  private static final List<MptConfidentialParty> PARTICIPANTS = Arrays.asList(
    MptConfidentialParty.of(PUBLIC_KEY, CIPHERTEXT),
    MptConfidentialParty.of(PUBLIC_KEY, CIPHERTEXT),
    MptConfidentialParty.of(PUBLIC_KEY, CIPHERTEXT)
  );

  private MptCryptoLibrary lib;
  private JnaConfidentialMptSendProofVerifier verifier;

  @BeforeEach
  void setUp() {
    lib = mock(MptCryptoLibrary.class);
    verifier = new JnaConfidentialMptSendProofVerifier(lib);
  }

  @Test
  void returnsTrueWhenNativeReturnsZero() {
    when(lib.mpt_verify_send_proof(any(), any(), anyByte(), any(), any(), any(), any())).thenReturn(0);
    assertThat(
      verifier.verifyProof(PROOF, PARTICIPANTS, CIPHERTEXT, CONTEXT, AMOUNT_COMMITMENT, BALANCE_COMMITMENT)
    ).isTrue();
  }

  @Test
  void returnsFalseWhenNativeReturnsNonZero() {
    when(lib.mpt_verify_send_proof(any(), any(), anyByte(), any(), any(), any(), any())).thenReturn(-1);
    assertThat(
      verifier.verifyProof(PROOF, PARTICIPANTS, CIPHERTEXT, CONTEXT, AMOUNT_COMMITMENT, BALANCE_COMMITMENT)
    ).isFalse();
  }

  @Test
  void rejectsWrongParticipantCount() {
    assertThatThrownBy(() -> verifier.verifyProof(
      PROOF, Collections.singletonList(MptConfidentialParty.of(PUBLIC_KEY, CIPHERTEXT)),
      CIPHERTEXT, CONTEXT, AMOUNT_COMMITMENT, BALANCE_COMMITMENT
    )).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("participants must contain");
  }

  @Test
  void rejectsNullArguments() {
    assertThatThrownBy(() -> verifier.verifyProof(
      null, PARTICIPANTS, CIPHERTEXT, CONTEXT, AMOUNT_COMMITMENT, BALANCE_COMMITMENT
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("proof");
    assertThatThrownBy(() -> verifier.verifyProof(
      PROOF, null, CIPHERTEXT, CONTEXT, AMOUNT_COMMITMENT, BALANCE_COMMITMENT
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("participants");
    assertThatThrownBy(() -> verifier.verifyProof(
      PROOF, PARTICIPANTS, null, CONTEXT, AMOUNT_COMMITMENT, BALANCE_COMMITMENT
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("senderSpendingCiphertext");
    assertThatThrownBy(() -> verifier.verifyProof(
      PROOF, PARTICIPANTS, CIPHERTEXT, null, AMOUNT_COMMITMENT, BALANCE_COMMITMENT
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("context");
    assertThatThrownBy(() -> verifier.verifyProof(
      PROOF, PARTICIPANTS, CIPHERTEXT, CONTEXT, null, BALANCE_COMMITMENT
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("amountCommitment");
    assertThatThrownBy(() -> verifier.verifyProof(
      PROOF, PARTICIPANTS, CIPHERTEXT, CONTEXT, AMOUNT_COMMITMENT, null
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("balanceCommitment");
  }
}
