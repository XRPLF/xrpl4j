package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SamePlaintextMultiProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link JavaSamePlaintextMultiProofGenerator}.
 *
 * <p>This test uses deterministic inputs for comparison with C implementation.</p>
 */
class JavaSamePlaintextMultiProofGeneratorTest {

  private SamePlaintextMultiProofGenerator proofGenerator;

  @BeforeEach
  void setUp() {
    proofGenerator = new JavaSamePlaintextMultiProofGenerator();
  }

  /**
   * Deterministic test with N=2 ciphertexts for comparison with C implementation.
   *
   * <p>This test uses:
   * <ul>
   *   <li>Amount: 500</li>
   *   <li>Public Key 1: derived from passphrase "test_pk1"</li>
   *   <li>Public Key 2: derived from passphrase "test_pk2"</li>
   *   <li>Blinding factor r1: 0x01, 0x01, ... (32 bytes of 0x01)</li>
   *   <li>Blinding factor r2: 0x02, 0x02, ... (32 bytes of 0x02)</li>
   *   <li>Context hash: 0xAA repeated 32 times</li>
   *   <li>Nonce km: 0x10, 0x10, ... (32 bytes of 0x10)</li>
   *   <li>Nonce kr1: 0x11, 0x11, ... (32 bytes of 0x11)</li>
   *   <li>Nonce kr2: 0x12, 0x12, ... (32 bytes of 0x12)</li>
   * </ul>
   */
  @Test
  void testDeterministicProofGenerationForCComparison() {
    // 1. Amount
    UnsignedLong amount = UnsignedLong.valueOf(500);

    // 2. Generate deterministic public keys from passphrases
    Seed seed1 = Seed.secp256k1SeedFromPassphrase(Passphrase.of("test_pk1"));
    KeyPair keypair1 = seed1.deriveKeyPair();
    ECPoint pk1 = BcKeyUtils.toEcPublicKeyParameters(keypair1.publicKey()).getQ();

    Seed seed2 = Seed.secp256k1SeedFromPassphrase(Passphrase.of("test_pk2"));
    KeyPair keypair2 = seed2.deriveKeyPair();
    ECPoint pk2 = BcKeyUtils.toEcPublicKeyParameters(keypair2.publicKey()).getQ();

    List<ECPoint> publicKeys = Arrays.asList(pk1, pk2);

    // 3. Deterministic blinding factors
    byte[] r1 = new byte[32];
    byte[] r2 = new byte[32];
    for (int i = 0; i < 32; i++) {
      r1[i] = 0x01;
      r2[i] = 0x02;
    }
    List<byte[]> blindingFactors = Arrays.asList(r1, r2);

    // 4. Create ciphertexts: R = r * G, S = m * G + r * Pk
    BigInteger mInt = BigInteger.valueOf(amount.longValue());
    ECPoint mG = Secp256k1Operations.multiplyG(mInt);

    BigInteger r1Int = new BigInteger(1, r1);
    ECPoint R1 = Secp256k1Operations.multiplyG(r1Int);
    ECPoint S1 = Secp256k1Operations.add(mG, Secp256k1Operations.multiply(pk1, r1Int));

    BigInteger r2Int = new BigInteger(1, r2);
    ECPoint R2 = Secp256k1Operations.multiplyG(r2Int);
    ECPoint S2 = Secp256k1Operations.add(mG, Secp256k1Operations.multiply(pk2, r2Int));

    List<ElGamalCiphertext> ciphertexts = Arrays.asList(
      new ElGamalCiphertext(R1, S1),
      new ElGamalCiphertext(R2, S2)
    );

    // 5. Deterministic context hash
    byte[] contextHash = new byte[32];
    for (int i = 0; i < 32; i++) {
      contextHash[i] = (byte) 0xAA;
    }

    // 6. Deterministic nonces
    byte[] nonceKm = new byte[32];
    byte[] nonceKr1 = new byte[32];
    byte[] nonceKr2 = new byte[32];
    for (int i = 0; i < 32; i++) {
      nonceKm[i] = 0x10;
      nonceKr1[i] = 0x11;
      nonceKr2[i] = 0x12;
    }
    List<byte[]> noncesKr = Arrays.asList(nonceKr1, nonceKr2);

    // 7. Generate proof
    byte[] proof = proofGenerator.generateProof(
      amount, ciphertexts, publicKeys, blindingFactors, contextHash, nonceKm, noncesKr
    );

    // 8. Print all values for C comparison
    printTestValues(amount, publicKeys, blindingFactors, ciphertexts, contextHash, nonceKm, noncesKr, proof);

    // 9. Verify proof
    boolean valid = proofGenerator.verify(proof, ciphertexts, publicKeys, contextHash);
    System.out.println("Proof valid: " + valid);
    System.out.println("================================================================\n");

    // 10. Assertions
    int expectedSize = SamePlaintextMultiProofGenerator.proofSize(2);
    assertThat(proof).hasSize(expectedSize);
    assertThat(valid).isTrue();
  }

