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
  private static final UnsignedLong AMOUNT = UnsignedLong.valueOf(100);
  private static final BlindingFactor BLINDING_FACTOR = BlindingFactor.of(Strings.repeat("11", 32));
  private static final Commitment COMMITMENT = Commitment.of(Strings.repeat("02", 33));
  private static final EncryptedAmount CIPHERTEXT = EncryptedAmount.of(Strings.repeat("03", 66));
  private static final ConfidentialMptSendContext CONTEXT =
    ConfidentialMptSendContext.fromHex(Strings.repeat("AB", 32));
  private static final List<MptConfidentialParty> PARTICIPANTS = Arrays.asList(
    MptConfidentialParty.of(KEY_PAIR.publicKey(), CIPHERTEXT),
    MptConfidentialParty.of(KEY_PAIR.publicKey(), CIPHERTEXT),
    MptConfidentialParty.of(KEY_PAIR.publicKey(), CIPHERTEXT)
  );

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
    Address account = Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
    Address destination = Address.of("rMTi57fNy2UkUb4RcdoUeJm7gjxVQvxzUo");
    MpTokenIssuanceId issuanceId = MpTokenIssuanceId.of(Strings.repeat("0A", 24));
    UnsignedInteger sequence = UnsignedInteger.valueOf(7);
    UnsignedInteger version = UnsignedInteger.valueOf(3);
    when(contextHashGenerator.generateSendContext(account, sequence, issuanceId, destination, version))
      .thenReturn(CONTEXT);

    assertThat(service.generateContext(account, sequence, issuanceId, destination, version)).isSameAs(CONTEXT);
    verify(contextHashGenerator).generateSendContext(account, sequence, issuanceId, destination, version);
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
    ConfidentialMptSendProof proof = ConfidentialMptSendProof.fromHex(Strings.repeat("CD", 946)); // 946 bytes.
    PedersenProofParams balanceParams = PedersenProofParams.builder()
      .pedersenCommitment(COMMITMENT.value())
      .amount(UnsignedLong.valueOf(500))
      .encryptedAmount(CIPHERTEXT)
      .blindingFactor(BLINDING_FACTOR)
      .build();
    when(proofGenerator.generateProof(KEY_PAIR, AMOUNT, PARTICIPANTS, BLINDING_FACTOR, CONTEXT, COMMITMENT,
      balanceParams)).thenReturn(proof);

    assertThat(service.generateProof(KEY_PAIR, AMOUNT, PARTICIPANTS, BLINDING_FACTOR, CONTEXT, COMMITMENT,
      balanceParams)).isSameAs(proof);
    verify(proofGenerator).generateProof(KEY_PAIR, AMOUNT, PARTICIPANTS, BLINDING_FACTOR, CONTEXT, COMMITMENT,
      balanceParams);
  }

  @Test
  void verifyProofDelegates() {
    ConfidentialMptSendProof proof = ConfidentialMptSendProof.fromHex(Strings.repeat("CD", 946)); // 946 bytes.
    when(proofVerifier.verifyProof(proof, PARTICIPANTS, CIPHERTEXT, CONTEXT, COMMITMENT, COMMITMENT))
      .thenReturn(true);

    assertThat(service.verifyProof(proof, PARTICIPANTS, CIPHERTEXT, CONTEXT, COMMITMENT, COMMITMENT)).isTrue();
    verify(proofVerifier).verifyProof(proof, PARTICIPANTS, CIPHERTEXT, CONTEXT, COMMITMENT, COMMITMENT);
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
