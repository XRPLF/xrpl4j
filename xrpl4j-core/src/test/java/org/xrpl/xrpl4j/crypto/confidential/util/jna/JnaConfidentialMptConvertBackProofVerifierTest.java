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
import org.xrpl.xrpl4j.crypto.confidential.model.Commitment;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertBackProof;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;

/**
 * Unit tests for {@link JnaConfidentialMptConvertBackProofVerifier} using a mocked {@link MptCryptoLibrary}, verifying
 * that the native return code maps to a boolean and that preconditions are enforced.
 */
class JnaConfidentialMptConvertBackProofVerifierTest {

  private static final PublicKey SECP_PUBLIC_KEY =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("convert-back-verify")).deriveKeyPair().publicKey();
  private static final PublicKey ED_PUBLIC_KEY =
    Seed.ed25519SeedFromPassphrase(Passphrase.of("convert-back-verify")).deriveKeyPair().publicKey();
  private static final UnsignedLong AMOUNT = UnsignedLong.valueOf(100);
  private static final EncryptedAmount CIPHERTEXT = EncryptedAmount.of(Strings.repeat("03", 66));
  private static final Commitment BALANCE_COMMITMENT = Commitment.of(Strings.repeat("02", 33));
  private static final ConfidentialMptConvertBackProof PROOF =
    ConfidentialMptConvertBackProof.fromHex(Strings.repeat("CD", 816)); // 816 bytes.
  private static final ConfidentialMptConvertBackContext CONTEXT =
    ConfidentialMptConvertBackContext.fromHex(Strings.repeat("AB", 32));

  private MptCryptoLibrary lib;
  private JnaConfidentialMptConvertBackProofVerifier verifier;

  @BeforeEach
  void setUp() {
    lib = mock(MptCryptoLibrary.class);
    verifier = new JnaConfidentialMptConvertBackProofVerifier(lib);
  }

  @Test
  void returnsTrueWhenNativeReturnsZero() {
    when(lib.mpt_verify_convert_back_proof(any(), any(), any(), any(), anyLong(), any())).thenReturn(0);
    assertThat(
      verifier.verifyProof(PROOF, SECP_PUBLIC_KEY, CIPHERTEXT, BALANCE_COMMITMENT, AMOUNT, CONTEXT)
    ).isTrue();
  }

  @Test
  void returnsFalseWhenNativeReturnsNonZero() {
    when(lib.mpt_verify_convert_back_proof(any(), any(), any(), any(), anyLong(), any())).thenReturn(-1);
    assertThat(
      verifier.verifyProof(PROOF, SECP_PUBLIC_KEY, CIPHERTEXT, BALANCE_COMMITMENT, AMOUNT, CONTEXT)
    ).isFalse();
  }

  @Test
  void rejectsNonSecp256k1PublicKey() {
    assertThatThrownBy(() -> verifier.verifyProof(
      PROOF, ED_PUBLIC_KEY, CIPHERTEXT, BALANCE_COMMITMENT, AMOUNT, CONTEXT
    )).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("SECP256K1");
  }

  @Test
  void rejectsNullArguments() {
    assertThatThrownBy(() -> verifier.verifyProof(
      null, SECP_PUBLIC_KEY, CIPHERTEXT, BALANCE_COMMITMENT, AMOUNT, CONTEXT
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("proof");
    assertThatThrownBy(() -> verifier.verifyProof(
      PROOF, null, CIPHERTEXT, BALANCE_COMMITMENT, AMOUNT, CONTEXT
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("senderPublicKey");
    assertThatThrownBy(() -> verifier.verifyProof(
      PROOF, SECP_PUBLIC_KEY, null, BALANCE_COMMITMENT, AMOUNT, CONTEXT
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("encryptedBalance");
    assertThatThrownBy(() -> verifier.verifyProof(
      PROOF, SECP_PUBLIC_KEY, CIPHERTEXT, null, AMOUNT, CONTEXT
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("balanceCommitment");
    assertThatThrownBy(() -> verifier.verifyProof(
      PROOF, SECP_PUBLIC_KEY, CIPHERTEXT, BALANCE_COMMITMENT, null, CONTEXT
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("amount");
    assertThatThrownBy(() -> verifier.verifyProof(
      PROOF, SECP_PUBLIC_KEY, CIPHERTEXT, BALANCE_COMMITMENT, AMOUNT, null
    )).isInstanceOf(NullPointerException.class).hasMessageContaining("context");
  }
}
