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
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;

import java.util.Arrays;

/**
 * Unit tests for {@link JnaMptAmountEncryptor} using a mocked {@link MptCryptoLibrary}. Verifies that the 66-byte
 * native ciphertext is returned as a wire {@link EncryptedAmount} and that preconditions are enforced.
 */
class JnaMptAmountEncryptorTest {

  private static final PublicKey SECP_PUBLIC_KEY =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("encryptor")).deriveKeyPair().publicKey();
  private static final PublicKey ED_PUBLIC_KEY =
    Seed.ed25519SeedFromPassphrase(Passphrase.of("encryptor")).deriveKeyPair().publicKey();
  private static final BlindingFactor BLINDING_FACTOR = BlindingFactor.of(Strings.repeat("11", 32));
  private static final UnsignedLong AMOUNT = UnsignedLong.valueOf(1000);

  private MptCryptoLibrary lib;
  private JnaMptAmountEncryptor encryptor;

  @BeforeEach
  void setUp() {
    lib = mock(MptCryptoLibrary.class);
    encryptor = new JnaMptAmountEncryptor(lib);
  }

  @Test
  void encryptReturnsWireEncryptedAmount() {
    byte[] expected = new byte[66]; // 66-byte ciphertext.
    Arrays.fill(expected, (byte) 0x02);
    when(lib.mpt_encrypt_amount(anyLong(), any(), any(), any())).thenAnswer(invocation -> {
      byte[] out = invocation.getArgument(3);
      System.arraycopy(expected, 0, out, 0, expected.length);
      return 0;
    });

    EncryptedAmount ciphertext = encryptor.encrypt(AMOUNT, SECP_PUBLIC_KEY, BLINDING_FACTOR);

    assertThat(ciphertext.value().toByteArray()).isEqualTo(expected);
  }

  @Test
  void encryptRejectsNonSecp256k1PublicKey() {
    assertThatThrownBy(() -> encryptor.encrypt(AMOUNT, ED_PUBLIC_KEY, BLINDING_FACTOR))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("SECP256K1");
  }

  @Test
  void encryptThrowsOnNativeError() {
    when(lib.mpt_encrypt_amount(anyLong(), any(), any(), any())).thenReturn(-1);

    assertThatThrownBy(() -> encryptor.encrypt(AMOUNT, SECP_PUBLIC_KEY, BLINDING_FACTOR))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("mpt_encrypt_amount failed");
  }

  @Test
  void encryptRejectsNullArguments() {
    assertThatThrownBy(() -> encryptor.encrypt(null, SECP_PUBLIC_KEY, BLINDING_FACTOR))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("amount");
    assertThatThrownBy(() -> encryptor.encrypt(AMOUNT, null, BLINDING_FACTOR))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("publicKey");
    assertThatThrownBy(() -> encryptor.encrypt(AMOUNT, SECP_PUBLIC_KEY, null))
      .isInstanceOf(NullPointerException.class)
      .hasMessageContaining("blindingFactor");
  }
}
