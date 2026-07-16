package org.xrpl.xrpl4j.crypto.confidential;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
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
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptSendProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptSendProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.ContextHashGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.PedersenCommitmentGenerator;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link ConfidentialMptSendService}, verifying that each public method delegates to the injected
 * collaborators and that the constructor rejects null collaborators.
 */
class ConfidentialMptSendServiceTest {

  private static final KeyPair KEY_PAIR =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("send-service")).deriveKeyPair();
  private static final PublicKey PUBLIC_KEY = KEY_PAIR.publicKey();
  private static final Address ACCOUNT = Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
  private static final Address DESTINATION = Address.of("rMTi57fNy2UkUb4RcdoUeJm7gjxVQvxzUo");
  private static final MpTokenIssuanceId ISSUANCE_ID = MpTokenIssuanceId.of(Strings.repeat("0A", 24));
  private static final UnsignedInteger SEQUENCE = UnsignedInteger.valueOf(7);
  private static final UnsignedInteger VERSION = UnsignedInteger.valueOf(3);
  private static final UnsignedLong AMOUNT = UnsignedLong.valueOf(100);
  private static final BlindingFactor BLINDING_FACTOR = BlindingFactor.of(Strings.repeat("11", 32));
  private static final Commitment COMMITMENT = Commitment.of(Strings.repeat("02", 33));
  private static final EncryptedAmount CIPHERTEXT = EncryptedAmount.of(Strings.repeat("03", 66));
  private static final ConfidentialMptSendContext CONTEXT =
    ConfidentialMptSendContext.fromHex(Strings.repeat("AB", 32));
  private static final ConfidentialMptSendProof PROOF =
    ConfidentialMptSendProof.fromHex(Strings.repeat("CD", ConfidentialMptSendProof.EXPECTED_SIZE));
  private static final List<MptConfidentialParty> PARTICIPANTS = Arrays.asList(
    MptConfidentialParty.of(PUBLIC_KEY, CIPHERTEXT),
    MptConfidentialParty.of(PUBLIC_KEY, CIPHERTEXT),
    MptConfidentialParty.of(PUBLIC_KEY, CIPHERTEXT)
  );
  private static final PedersenProofParams BALANCE_PARAMS = PedersenProofParams.builder()
    .pedersenCommitment(COMMITMENT.value())
    .amount(UnsignedLong.valueOf(500))
    .encryptedAmount(CIPHERTEXT)
    .blindingFactor(BLINDING_FACTOR)
    .build();

  private ContextHashGenerator contextHashGenerator;
  private ConfidentialMptSendProofGenerator proofGenerator;
  private ConfidentialMptSendProofVerifier proofVerifier;
  private PedersenCommitmentGenerator commitmentGenerator;
  private ConfidentialMptSendService service;

  @BeforeEach
  void setUp() {
    contextHashGenerator = mock(ContextHashGenerator.class);
    proofGenerator = mock(ConfidentialMptSendProofGenerator.class);
    proofVerifier = mock(ConfidentialMptSendProofVerifier.class);
    commitmentGenerator = mock(PedersenCommitmentGenerator.class);
    service = new ConfidentialMptSendService(
      contextHashGenerator, proofGenerator, proofVerifier, commitmentGenerator
    );
  }

  @Test
  void generateContextDelegates() {
    when(contextHashGenerator.generateSendContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, DESTINATION, VERSION))
      .thenReturn(CONTEXT);

    assertThat(service.generateContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, DESTINATION, VERSION)).isSameAs(CONTEXT);
    verify(contextHashGenerator).generateSendContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, DESTINATION, VERSION);
  }

  @Test
  void generatePedersenCommitmentDelegates() {
    when(commitmentGenerator.generateCommitment(AMOUNT, BLINDING_FACTOR)).thenReturn(COMMITMENT);

    assertThat(service.generatePedersenCommitment(AMOUNT, BLINDING_FACTOR)).isSameAs(COMMITMENT);
    verify(commitmentGenerator).generateCommitment(AMOUNT, BLINDING_FACTOR);
  }

  @Test
  void generatePedersenProofParamsBundlesCommitment() {
    when(commitmentGenerator.generateCommitment(AMOUNT, BLINDING_FACTOR)).thenReturn(COMMITMENT);

    PedersenProofParams params = service.generatePedersenProofParams(AMOUNT, CIPHERTEXT, BLINDING_FACTOR);

    assertThat(params.pedersenCommitment()).isEqualTo(COMMITMENT.value());
    assertThat(params.amount()).isEqualTo(AMOUNT);
    assertThat(params.encryptedAmount()).isSameAs(CIPHERTEXT);
    assertThat(params.blindingFactor()).isSameAs(BLINDING_FACTOR);
    verify(commitmentGenerator).generateCommitment(AMOUNT, BLINDING_FACTOR);
  }

  @Test
  void generateProofDelegates() {
    when(proofGenerator.generateProof(KEY_PAIR, AMOUNT, PARTICIPANTS, BLINDING_FACTOR, CONTEXT, COMMITMENT,
      BALANCE_PARAMS)).thenReturn(PROOF);

    assertThat(service.generateProof(KEY_PAIR, AMOUNT, PARTICIPANTS, BLINDING_FACTOR, CONTEXT, COMMITMENT,
      BALANCE_PARAMS)).isSameAs(PROOF);
    verify(proofGenerator).generateProof(KEY_PAIR, AMOUNT, PARTICIPANTS, BLINDING_FACTOR, CONTEXT, COMMITMENT,
      BALANCE_PARAMS);
  }

  @Test
  void verifyProofDelegates() {
    when(proofVerifier.verifyProof(PROOF, PARTICIPANTS, CIPHERTEXT, CONTEXT, COMMITMENT, COMMITMENT))
      .thenReturn(true);

    assertThat(service.verifyProof(PROOF, PARTICIPANTS, CIPHERTEXT, CONTEXT, COMMITMENT, COMMITMENT)).isTrue();
    verify(proofVerifier).verifyProof(PROOF, PARTICIPANTS, CIPHERTEXT, CONTEXT, COMMITMENT, COMMITMENT);
  }

  @Test
  void constructorRejectsNullCollaborators() {
    assertThatThrownBy(() -> new ConfidentialMptSendService(null, proofGenerator, proofVerifier, commitmentGenerator))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("contextHashGenerator");
    assertThatThrownBy(() -> new ConfidentialMptSendService(contextHashGenerator, null, proofVerifier,
      commitmentGenerator)).isInstanceOf(NullPointerException.class).hasMessageContaining("proofGenerator");
    assertThatThrownBy(() -> new ConfidentialMptSendService(contextHashGenerator, proofGenerator, null,
      commitmentGenerator)).isInstanceOf(NullPointerException.class).hasMessageContaining("proofVerifier");
    assertThatThrownBy(() -> new ConfidentialMptSendService(contextHashGenerator, proofGenerator, proofVerifier,
      null)).isInstanceOf(NullPointerException.class).hasMessageContaining("commitmentGenerator");
  }
}
