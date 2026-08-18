package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.model.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.util.BlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialCiphertextArithmetic;
import org.xrpl.xrpl4j.crypto.confidential.util.MptAmountDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.MptAmountEncryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaBlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaConfidentialCiphertextArithmetic;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaMptAmountDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.util.jna.JnaMptAmountEncryptor;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;

/**
 * Real-crypto round-trip test for {@link JnaConfidentialCiphertextArithmetic} against the native mpt-crypto library.
 *
 * <p>Unlike the mocked unit test in {@code xrpl4j-core}, this exercises the actual native {@code secp256k1_elgamal_add}
 * / {@code secp256k1_elgamal_subtract} routines (the native library is on the classpath only in this module). It proves
 * the homomorphic identity the Batch assembler relies on: combining two ciphertexts under the same key yields an
 * encryption of the sum/difference of their plaintexts. It does not require a rippled node.</p>
 */
class ConfidentialCiphertextArithmeticIT {

  private final MptAmountEncryptor encryptor = new JnaMptAmountEncryptor();
  private final MptAmountDecryptor decryptor = new JnaMptAmountDecryptor();
  private final ConfidentialCiphertextArithmetic arithmetic = new JnaConfidentialCiphertextArithmetic();
  private final BlindingFactorGenerator blindingFactors = new JnaBlindingFactorGenerator();

  @Test
  void addYieldsEncryptionOfSum() {
    KeyPair keyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    UnsignedLong a = UnsignedLong.valueOf(100);
    UnsignedLong b = UnsignedLong.valueOf(30);

    // Independent blinding factors: the homomorphic sum's blinding is r_a + r_b, which still decrypts to a + b.
    EncryptedAmount encA = encryptor.encrypt(a, keyPair.publicKey(), blindingFactors.generate());
    EncryptedAmount encB = encryptor.encrypt(b, keyPair.publicKey(), blindingFactors.generate());

    EncryptedAmount sum = arithmetic.add(encA, encB);

    UnsignedLong decrypted = decryptor.decrypt(sum, keyPair.privateKey(), UnsignedLong.ZERO, UnsignedLong.valueOf(1000));
    assertThat(decrypted).isEqualTo(UnsignedLong.valueOf(130));
  }

  @Test
  void subtractYieldsEncryptionOfDifference() {
    KeyPair keyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    UnsignedLong a = UnsignedLong.valueOf(100);
    UnsignedLong b = UnsignedLong.valueOf(30);

    EncryptedAmount encA = encryptor.encrypt(a, keyPair.publicKey(), blindingFactors.generate());
    EncryptedAmount encB = encryptor.encrypt(b, keyPair.publicKey(), blindingFactors.generate());

    EncryptedAmount difference = arithmetic.subtract(encA, encB);

    UnsignedLong decrypted =
      decryptor.decrypt(difference, keyPair.privateKey(), UnsignedLong.ZERO, UnsignedLong.valueOf(1000));
    assertThat(decrypted).isEqualTo(UnsignedLong.valueOf(70));
  }

  @Test
  void subtractEqualAmountsYieldsZero() {
    // Spending an entire balance: the balance ciphertext and the amount ciphertext encrypt the same value but under
    // independent blinding factors, so the difference is a valid ciphertext of 0 (not the degenerate point-at-infinity
    // that subtracting an identical ciphertext would produce). This is the shape the Batch assembler actually creates.
    KeyPair keyPair = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    UnsignedLong amount = UnsignedLong.valueOf(100);

    EncryptedAmount balance = encryptor.encrypt(amount, keyPair.publicKey(), blindingFactors.generate());
    EncryptedAmount spent = encryptor.encrypt(amount, keyPair.publicKey(), blindingFactors.generate());
    EncryptedAmount remaining = arithmetic.subtract(balance, spent);

    UnsignedLong decrypted =
      decryptor.decrypt(remaining, keyPair.privateKey(), UnsignedLong.ZERO, UnsignedLong.valueOf(1000));
    assertThat(decrypted).isEqualTo(UnsignedLong.ZERO);
  }
}
