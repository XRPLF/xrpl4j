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
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptClawbackProof;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;

/**
 * Unit tests for {@link JnaConfidentialMptClawbackProofVerifier} using a mocked {@link MptCryptoLibrary}, verifying
 * that the native return code maps to a boolean and that preconditions are enforced.
 */
class JnaConfidentialMptClawbackProofVerifierTest {

  private static final PublicKey SECP_PUBLIC_KEY =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("clawback-verify")).deriveKeyPair().publicKey();
  private static final PublicKey ED_PUBLIC_KEY =
    Seed.ed25519SeedFromPassphrase(Passphrase.of("clawback-verify")).deriveKeyPair().publicKey();
  private static final UnsignedLong AMOUNT = UnsignedLong.valueOf(100);
  private static final EncryptedAmount CIPHERTEXT = EncryptedAmount.of(Strings.repeat("03", 66));
  private static final ConfidentialMptClawbackProof PROOF =
    ConfidentialMptClawbackProof.fromHex(Strings.repeat("CD", ConfidentialMptClawbackProof.EXPECTED_SIZE));
  private static final ConfidentialMptClawbackContext CONTEXT =
    ConfidentialMptClawbackContext.fromHex(Strings.repeat("AB", 32));

  private MptCryptoLibrary lib;
  private JnaConfidentialMptClawbackProofVerifier verifier;

  @BeforeEach
  void setUp() {
    lib = mock(MptCryptoLibrary.class);
    verifier = new JnaConfidentialMptClawbackProofVerifier(lib);
  }

  @Test
  void returnsTrueWhenNativeReturnsZero() {
    when(lib.mpt_verify_clawback_proof(any(), anyLong(), any(), any(), any())).thenReturn(0);
    assertThat(verifier.verifyProof(PROOF, CIPHERTEXT, SECP_PUBLIC_KEY, AMOUNT, CONTEXT)).isTrue();
  }

  @Test
  void returnsFalseWhenNativeReturnsNonZero() {
    when(lib.mpt_verify_clawback_proof(any(), anyLong(), any(), any(), any())).thenReturn(-1);
    assertThat(verifier.verifyProof(PROOF, CIPHERTEXT, SECP_PUBLIC_KEY, AMOUNT, CONTEXT)).isFalse();
  }

  @Test
  void rejectsNonSecp256k1PublicKey() {
    assertThatThrownBy(() -> verifier.verifyProof(PROOF, CIPHERTEXT, ED_PUBLIC_KEY, AMOUNT, CONTEXT))
      .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("SECP256K1");
  }

  @Test
  void rejectsNullArguments() {
    assertThatThrownBy(() -> verifier.verifyProof(null, CIPHERTEXT, SECP_PUBLIC_KEY, AMOUNT, CONTEXT))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("proof");
    assertThatThrownBy(() -> verifier.verifyProof(PROOF, null, SECP_PUBLIC_KEY, AMOUNT, CONTEXT))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("issuerEncryptedBalance");
    assertThatThrownBy(() -> verifier.verifyProof(PROOF, CIPHERTEXT, null, AMOUNT, CONTEXT))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("issuerPublicKey");
    assertThatThrownBy(() -> verifier.verifyProof(PROOF, CIPHERTEXT, SECP_PUBLIC_KEY, null, CONTEXT))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("amount");
    assertThatThrownBy(() -> verifier.verifyProof(PROOF, CIPHERTEXT, SECP_PUBLIC_KEY, AMOUNT, null))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("context");
  }
}
