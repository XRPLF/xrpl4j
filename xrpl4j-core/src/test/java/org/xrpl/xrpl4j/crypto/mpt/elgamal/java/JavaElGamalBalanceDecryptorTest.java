package org.xrpl.xrpl4j.crypto.mpt.elgamal.java;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.mpt.RandomnessUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalBalanceEncryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKey;

import java.security.SecureRandom;

/**
 * Unit tests for {@link JavaElGamalBalanceDecryptor}.
 */
public class JavaElGamalBalanceDecryptorTest extends AbstractElGamalTest {

  private ElGamalBalanceEncryptor elGamalBalanceEncryptor;
  private Secp256k1Operations secp256k1;

  private JavaElGamalBalanceDecryptor javaElGamalBalanceDecryptor;

  @BeforeEach
  void setUp() {
    this.secp256k1 = new Secp256k1Operations();
    this.elGamalBalanceEncryptor = new JavaElGamalBalanceEncryptor(secp256k1);
    this.javaElGamalBalanceDecryptor = new JavaElGamalBalanceDecryptor(secp256k1);
  }

  @Test
  void testDecryptionFailsForLargeAmount() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());

    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    UnsignedLong largeAmount = ElGamalBalanceEncryptor.MAX_DECRYPTABLE_AMOUNT.plus(UnsignedLong.ONE);

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(largeAmount, publicKeyAsEcPoint, blindingFactor);

    assertThatThrownBy(() -> javaElGamalBalanceDecryptor.decrypt(ciphertext, privateKey))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Amount not found within search range");
  }
}
