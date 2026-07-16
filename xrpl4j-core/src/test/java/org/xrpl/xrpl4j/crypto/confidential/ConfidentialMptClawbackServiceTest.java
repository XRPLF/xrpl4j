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

  private static final KeyPair KEY_PAIR =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("clawback-service")).deriveKeyPair();
  private static final PublicKey PUBLIC_KEY = KEY_PAIR.publicKey();
  private static final PrivateKey PRIVATE_KEY = KEY_PAIR.privateKey();
  private static final Address ACCOUNT = Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
  private static final Address HOLDER = Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w");
  private static final MpTokenIssuanceId ISSUANCE_ID = MpTokenIssuanceId.of(Strings.repeat("0A", 24));
  private static final UnsignedInteger SEQUENCE = UnsignedInteger.valueOf(7);
  private static final UnsignedLong AMOUNT = UnsignedLong.valueOf(100);
  private static final EncryptedAmount CIPHERTEXT = EncryptedAmount.of(Strings.repeat("03", 66));
  private static final ConfidentialMptClawbackContext CONTEXT =
    ConfidentialMptClawbackContext.fromHex(Strings.repeat("AB", 32));
  private static final ConfidentialMptClawbackProof PROOF =
    ConfidentialMptClawbackProof.fromHex(Strings.repeat("CD", ConfidentialMptClawbackProof.EXPECTED_SIZE));

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
    when(contextHashGenerator.generateClawbackContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, HOLDER)).thenReturn(CONTEXT);

    assertThat(service.generateContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, HOLDER)).isSameAs(CONTEXT);
    verify(contextHashGenerator).generateClawbackContext(ACCOUNT, SEQUENCE, ISSUANCE_ID, HOLDER);
  }

  @Test
  void generateProofDelegates() {
    when(proofGenerator.generateProof(CIPHERTEXT, PUBLIC_KEY, AMOUNT, PRIVATE_KEY, CONTEXT)).thenReturn(PROOF);

    assertThat(service.generateProof(CIPHERTEXT, PUBLIC_KEY, AMOUNT, PRIVATE_KEY, CONTEXT)).isSameAs(PROOF);
    verify(proofGenerator).generateProof(CIPHERTEXT, PUBLIC_KEY, AMOUNT, PRIVATE_KEY, CONTEXT);
  }

  @Test
  void verifyProofDelegates() {
    when(proofVerifier.verifyProof(PROOF, CIPHERTEXT, PUBLIC_KEY, AMOUNT, CONTEXT)).thenReturn(true);

    assertThat(service.verifyProof(PROOF, CIPHERTEXT, PUBLIC_KEY, AMOUNT, CONTEXT)).isTrue();
    verify(proofVerifier).verifyProof(PROOF, CIPHERTEXT, PUBLIC_KEY, AMOUNT, CONTEXT);
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
