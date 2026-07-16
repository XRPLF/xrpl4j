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
import org.xrpl.xrpl4j.crypto.keys.Seed;

import java.util.Arrays;

/**
 * Unit tests for {@link JnaConfidentialMptConvertProofGenerator} using a mocked {@link MptCryptoLibrary}, verifying
 * key-type/size preconditions and error handling without loading the native mpt-crypto library.
 */
class JnaConfidentialMptConvertProofGeneratorTest {

  private static final KeyPair SECP_KEY_PAIR =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("convert-proof-gen")).deriveKeyPair();
  private static final KeyPair ED_KEY_PAIR =
    Seed.ed25519SeedFromPassphrase(Passphrase.of("convert-proof-gen")).deriveKeyPair();
  private static final ConfidentialMptConvertContext CONTEXT =
    ConfidentialMptConvertContext.fromHex(Strings.repeat("AB", 32));

  private MptCryptoLibrary lib;
  private JnaConfidentialMptConvertProofGenerator generator;

  @BeforeEach
  void setUp() {
    lib = mock(MptCryptoLibrary.class);
    generator = new JnaConfidentialMptConvertProofGenerator(lib);
  }

  @Test
  void generateProofReturnsNativeProof() {
    byte[] expected = new byte[64]; // 64-byte Schnorr proof.
    Arrays.fill(expected, (byte) 0x09);
    when(lib.mpt_get_convert_proof(any(), any(), any(), any())).thenAnswer(invocation -> {
      byte[] out = invocation.getArgument(3);
      System.arraycopy(expected, 0, out, 0, expected.length);
      return 0;
    });

    ConfidentialMptConvertProof proof = generator.generateProof(SECP_KEY_PAIR, CONTEXT);

    assertThat(proof.value().toByteArray()).isEqualTo(expected);
  }

  @Test
  void generateProofRejectsNonSecp256k1KeyPair() {
    assertThatThrownBy(() -> generator.generateProof(ED_KEY_PAIR, CONTEXT))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("SECP256K1");
  }

  @Test
  void generateProofThrowsOnNativeError() {
    when(lib.mpt_get_convert_proof(any(), any(), any(), any())).thenReturn(-1);

    assertThatThrownBy(() -> generator.generateProof(SECP_KEY_PAIR, CONTEXT))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("mpt_get_convert_proof failed");
  }

  @Test
  void generateProofRejectsNullArguments() {
    assertThatThrownBy(() -> generator.generateProof(null, CONTEXT))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("keyPair");
    assertThatThrownBy(() -> generator.generateProof(SECP_KEY_PAIR, null))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("context");
  }
}
