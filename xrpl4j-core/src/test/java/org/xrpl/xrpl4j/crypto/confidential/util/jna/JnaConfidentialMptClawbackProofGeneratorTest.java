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
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;

import java.util.Arrays;

/**
 * Unit tests for {@link JnaConfidentialMptClawbackProofGenerator} using a mocked {@link MptCryptoLibrary}, verifying
 * key-type preconditions and error handling without loading the native mpt-crypto library.
 */
class JnaConfidentialMptClawbackProofGeneratorTest {

  private static final KeyPair SECP_KEY_PAIR =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("clawback-proof-gen")).deriveKeyPair();
  private static final KeyPair ED_KEY_PAIR =
    Seed.ed25519SeedFromPassphrase(Passphrase.of("clawback-proof-gen")).deriveKeyPair();
  private static final PublicKey SECP_PUBLIC_KEY = SECP_KEY_PAIR.publicKey();
  private static final PrivateKey SECP_PRIVATE_KEY = SECP_KEY_PAIR.privateKey();
  private static final UnsignedLong AMOUNT = UnsignedLong.valueOf(100);
  private static final EncryptedAmount CIPHERTEXT = EncryptedAmount.of(Strings.repeat("03", 66));
  private static final ConfidentialMptClawbackContext CONTEXT =
    ConfidentialMptClawbackContext.fromHex(Strings.repeat("AB", 32));

  private MptCryptoLibrary lib;
  private JnaConfidentialMptClawbackProofGenerator generator;

  @BeforeEach
  void setUp() {
    lib = mock(MptCryptoLibrary.class);
    generator = new JnaConfidentialMptClawbackProofGenerator(lib);
  }

  @Test
  void generateProofReturnsNativeProof() {
    byte[] expected = new byte[64]; // 64-byte compact clawback proof.
    Arrays.fill(expected, (byte) 0x09);
    when(lib.mpt_get_clawback_proof(any(), any(), any(), anyLong(), any(), any())).thenAnswer(invocation -> {
      byte[] out = invocation.getArgument(5);
      System.arraycopy(expected, 0, out, 0, expected.length);
      return 0;
    });

    ConfidentialMptClawbackProof proof =
      generator.generateProof(CIPHERTEXT, SECP_PUBLIC_KEY, AMOUNT, SECP_PRIVATE_KEY, CONTEXT);

    assertThat(proof.value().toByteArray()).isEqualTo(expected);
  }

  @Test
  void generateProofRejectsNonSecp256k1PublicKey() {
    assertThatThrownBy(() -> generator.generateProof(
      CIPHERTEXT, ED_KEY_PAIR.publicKey(), AMOUNT, ED_KEY_PAIR.privateKey(), CONTEXT
    )).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("SECP256K1");
  }

  @Test
  void generateProofThrowsOnNativeError() {
    when(lib.mpt_get_clawback_proof(any(), any(), any(), anyLong(), any(), any())).thenReturn(-1);

    assertThatThrownBy(() -> generator.generateProof(CIPHERTEXT, SECP_PUBLIC_KEY, AMOUNT, SECP_PRIVATE_KEY, CONTEXT))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("mpt_get_clawback_proof failed");
  }

  @Test
  void generateProofRejectsNullArguments() {
    assertThatThrownBy(() -> generator.generateProof(null, SECP_PUBLIC_KEY, AMOUNT, SECP_PRIVATE_KEY, CONTEXT))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("issuerEncryptedBalance");
    assertThatThrownBy(() -> generator.generateProof(CIPHERTEXT, null, AMOUNT, SECP_PRIVATE_KEY, CONTEXT))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("issuerPublicKey");
    assertThatThrownBy(() -> generator.generateProof(CIPHERTEXT, SECP_PUBLIC_KEY, null, SECP_PRIVATE_KEY, CONTEXT))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("amount");
    assertThatThrownBy(() -> generator.generateProof(CIPHERTEXT, SECP_PUBLIC_KEY, AMOUNT, null, CONTEXT))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("issuerPrivateKey");
    assertThatThrownBy(() -> generator.generateProof(CIPHERTEXT, SECP_PUBLIC_KEY, AMOUNT, SECP_PRIVATE_KEY, null))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("context");
  }
}
