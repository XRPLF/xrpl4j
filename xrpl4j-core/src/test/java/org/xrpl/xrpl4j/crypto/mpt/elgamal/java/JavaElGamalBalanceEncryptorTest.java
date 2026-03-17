package org.xrpl.xrpl4j.crypto.mpt.elgamal.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.mpt.KeyPairUtils;
import org.xrpl.xrpl4j.crypto.mpt.RandomnessUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalBalanceDecryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalKeyPair;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Unit tests for {@link JavaElGamalBalanceEncryptor}.
 */
public class JavaElGamalBalanceEncryptorTest extends AbstractElGamalTest {

  private ElGamalBalanceDecryptor elGamalBalanceDecryptor;
  private Secp256k1Operations secp256k1;

  private JavaElGamalBalanceEncryptor elGamalBalanceEncryptor;

  @BeforeEach
  void setUp() {
    this.secp256k1 = new Secp256k1Operations();
    this.elGamalBalanceEncryptor = new JavaElGamalBalanceEncryptor(secp256k1);
    this.elGamalBalanceDecryptor = new JavaElGamalBalanceDecryptor(secp256k1);
  }

  @Test
  void deleteMe() {

    KeyPair keypair = Seed.secp256k1Seed().deriveKeyPair();
    ECPoint publicKey = BcKeyUtils.toEcPublicKeyParameters(keypair.publicKey()).getQ();
//        ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(
      UnsignedLong.valueOf(12345), publicKey, blindingFactor
    );
  }

  @Test
  void testEncryptionSmokeTest() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(UnsignedLong.valueOf(12345), publicKeyAsEcPoint,
      blindingFactor);

    assertThat(ciphertext).isNotNull();
    assertThat(ciphertext.c1()).isNotNull();
    assertThat(ciphertext.c2()).isNotNull();
    assertThat(ciphertext.c1().isInfinity()).isFalse();
    assertThat(ciphertext.c2().isInfinity()).isFalse();
  }

  @Test
  void testEncryptionDecryptionRoundTrip() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    UnsignedLong originalAmount = UnsignedLong.valueOf(10001);

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(originalAmount, publicKeyAsEcPoint, blindingFactor);
    long decryptedAmount = elGamalBalanceDecryptor.decrypt(ciphertext,
      keyPair.privateKey().naturalBytes().toByteArray()
    );

    assertThat(decryptedAmount).isEqualTo(originalAmount.longValue());
  }

  @Test
  void testZeroEncryption() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    UnsignedLong originalAmount = UnsignedLong.ZERO;

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(
      originalAmount, publicKeyAsEcPoint, blindingFactor
    );
    long decryptedAmount = elGamalBalanceDecryptor.decrypt(ciphertext,
      keyPair.privateKey().naturalBytes().toByteArray()
    );

    assertThat(decryptedAmount).isEqualTo(originalAmount.longValue());
  }

  @Test
  void testCanonicalEncryptedZero() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());

    // Use placeholder byte arrays for IDs (20 bytes for account, 24 for issuance)
    byte[] accountId = new byte[20];
    accountId[0] = 1;
    byte[] issuanceId = new byte[24];
    issuanceId[0] = 2;

    // Generate it once
    ElGamalCiphertext ciphertextA = elGamalBalanceEncryptor.generateCanonicalEncryptedZero(
      publicKeyAsEcPoint, accountId, issuanceId
    );

    // Generate it a second time with the same inputs
    ElGamalCiphertext ciphertextB = elGamalBalanceEncryptor.generateCanonicalEncryptedZero(
      publicKeyAsEcPoint, accountId, issuanceId
    );

    // 1. Verify that it decrypts to zero
    long decryptedAmount = elGamalBalanceDecryptor.decrypt(ciphertextA,
      keyPair.privateKey().naturalBytes().toByteArray());
    assertThat(decryptedAmount).isEqualTo(0);

    // 2. Verify that the output is deterministic (both ciphertexts are identical)
    assertThat(ciphertextA).isEqualTo(ciphertextB);
  }

  @Test
  void testVerifyEncryptionValidCase() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    UnsignedLong amount = UnsignedLong.valueOf(12345);

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(amount, publicKeyAsEcPoint, blindingFactor);

    boolean isValid = elGamalBalanceEncryptor.verifyEncryption(ciphertext, publicKeyAsEcPoint, amount, blindingFactor);
    assertThat(isValid).isTrue();
  }

  @Test
  void testVerifyEncryptionInvalidAmount() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    UnsignedLong amount = UnsignedLong.valueOf(12345);
    UnsignedLong badAmount = UnsignedLong.valueOf(54321);

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(amount, publicKeyAsEcPoint, blindingFactor);

    boolean isValid = elGamalBalanceEncryptor.verifyEncryption(ciphertext, publicKeyAsEcPoint, badAmount,
      blindingFactor);
    assertThat(isValid).isFalse();
  }

  @Test
  void testVerifyEncryptionInvalidBlindingFactor() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    UnsignedLong amount = UnsignedLong.valueOf(12345);

    // Create a bad blinding factor by flipping bits
    byte[] badBlindingFactor = Arrays.copyOf(blindingFactor, blindingFactor.length);
    badBlindingFactor[31] ^= (byte) 0xFF;

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(amount, publicKeyAsEcPoint, blindingFactor);

    boolean isValid = elGamalBalanceEncryptor.verifyEncryption(ciphertext, publicKeyAsEcPoint, amount,
      badBlindingFactor);
    assertThat(isValid).isFalse();
  }

  @Test
  void testCiphertextSerialization() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    UnsignedLong amount = UnsignedLong.valueOf(42);

    ElGamalCiphertext original = elGamalBalanceEncryptor.encrypt(amount, publicKeyAsEcPoint, blindingFactor);

    // Serialize and deserialize
    byte[] serialized = original.toBytes();
    assertThat(serialized).hasSize(66); // 33 + 33 bytes

    ElGamalCiphertext deserialized = ElGamalCiphertext.fromBytes(serialized, secp256k1);

    assertThat(deserialized).isEqualTo(original);

    // Verify the deserialized ciphertext still decrypts correctly
    long decryptedAmount = elGamalBalanceDecryptor.decrypt(deserialized,
      keyPair.privateKey().naturalBytes().toByteArray());
    assertThat(decryptedAmount).isEqualTo(amount.longValue());
  }

  @Test
  void testCanonicalZeroDifferentInputsProduceDifferentOutputs() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());

    byte[] accountId1 = new byte[20];
    accountId1[0] = 1;
    byte[] accountId2 = new byte[20];
    accountId2[0] = 2;
    byte[] issuanceId = new byte[24];

    ElGamalCiphertext ciphertext1 = elGamalBalanceEncryptor.generateCanonicalEncryptedZero(
      publicKeyAsEcPoint, accountId1, issuanceId
    );
    ElGamalCiphertext ciphertext2 = elGamalBalanceEncryptor.generateCanonicalEncryptedZero(
      publicKeyAsEcPoint, accountId2, issuanceId
    );

    // Different inputs should produce different ciphertexts
    assertThat(ciphertext1).isNotEqualTo(ciphertext2);

    // But both should still decrypt to zero
    assertThat(
      elGamalBalanceDecryptor.decrypt(ciphertext1, keyPair.privateKey().naturalBytes().toByteArray())).isEqualTo(0);
    assertThat(
      elGamalBalanceDecryptor.decrypt(ciphertext2, keyPair.privateKey().naturalBytes().toByteArray())).isEqualTo(0);
  }
}
