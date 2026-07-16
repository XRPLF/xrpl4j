package org.xrpl.xrpl4j.crypto.confidential;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptConvertProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptConvertProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.ContextHashGenerator;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

/**
 * Unit tests for {@link ConfidentialMptConvertService}, verifying that each public method delegates to the injected
 * collaborators and that the constructor rejects null collaborators.
 */
class ConfidentialMptConvertServiceTest {

  private static final KeyPair KEY_PAIR =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("convert-service")).deriveKeyPair();
  private static final PublicKey PUBLIC_KEY = KEY_PAIR.publicKey();
  private static final Address ACCOUNT = Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
  private static final MpTokenIssuanceId ISSUANCE_ID = MpTokenIssuanceId.of(Strings.repeat("0A", 24));
  private static final UnsignedInteger SEQUENCE = UnsignedInteger.valueOf(7);
  private static final ConfidentialMptConvertContext CONTEXT =
    ConfidentialMptConvertContext.fromHex(Strings.repeat("AB", 32));
  private static final ConfidentialMptConvertProof PROOF =
    ConfidentialMptConvertProof.fromHex(Strings.repeat("CD", 64));

  private ContextHashGenerator contextHashGenerator;
  private ConfidentialMptConvertProofGenerator proofGenerator;
  private ConfidentialMptConvertProofVerifier proofVerifier;
  private ConfidentialMptConvertService service;

  @BeforeEach
  void setUp() {
    contextHashGenerator = mock(ContextHashGenerator.class);
    proofGenerator = mock(ConfidentialMptConvertProofGenerator.class);
    proofVerifier = mock(ConfidentialMptConvertProofVerifier.class);
    service = new ConfidentialMptConvertService(contextHashGenerator, proofGenerator, proofVerifier);
  }

  @Test
  void generateContextDelegates() {
    when(contextHashGenerator.generateConvertContext(ACCOUNT, SEQUENCE, ISSUANCE_ID)).thenReturn(CONTEXT);

    assertThat(service.generateContext(ACCOUNT, SEQUENCE, ISSUANCE_ID)).isSameAs(CONTEXT);
    verify(contextHashGenerator).generateConvertContext(ACCOUNT, SEQUENCE, ISSUANCE_ID);
  }

  @Test
  void generateProofDelegates() {
    when(proofGenerator.generateProof(KEY_PAIR, CONTEXT)).thenReturn(PROOF);

    assertThat(service.generateProof(KEY_PAIR, CONTEXT)).isSameAs(PROOF);
    verify(proofGenerator).generateProof(KEY_PAIR, CONTEXT);
  }

  @Test
  void verifyProofDelegates() {
    when(proofVerifier.verifyProof(PROOF, PUBLIC_KEY, CONTEXT)).thenReturn(true);

    assertThat(service.verifyProof(PROOF, PUBLIC_KEY, CONTEXT)).isTrue();
    verify(proofVerifier).verifyProof(PROOF, PUBLIC_KEY, CONTEXT);
  }

  @Test
  void constructorRejectsNullCollaborators() {
    assertThatThrownBy(() -> new ConfidentialMptConvertService(null, proofGenerator, proofVerifier))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("contextHashGenerator");
    assertThatThrownBy(() -> new ConfidentialMptConvertService(contextHashGenerator, null, proofVerifier))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("proofGenerator");
    assertThatThrownBy(() -> new ConfidentialMptConvertService(contextHashGenerator, proofGenerator, null))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("proofVerifier");
  }
}
