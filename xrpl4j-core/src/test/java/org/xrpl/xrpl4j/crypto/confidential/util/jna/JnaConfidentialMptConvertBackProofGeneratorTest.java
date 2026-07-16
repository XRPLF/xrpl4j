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
import org.xrpl.xrpl4j.crypto.confidential.model.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.Commitment;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertBackProof;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.Seed;

/**
 * Unit tests for {@link JnaConfidentialMptConvertBackProofGenerator} using a mocked {@link MptCryptoLibrary}, verifying
 * key-type preconditions and error handling without loading the native mpt-crypto library.
 */
class JnaConfidentialMptConvertBackProofGeneratorTest {

  private static final KeyPair SECP_KEY_PAIR =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("convert-back-proof-gen")).deriveKeyPair();
  private static final KeyPair ED_KEY_PAIR =
    Seed.ed25519SeedFromPassphrase(Passphrase.of("convert-back-proof-gen")).deriveKeyPair();
  private static final UnsignedLong AMOUNT = UnsignedLong.valueOf(100);
  private static final ConfidentialMptConvertBackContext CONTEXT =
    ConfidentialMptConvertBackContext.fromHex(Strings.repeat("AB", 32));
  private static final PedersenProofParams BALANCE_PARAMS = PedersenProofParams.builder()
    .pedersenCommitment(Commitment.of(Strings.repeat("02", 33)).value())
    .amount(UnsignedLong.valueOf(500))
    .encryptedAmount(EncryptedAmount.of(Strings.repeat("03", 66)))
    .blindingFactor(BlindingFactor.of(Strings.repeat("11", 32)))
    .build();

  private MptCryptoLibrary lib;
  private JnaConfidentialMptConvertBackProofGenerator generator;

  @BeforeEach
  void setUp() {
    lib = mock(MptCryptoLibrary.class);
    generator = new JnaConfidentialMptConvertBackProofGenerator(lib);
  }

  @Test
  void generateProofReturnsNativeProof() {
    when(lib.mpt_get_convert_back_proof(any(), any(), any(), anyLong(), any(), any())).thenReturn(0);

    ConfidentialMptConvertBackProof proof = generator.generateProof(SECP_KEY_PAIR, AMOUNT, CONTEXT, BALANCE_PARAMS);

    assertThat(proof.value().length()).isEqualTo(816); // 816 bytes.
  }

  @Test
  void generateProofRejectsNonSecp256k1KeyPair() {
    assertThatThrownBy(() -> generator.generateProof(ED_KEY_PAIR, AMOUNT, CONTEXT, BALANCE_PARAMS))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("SECP256K1");
  }

  @Test
  void generateProofThrowsOnNativeError() {
    when(lib.mpt_get_convert_back_proof(any(), any(), any(), anyLong(), any(), any())).thenReturn(-1);

    assertThatThrownBy(() -> generator.generateProof(SECP_KEY_PAIR, AMOUNT, CONTEXT, BALANCE_PARAMS))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("mpt_get_convert_back_proof failed");
  }

  @Test
  void generateProofRejectsNullArguments() {
    assertThatThrownBy(() -> generator.generateProof(null, AMOUNT, CONTEXT, BALANCE_PARAMS))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("senderKeyPair");
    assertThatThrownBy(() -> generator.generateProof(SECP_KEY_PAIR, null, CONTEXT, BALANCE_PARAMS))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("amount");
    assertThatThrownBy(() -> generator.generateProof(SECP_KEY_PAIR, AMOUNT, null, BALANCE_PARAMS))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("context");
    assertThatThrownBy(() -> generator.generateProof(SECP_KEY_PAIR, AMOUNT, CONTEXT, null))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("balanceParams");
  }
}
