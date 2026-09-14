package org.xrpl.xrpl4j.crypto.confidential.util.jna;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertProof;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;

/**
 * Unit tests for {@link JnaConfidentialMptConvertProofVerifier} using a mocked {@link MptCryptoLibrary}, verifying that
 * the native return code maps to a boolean and that preconditions are enforced.
 */
class JnaConfidentialMptConvertProofVerifierTest {

  private static final PublicKey SECP_PUBLIC_KEY =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("convert-proof-verify")).deriveKeyPair().publicKey();
  private static final PublicKey ED_PUBLIC_KEY =
    Seed.ed25519SeedFromPassphrase(Passphrase.of("convert-proof-verify")).deriveKeyPair().publicKey();
  private static final ConfidentialMptConvertProof PROOF =
    ConfidentialMptConvertProof.fromHex(Strings.repeat("CD", 64));
  private static final ConfidentialMptConvertContext CONTEXT =
    ConfidentialMptConvertContext.fromHex(Strings.repeat("AB", 32));

  private MptCryptoLibrary lib;
  private JnaConfidentialMptConvertProofVerifier verifier;

  @BeforeEach
  void setUp() {
    lib = mock(MptCryptoLibrary.class);
    verifier = new JnaConfidentialMptConvertProofVerifier(lib);
  }

  @Test
  void returnsTrueWhenNativeReturnsZero() {
    when(lib.mpt_verify_convert_proof(any(), any(), any())).thenReturn(0);
    assertThat(verifier.verifyProof(PROOF, SECP_PUBLIC_KEY, CONTEXT)).isTrue();
  }

  @Test
  void returnsFalseWhenNativeReturnsNonZero() {
    when(lib.mpt_verify_convert_proof(any(), any(), any())).thenReturn(-1);
    assertThat(verifier.verifyProof(PROOF, SECP_PUBLIC_KEY, CONTEXT)).isFalse();
  }

  @Test
  void rejectsNonSecp256k1PublicKey() {
    assertThatThrownBy(() -> verifier.verifyProof(PROOF, ED_PUBLIC_KEY, CONTEXT))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("SECP256K1");
  }

  @Test
  void rejectsNullArguments() {
    assertThatThrownBy(() -> verifier.verifyProof(null, SECP_PUBLIC_KEY, CONTEXT))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("proof");
    assertThatThrownBy(() -> verifier.verifyProof(PROOF, null, CONTEXT))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("publicKey");
    assertThatThrownBy(() -> verifier.verifyProof(PROOF, SECP_PUBLIC_KEY, null))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("context");
  }
}
