package org.xrpl.xrpl4j.crypto.confidential.util.jna;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.Commitment;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.MptConfidentialParty;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptSendProof;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link JnaConfidentialMptSendProofGenerator} using a mocked {@link MptCryptoLibrary}, verifying
 * key-type/size/participant preconditions and error handling without loading the native mpt-crypto library.
 */
class JnaConfidentialMptSendProofGeneratorTest {

  private static final KeyPair SECP_KEY_PAIR =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("send-proof-gen")).deriveKeyPair();
  private static final KeyPair ED_KEY_PAIR =
    Seed.ed25519SeedFromPassphrase(Passphrase.of("send-proof-gen")).deriveKeyPair();
  private static final PublicKey PUBLIC_KEY = SECP_KEY_PAIR.publicKey();
  private static final UnsignedLong AMOUNT = UnsignedLong.valueOf(100);
  private static final BlindingFactor BLINDING_FACTOR = BlindingFactor.of(Strings.repeat("11", 32));
  private static final Commitment AMOUNT_COMMITMENT = Commitment.of(Strings.repeat("02", 33));
  private static final EncryptedAmount CIPHERTEXT = EncryptedAmount.of(Strings.repeat("03", 66));
  private static final ConfidentialMptSendContext CONTEXT =
    ConfidentialMptSendContext.fromHex(Strings.repeat("AB", 32));
  private static final PedersenProofParams BALANCE_PARAMS = PedersenProofParams.builder()
    .pedersenCommitment(Commitment.of(Strings.repeat("02", 33)).value())
    .amount(UnsignedLong.valueOf(500))
    .encryptedAmount(CIPHERTEXT)
    .blindingFactor(BLINDING_FACTOR)
    .build();
  private static final List<MptConfidentialParty> PARTICIPANTS = Arrays.asList(
    MptConfidentialParty.of(PUBLIC_KEY, CIPHERTEXT),
    MptConfidentialParty.of(PUBLIC_KEY, CIPHERTEXT),
    MptConfidentialParty.of(PUBLIC_KEY, CIPHERTEXT)
  );

  private MptCryptoLibrary lib;
  private JnaConfidentialMptSendProofGenerator generator;

  @BeforeEach
  void setUp() {
    lib = mock(MptCryptoLibrary.class);
    generator = new JnaConfidentialMptSendProofGenerator(lib);
  }

  @Test
  void generateProofReturnsNativeProof() {
    when(lib.mpt_get_confidential_send_proof(
      any(), any(), anyLong(), any(), anyLong(), any(), any(), any(), any(), any(), any()
    )).thenReturn(0);

    ConfidentialMptSendProof proof = generator.generateProof(
      SECP_KEY_PAIR, AMOUNT, PARTICIPANTS, BLINDING_FACTOR, CONTEXT, AMOUNT_COMMITMENT, BALANCE_PARAMS
    );

    assertThat(proof.value().length()).isEqualTo(ConfidentialMptSendProof.EXPECTED_SIZE);
  }

  @Test
  void generateProofRejectsNonSecp256k1KeyPair() {
    assertThatThrownBy(() -> generator.generateProof(
      ED_KEY_PAIR, AMOUNT, PARTICIPANTS, BLINDING_FACTOR, CONTEXT, AMOUNT_COMMITMENT, BALANCE_PARAMS
    )).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("SECP256K1");
  }

  @Test
  void generateProofRejectsWrongParticipantCount() {
    assertThatThrownBy(() -> generator.generateProof(
      SECP_KEY_PAIR, AMOUNT, Collections.singletonList(MptConfidentialParty.of(PUBLIC_KEY, CIPHERTEXT)),
      BLINDING_FACTOR, CONTEXT, AMOUNT_COMMITMENT, BALANCE_PARAMS
    )).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("participants must contain");
  }

  @Test
  void generateProofThrowsOnNativeError() {
    when(lib.mpt_get_confidential_send_proof(
      any(), any(), anyLong(), any(), anyLong(), any(), any(), any(), any(), any(), any()
    )).thenReturn(-1);

    assertThatThrownBy(() -> generator.generateProof(
      SECP_KEY_PAIR, AMOUNT, PARTICIPANTS, BLINDING_FACTOR, CONTEXT, AMOUNT_COMMITMENT, BALANCE_PARAMS
    )).isInstanceOf(IllegalStateException.class).hasMessageContaining("mpt_get_confidential_send_proof failed");
  }

  @Test
  void generateProofRejectsNullArguments() {
    assertThatThrownBy(() -> generator.generateProof(
      null, AMOUNT, PARTICIPANTS, BLINDING_FACTOR, CONTEXT, AMOUNT_COMMITMENT, BALANCE_PARAMS
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("senderKeyPair");
    assertThatThrownBy(() -> generator.generateProof(
      SECP_KEY_PAIR, null, PARTICIPANTS, BLINDING_FACTOR, CONTEXT, AMOUNT_COMMITMENT, BALANCE_PARAMS
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("amount");
    assertThatThrownBy(() -> generator.generateProof(
      SECP_KEY_PAIR, AMOUNT, null, BLINDING_FACTOR, CONTEXT, AMOUNT_COMMITMENT, BALANCE_PARAMS
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("participants");
    assertThatThrownBy(() -> generator.generateProof(
      SECP_KEY_PAIR, AMOUNT, PARTICIPANTS, null, CONTEXT, AMOUNT_COMMITMENT, BALANCE_PARAMS
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("txBlindingFactor");
    assertThatThrownBy(() -> generator.generateProof(
      SECP_KEY_PAIR, AMOUNT, PARTICIPANTS, BLINDING_FACTOR, null, AMOUNT_COMMITMENT, BALANCE_PARAMS
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("context");
    assertThatThrownBy(() -> generator.generateProof(
      SECP_KEY_PAIR, AMOUNT, PARTICIPANTS, BLINDING_FACTOR, CONTEXT, null, BALANCE_PARAMS
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("amountCommitment");
    assertThatThrownBy(() -> generator.generateProof(
      SECP_KEY_PAIR, AMOUNT, PARTICIPANTS, BLINDING_FACTOR, CONTEXT, AMOUNT_COMMITMENT, null
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("balanceParams");
  }
}
