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
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.ElGamalPedersenLinkProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.PedersenCommitmentGenerator;

import java.math.BigInteger;

/**
 * Unit tests for {@link JavaElGamalPedersenLinkProofGenerator}.
 *
 * <p>These tests use deterministic inputs to verify compatibility with C implementation.</p>
 */
class JavaElGamalPedersenLinkProofGeneratorTest {

  private static final BaseEncoding HEX = BaseEncoding.base16().upperCase();

  private JavaElGamalPedersenLinkProofGenerator generator;
  private PedersenCommitmentGenerator pedersenGen;

  @BeforeEach
  void setUp() {
    generator = new JavaElGamalPedersenLinkProofGenerator();
    pedersenGen = new PedersenCommitmentGenerator();
  }

  /**
   * Deterministic test: Generate and verify a link proof with fixed inputs.
   *
   * <p>Test vector:
   * <ul>
   *   <li>amount = 12345</li>
   *   <li>Public key derived from passphrase "test_link_pk"</li>
   *   <li>r (ElGamal randomness) = 0x01 repeated 32 times</li>
   *   <li>rho (Pedersen blinding factor) = 0x02 repeated 32 times</li>
   *   <li>contextHash = 0xAA repeated 32 times</li>
   *   <li>nonceKm = 0x10 repeated 32 times</li>
   *   <li>nonceKr = 0x11 repeated 32 times</li>
   *   <li>nonceKrho = 0x12 repeated 32 times</li>
   * </ul>
   * </p>
   */
  @Test
  void testGenerateAndVerifyProofDeterministic() {
    // 1. Amount
    UnsignedLong amount = UnsignedLong.valueOf(1000);

    // 2. Generate deterministic public key from passphrase
    Seed seed = Seed.secp256k1SeedFromPassphrase(Passphrase.of("test_link_pk"));
    KeyPair keypair = seed.deriveKeyPair();
    ECPoint publicKey = BcKeyUtils.toEcPublicKeyParameters(keypair.publicKey()).getQ();

    // 3. Deterministic ElGamal randomness r
    byte[] r = new byte[32];
    for (int i = 0; i < 32; i++) {
      r[i] = 0x01;
    }

    // 4. Deterministic Pedersen blinding factor rho
    byte[] rho = new byte[32];
    for (int i = 0; i < 32; i++) {
      rho[i] = 0x02;
    }

    // 5. Deterministic context hash
    byte[] contextHash = new byte[32];
    for (int i = 0; i < 32; i++) {
      contextHash[i] = (byte) 0xAA;
    }

    // 6. Deterministic nonces
    byte[] nonceKm = new byte[32];
    byte[] nonceKr = new byte[32];
    byte[] nonceKrho = new byte[32];
    for (int i = 0; i < 32; i++) {
      nonceKm[i] = 0x10;
      nonceKr[i] = 0x11;
      nonceKrho[i] = 0x12;
    }

    // 7. Create ElGamal ciphertext: C1 = r * G, C2 = m * G + r * Pk
    BigInteger rInt = new BigInteger(1, r);
    ECPoint c1 = Secp256k1Operations.multiplyG(rInt);

    BigInteger mInt = BigInteger.valueOf(amount.longValue());
    ECPoint mG = Secp256k1Operations.multiplyG(mInt);
    ECPoint rPk = Secp256k1Operations.multiply(publicKey, rInt);
    ECPoint c2 = Secp256k1Operations.add(mG, rPk);

    // 8. Create Pedersen commitment: PCm = m * G + rho * H
    byte[] commitmentBytes = pedersenGen.generateCommitment(amount, rho);
    ECPoint commitment = Secp256k1Operations.deserialize(commitmentBytes);

    // Print inputs in C code format (uncompressed points with 04 prefix)
    System.out.println("\n=== ElGamal-Pedersen Link Proof Test ===\n");
    System.out.println("--- INPUTS (C code format) ---");
    System.out.println("Amount: " + amount);
    System.out.println();
    System.out.println("// Public Key Pk (65 bytes with 04 prefix)");
    System.out.println("const char* pk_hex = \"" + HEX.encode(publicKey.getEncoded(false)) + "\";");
    System.out.println();
    System.out.println("// ElGamal ciphertext C1 = r*G (65 bytes with 04 prefix)");
    System.out.println("const char* c1_hex = \"" + HEX.encode(c1.getEncoded(false)) + "\";");
    System.out.println();
    System.out.println("// ElGamal ciphertext C2 = m*G + r*Pk (65 bytes with 04 prefix)");
    System.out.println("const char* c2_hex = \"" + HEX.encode(c2.getEncoded(false)) + "\";");
    System.out.println();
    System.out.println("// Pedersen commitment PCm = m*G + rho*H (65 bytes with 04 prefix)");
    System.out.println("const char* pcm_hex = \"" + HEX.encode(commitment.getEncoded(false)) + "\";");
    System.out.println();
    System.out.println("// ElGamal randomness r (32 bytes)");
    System.out.println("const char* r_hex = \"" + HEX.encode(r) + "\";");
    System.out.println();
    System.out.println("// Pedersen blinding factor rho (32 bytes)");
    System.out.println("const char* rho_hex = \"" + HEX.encode(rho) + "\";");
    System.out.println();
    System.out.println("// Context ID (32 bytes)");
    System.out.println("const char* context_id_hex = \"" + HEX.encode(contextHash) + "\";");
    System.out.println();
    System.out.println("// Deterministic nonces: km, kr, krho (32 bytes each)");
    System.out.println("const char* km_hex = \"" + HEX.encode(nonceKm) + "\";");
    System.out.println("const char* kr_hex = \"" + HEX.encode(nonceKr) + "\";");
    System.out.println("const char* krho_hex = \"" + HEX.encode(nonceKrho) + "\";");
    System.out.println();

    // 9. Generate proof
    byte[] proof = generator.generateProof(
      c1, c2, publicKey, commitment,
      amount, r, rho, contextHash,
      nonceKm, nonceKr, nonceKrho
    );

    // Print output
    System.out.println("=== Output ===");
    System.out.println("Proof (" + proof.length + " bytes): " + HEX.encode(proof));
    System.out.println();

    // Parse and print proof components for comparison with C
    System.out.println("--- Proof Components ---");
    int offset = 0;
    byte[] t1 = new byte[33];
    System.arraycopy(proof, offset, t1, 0, 33);
    offset += 33;
    System.out.println("T1 (kr*G): " + HEX.encode(t1));

    byte[] t2 = new byte[33];
    System.arraycopy(proof, offset, t2, 0, 33);
    offset += 33;
    System.out.println("T2 (km*G + kr*Pk): " + HEX.encode(t2));

    byte[] t3 = new byte[33];
    System.arraycopy(proof, offset, t3, 0, 33);
    offset += 33;
    System.out.println("T3 (km*G + krho*H): " + HEX.encode(t3));

    byte[] sm = new byte[32];
    System.arraycopy(proof, offset, sm, 0, 32);
    offset += 32;
    System.out.println("sm: " + HEX.encode(sm));

    byte[] sr = new byte[32];
    System.arraycopy(proof, offset, sr, 0, 32);
    offset += 32;
    System.out.println("sr: " + HEX.encode(sr));

    byte[] srho = new byte[32];
    System.arraycopy(proof, offset, srho, 0, 32);
    System.out.println("srho: " + HEX.encode(srho));
    System.out.println();

    // 10. Verify proof
    boolean isValid = generator.verify(proof, c1, c2, publicKey, commitment, contextHash);
    System.out.println("Verification Result: " + isValid);

    // 11. Test that verification fails with wrong commitment
    byte[] wrongRho = new byte[32];
    for (int i = 0; i < 32; i++) {
      wrongRho[i] = 0x03;
    }
    byte[] wrongCommitmentBytes = pedersenGen.generateCommitment(amount, wrongRho);
    ECPoint wrongCommitment = Secp256k1Operations.deserialize(wrongCommitmentBytes);

    boolean isInvalid = generator.verify(proof, c1, c2, publicKey, wrongCommitment, contextHash);
    System.out.println("Verification with wrong commitment: " + isInvalid);
    System.out.println("================================================================\n");

    // Assertions
    assertThat(proof).hasSize(ElGamalPedersenLinkProofGenerator.PROOF_SIZE);
    assertThat(isValid).isTrue();
    assertThat(isInvalid).isFalse();
  }
}

