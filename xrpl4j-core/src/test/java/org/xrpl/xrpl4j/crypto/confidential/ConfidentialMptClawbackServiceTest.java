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
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptClawbackProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptClawbackProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptClawbackProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.ContextHashGenerator;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

/**
 * Unit tests for {@link ConfidentialMptClawbackService}, verifying that each public method delegates to the injected
 * collaborators and that the constructor rejects null collaborators.
 */
class ConfidentialMptClawbackServiceTest {

  private static final ConfidentialMptClawbackContext CONTEXT =
    ConfidentialMptClawbackContext.fromHex(Strings.repeat("AB", 32));

  private ContextHashGenerator contextHashGenerator;
  private ConfidentialMptClawbackProofGenerator proofGenerator;
  private ConfidentialMptClawbackProofVerifier proofVerifier;
  private ConfidentialMptClawbackService service;

  @BeforeEach
  void setUp() {
    contextHashGenerator = mock(ContextHashGenerator.class);
    proofGenerator = mock(ConfidentialMptClawbackProofGenerator.class);
    proofVerifier = mock(ConfidentialMptClawbackProofVerifier.class);
    service = new ConfidentialMptClawbackService(contextHashGenerator, proofGenerator, proofVerifier);
  }

  @Test
  void generateContextDelegates() {
    Address account = Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
    Address holder = Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w");
    MpTokenIssuanceId issuanceId = MpTokenIssuanceId.of(Strings.repeat("0A", 24));
    UnsignedInteger sequence = UnsignedInteger.valueOf(7);
    when(contextHashGenerator.generateClawbackContext(account, sequence, issuanceId, holder)).thenReturn(CONTEXT);

    assertThat(service.generateContext(account, sequence, issuanceId, holder)).isSameAs(CONTEXT);
    verify(contextHashGenerator).generateClawbackContext(account, sequence, issuanceId, holder);
  }

  @Test
  void generateProofDelegates() {
    KeyPair keyPair = Seed.secp256k1SeedFromPassphrase(Passphrase.of("clawback-service")).deriveKeyPair();
    PublicKey publicKey = keyPair.publicKey();
    PrivateKey privateKey = keyPair.privateKey();
    UnsignedLong amount = UnsignedLong.valueOf(100);
    EncryptedAmount ciphertext = EncryptedAmount.of(Strings.repeat("03", 66));
    ConfidentialMptClawbackProof proof = ConfidentialMptClawbackProof.fromHex(Strings.repeat("CD", 64)); // 64 bytes.
    when(proofGenerator.generateProof(ciphertext, publicKey, amount, privateKey, CONTEXT)).thenReturn(proof);

    assertThat(service.generateProof(ciphertext, publicKey, amount, privateKey, CONTEXT)).isSameAs(proof);
    verify(proofGenerator).generateProof(ciphertext, publicKey, amount, privateKey, CONTEXT);
  }

  @Test
  void verifyProofDelegates() {
    PublicKey publicKey =
      Seed.secp256k1SeedFromPassphrase(Passphrase.of("clawback-service")).deriveKeyPair().publicKey();
    UnsignedLong amount = UnsignedLong.valueOf(100);
    EncryptedAmount ciphertext = EncryptedAmount.of(Strings.repeat("03", 66));
    ConfidentialMptClawbackProof proof = ConfidentialMptClawbackProof.fromHex(Strings.repeat("CD", 64)); // 64 bytes.
    when(proofVerifier.verifyProof(proof, ciphertext, publicKey, amount, CONTEXT)).thenReturn(true);

    assertThat(service.verifyProof(proof, ciphertext, publicKey, amount, CONTEXT)).isTrue();
    verify(proofVerifier).verifyProof(proof, ciphertext, publicKey, amount, CONTEXT);
  }

  @Test
  void constructorRejectsNullCollaborators() {
    assertThatThrownBy(() -> new ConfidentialMptClawbackService(null, proofGenerator, proofVerifier))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("contextHashGenerator");
    assertThatThrownBy(() -> new ConfidentialMptClawbackService(contextHashGenerator, null, proofVerifier))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("proofGenerator");
    assertThatThrownBy(() -> new ConfidentialMptClawbackService(contextHashGenerator, proofGenerator, null))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("proofVerifier");
  }
}