  private void printTestValues(
    UnsignedLong amount,
    List<ECPoint> publicKeys,
    List<byte[]> blindingFactors,
    List<ElGamalCiphertext> ciphertexts,
    byte[] contextHash,
    byte[] nonceKm,
    List<byte[]> noncesKr,
    byte[] proof
  ) {
    System.out.println("\n========== SAME PLAINTEXT MULTI PROOF TEST VALUES (N=2) ==========");
    System.out.println();

    System.out.println("=== Amount ===");
    System.out.println("Value: " + amount);
    System.out.println("As scalar (32 bytes): " + BaseEncoding.base16().encode(Secp256k1Operations.unsignedLongToScalar(amount)));
    System.out.println();

    System.out.println("=== Public Keys (uncompressed, 65 bytes each) ===");
    for (int i = 0; i < publicKeys.size(); i++) {
      byte[] uncompressed = publicKeys.get(i).getEncoded(false);
      System.out.println("Pk[" + i + "]: " + BaseEncoding.base16().encode(uncompressed));
    }
    System.out.println();

    System.out.println("=== Blinding Factors (32 bytes each) ===");
    for (int i = 0; i < blindingFactors.size(); i++) {
      System.out.println("r[" + i + "]: " + BaseEncoding.base16().encode(blindingFactors.get(i)));
    }
    System.out.println();

    System.out.println("=== Ciphertexts (uncompressed, 65 bytes each) ===");
    for (int i = 0; i < ciphertexts.size(); i++) {
      ElGamalCiphertext ct = ciphertexts.get(i);
      System.out.println("R[" + i + "] (C1): " + BaseEncoding.base16().encode(ct.c1().getEncoded(false)));
      System.out.println("S[" + i + "] (C2): " + BaseEncoding.base16().encode(ct.c2().getEncoded(false)));
    }
    System.out.println();

    System.out.println("=== Context Hash (32 bytes) ===");
    System.out.println("tx_id: " + BaseEncoding.base16().encode(contextHash));
    System.out.println();

    System.out.println("=== Nonces ===");
    System.out.println("km (32 bytes): " + BaseEncoding.base16().encode(nonceKm));
    for (int i = 0; i < noncesKr.size(); i++) {
      System.out.println("kr[" + i + "] (32 bytes): " + BaseEncoding.base16().encode(noncesKr.get(i)));
    }
    System.out.println();

    // Parse proof components for display
    int n = 2;
    int offset = 0;

    byte[] tm = new byte[33];
    System.arraycopy(proof, offset, tm, 0, 33);
    offset += 33;

    System.out.println("=== Proof Components ===");
    System.out.println("Tm (33 bytes): " + BaseEncoding.base16().encode(tm));

    for (int i = 0; i < n; i++) {
      byte[] trg = new byte[33];
      System.arraycopy(proof, offset, trg, 0, 33);
      offset += 33;
      System.out.println("TrG[" + i + "] (33 bytes): " + BaseEncoding.base16().encode(trg));
    }

    for (int i = 0; i < n; i++) {
      byte[] trp = new byte[33];
      System.arraycopy(proof, offset, trp, 0, 33);
      offset += 33;
      System.out.println("TrP[" + i + "] (33 bytes): " + BaseEncoding.base16().encode(trp));
    }

    byte[] sm = new byte[32];
    System.arraycopy(proof, offset, sm, 0, 32);
    offset += 32;
    System.out.println("sm (32 bytes): " + BaseEncoding.base16().encode(sm));

    for (int i = 0; i < n; i++) {
      byte[] sr = new byte[32];
      System.arraycopy(proof, offset, sr, 0, 32);
      offset += 32;
      System.out.println("sr[" + i + "] (32 bytes): " + BaseEncoding.base16().encode(sr));
    }
    System.out.println();

    System.out.println("=== Serialized Proof ===");
    System.out.println("Size: " + proof.length + " bytes");
    System.out.println("Hex: " + BaseEncoding.base16().encode(proof));
    System.out.println();
  }
}

