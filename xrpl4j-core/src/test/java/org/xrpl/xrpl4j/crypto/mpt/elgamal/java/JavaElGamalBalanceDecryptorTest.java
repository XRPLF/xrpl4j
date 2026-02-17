package org.xrpl.xrpl4j.crypto.mpt.elgamal.java;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalBalanceEncryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKey;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPublicKey;

/**
 * Unit tests for {@link JavaElGamalBalanceDecryptor}.
 */
public class JavaElGamalBalanceDecryptorTest extends AbstractElGamalTest {

  private ElGamalBalanceEncryptor elGamalBalanceEncryptor;

  private JavaElGamalBalanceDecryptor javaElGamalBalanceDecryptor;

  @BeforeEach
  void setUp() {
    this.elGamalBalanceEncryptor = new JavaElGamalBalanceEncryptor();
    this.javaElGamalBalanceDecryptor = new JavaElGamalBalanceDecryptor();
  }

  @Test
  void testDecryptionFailsForLargeAmount() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());

    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong largeAmount = ElGamalBalanceEncryptor.MAX_DECRYPTABLE_AMOUNT.plus(UnsignedLong.ONE);

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(largeAmount, publicKey, blindingFactor);

    assertThatThrownBy(() -> javaElGamalBalanceDecryptor.decrypt(ciphertext, privateKey))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Amount not found within search range");
  }
}
