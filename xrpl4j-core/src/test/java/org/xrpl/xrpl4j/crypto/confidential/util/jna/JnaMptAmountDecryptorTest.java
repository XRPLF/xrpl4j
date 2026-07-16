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
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;

/**
 * Unit tests for {@link JnaMptAmountDecryptor} using a mocked {@link MptCryptoLibrary}. Verifies that the native
 * out-parameter is returned as an {@link UnsignedLong} and that preconditions are enforced.
 */
class JnaMptAmountDecryptorTest {

  private static final KeyPair SECP_KEY_PAIR =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("decryptor")).deriveKeyPair();
  private static final PrivateKey SECP_PRIVATE_KEY = SECP_KEY_PAIR.privateKey();
  private static final PrivateKey ED_PRIVATE_KEY =
    Seed.ed25519SeedFromPassphrase(Passphrase.of("decryptor")).deriveKeyPair().privateKey();
  private static final EncryptedAmount CIPHERTEXT = EncryptedAmount.of(Strings.repeat("03", 66));
  private static final UnsignedLong MIN = UnsignedLong.ZERO;
  private static final UnsignedLong MAX = UnsignedLong.valueOf(1000);

  private MptCryptoLibrary lib;
  private JnaMptAmountDecryptor decryptor;

  @BeforeEach
  void setUp() {
    lib = mock(MptCryptoLibrary.class);
    decryptor = new JnaMptAmountDecryptor(lib);
  }

  @Test
  void decryptReturnsNativeAmount() {
    when(lib.mpt_decrypt_amount(any(), any(), any(), anyLong(), anyLong())).thenAnswer(invocation -> {
      long[] out = invocation.getArgument(2);
      out[0] = 42L;
      return 0;
    });

    assertThat(decryptor.decrypt(CIPHERTEXT, SECP_PRIVATE_KEY, MIN, MAX)).isEqualTo(UnsignedLong.valueOf(42));
  }

  @Test
  void decryptRejectsNonSecp256k1PrivateKey() {
    assertThatThrownBy(() -> decryptor.decrypt(CIPHERTEXT, ED_PRIVATE_KEY, MIN, MAX))
      .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("SECP256K1");
  }

  @Test
  void decryptRejectsMinGreaterThanMax() {
    assertThatThrownBy(() ->
      decryptor.decrypt(CIPHERTEXT, SECP_PRIVATE_KEY, UnsignedLong.valueOf(100), UnsignedLong.valueOf(50))
    ).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("minAmount must be less than");
  }

  @Test
  void decryptThrowsOnNativeError() {
    when(lib.mpt_decrypt_amount(any(), any(), any(), anyLong(), anyLong())).thenReturn(-1);

    assertThatThrownBy(() -> decryptor.decrypt(CIPHERTEXT, SECP_PRIVATE_KEY, MIN, MAX))
      .isInstanceOf(IllegalStateException.class).hasMessageContaining("mpt_decrypt_amount failed");
  }

  @Test
  void decryptRejectsNullArguments() {
    assertThatThrownBy(() -> decryptor.decrypt(null, SECP_PRIVATE_KEY, MIN, MAX))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("ciphertext");
    assertThatThrownBy(() -> decryptor.decrypt(CIPHERTEXT, null, MIN, MAX))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("privateKey");
    assertThatThrownBy(() -> decryptor.decrypt(CIPHERTEXT, SECP_PRIVATE_KEY, null, MAX))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("minAmount");
    assertThatThrownBy(() -> decryptor.decrypt(CIPHERTEXT, SECP_PRIVATE_KEY, MIN, null))
      .isInstanceOf(NullPointerException.class).hasMessageContaining("maxAmount");
  }
}
