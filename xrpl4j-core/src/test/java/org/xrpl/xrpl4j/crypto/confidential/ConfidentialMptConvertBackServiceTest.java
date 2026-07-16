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
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertBackProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptConvertBackProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptConvertBackProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.ContextHashGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.PedersenCommitmentGenerator;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

/**
 * Unit tests for {@link ConfidentialMptConvertBackService}, verifying that each public method delegates to the injected
 * collaborators and that the constructor rejects null collaborators.
 */
class ConfidentialMptConvertBackServiceTest {

  private static final UnsignedLong AMOUNT = UnsignedLong.valueOf(100);
  private static final Commitment COMMITMENT = Commitment.of(Strings.repeat("02", 33));
  private static final EncryptedAmount CIPHERTEXT = EncryptedAmount.of(Strings.repeat("03", 66));
  private static final ConfidentialMptConvertBackContext CONTEXT =
    ConfidentialMptConvertBackContext.fromHex(Strings.repeat("AB", 32));

  private ContextHashGenerator contextHashGenerator;
  private ConfidentialMptConvertBackProofGenerator proofGenerator;
  private ConfidentialMptConvertBackProofVerifier proofVerifier;
  private PedersenCommitmentGenerator commitmentGenerator;
  private ConfidentialMptConvertBackService service;

  @BeforeEach
  void setUp() {
    contextHashGenerator = mock(ContextHashGenerator.class);
    proofGenerator = mock(ConfidentialMptConvertBackProofGenerator.class);
    proofVerifier = mock(ConfidentialMptConvertBackProofVerifier.class);
    commitmentGenerator = mock(PedersenCommitmentGenerator.class);
    service = new ConfidentialMptConvertBackService(
      contextHashGenerator, proofGenerator, proofVerifier, commitmentGenerator
    );
  }

  @Test
  void generateContextDelegates() {
    Address account = Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
    MpTokenIssuanceId issuanceId = MpTokenIssuanceId.of(Strings.repeat("0A", 24));
    UnsignedInteger sequence = UnsignedInteger.valueOf(7);
    UnsignedInteger version = UnsignedInteger.valueOf(3);
    when(contextHashGenerator.generateConvertBackContext(account, sequence, issuanceId, version))
      .thenReturn(CONTEXT);

    assertThat(service.generateContext(account, sequence, issuanceId, version)).isSameAs(CONTEXT);
    verify(contextHashGenerator).generateConvertBackContext(account, sequence, issuanceId, version);
  }

  @Test
  void generatePedersenProofParamsBundlesCommitment() {
    BlindingFactor blindingFactor = BlindingFactor.of(Strings.repeat("11", 32));
    when(commitmentGenerator.generateCommitment(AMOUNT, blindingFactor)).thenReturn(COMMITMENT);

    PedersenProofParams params = service.generatePedersenProofParams(AMOUNT, CIPHERTEXT, blindingFactor);

    assertThat(params.pedersenCommitment()).isEqualTo(COMMITMENT.value());
    assertThat(params.amount()).isEqualTo(AMOUNT);
    assertThat(params.encryptedAmount()).isSameAs(CIPHERTEXT);
    assertThat(params.blindingFactor()).isSameAs(blindingFactor);
    verify(commitmentGenerator).generateCommitment(AMOUNT, blindingFactor);
  }

  @Test
  void generateProofDelegates() {
    KeyPair keyPair = Seed.secp256k1SeedFromPassphrase(Passphrase.of("convert-back-service")).deriveKeyPair();
    ConfidentialMptConvertBackProof proof =
      ConfidentialMptConvertBackProof.fromHex(Strings.repeat("CD", 816)); // 816 bytes.
    PedersenProofParams balanceParams = PedersenProofParams.builder()
      .pedersenCommitment(COMMITMENT.value())
      .amount(UnsignedLong.valueOf(500))
      .encryptedAmount(CIPHERTEXT)
      .blindingFactor(BlindingFactor.of(Strings.repeat("11", 32)))
      .build();
    when(proofGenerator.generateProof(keyPair, AMOUNT, CONTEXT, balanceParams)).thenReturn(proof);

    assertThat(service.generateProof(keyPair, AMOUNT, CONTEXT, balanceParams)).isSameAs(proof);
    verify(proofGenerator).generateProof(keyPair, AMOUNT, CONTEXT, balanceParams);
  }

  @Test
  void verifyProofDelegates() {
    PublicKey publicKey =
      Seed.secp256k1SeedFromPassphrase(Passphrase.of("convert-back-service")).deriveKeyPair().publicKey();
    ConfidentialMptConvertBackProof proof =
      ConfidentialMptConvertBackProof.fromHex(Strings.repeat("CD", 816)); // 816 bytes.
    when(proofVerifier.verifyProof(proof, publicKey, CIPHERTEXT, COMMITMENT, AMOUNT, CONTEXT)).thenReturn(true);

    assertThat(service.verifyProof(proof, publicKey, CIPHERTEXT, COMMITMENT, AMOUNT, CONTEXT)).isTrue();
    verify(proofVerifier).verifyProof(proof, publicKey, CIPHERTEXT, COMMITMENT, AMOUNT, CONTEXT);
  }

  @Test
  void constructorRejectsNullCollaborators() {
    assertThatThrownBy(() -> new ConfidentialMptConvertBackService(
      null, proofGenerator, proofVerifier, commitmentGenerator
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("contextHashGenerator");
    assertThatThrownBy(() -> new ConfidentialMptConvertBackService(
      contextHashGenerator, null, proofVerifier, commitmentGenerator
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("proofGenerator");
    assertThatThrownBy(() -> new ConfidentialMptConvertBackService(
      contextHashGenerator, proofGenerator, null, commitmentGenerator
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("proofVerifier");
    assertThatThrownBy(() -> new ConfidentialMptConvertBackService(
      contextHashGenerator, proofGenerator, proofVerifier, null
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("commitmentGenerator");
  }
}
