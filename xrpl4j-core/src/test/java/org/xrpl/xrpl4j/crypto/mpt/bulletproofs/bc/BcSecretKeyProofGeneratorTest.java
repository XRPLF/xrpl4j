package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SecretKeyProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SecretKeyProofVerifier;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTConvertContext;
import org.xrpl.xrpl4j.crypto.mpt.models.SecretKeyProof;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

/**
 * Unit tests for {@link BcSecretKeyProofGenerator}.
 */
class BcSecretKeyProofGeneratorTest {

  private SecretKeyProofGenerator proofGenerator;
  private SecretKeyProofVerifier proofVerifier;

  @BeforeEach
  void setUp() {
    proofGenerator = new BcSecretKeyProofGenerator();
    proofVerifier = new BcSecretKeyProofVerifier();
  }

  /**
   * Test that proof generation and verification works correctly.
   */
  @Test
  void testGenerateAndVerifyProof() {
    // 1. Generate keypair from passphrase
    Seed seed = Seed.elGamalSecp256k1SeedFromPassphrase(Passphrase.of("test_elgamal_key"));
    KeyPair keypair = seed.deriveKeyPair();

    PrivateKey privateKey = keypair.privateKey();
    PublicKey publicKey = keypair.publicKey();

    // 2. Fixed test values
    Address account = Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
    UnsignedInteger sequence = UnsignedInteger.valueOf(12345);
    MpTokenIssuanceId issuanceId = MpTokenIssuanceId.of("00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41");
    UnsignedLong amount = UnsignedLong.valueOf(500);

    // 3. Generate context
    ConfidentialMPTConvertContext context = ConfidentialMPTConvertContext.generate(
      account, sequence, issuanceId, amount
    );

    // 4. Generate proof (nonce is generated internally)
    SecretKeyProof proof = proofGenerator.generateProof(privateKey, context);

    // 5. Assertions
    assertThat(context.toBytes()).hasSize(32);
    assertThat(proof.toBytes()).hasSize(65);
    assertThat(proofVerifier.verifyProof(proof, publicKey, context)).isTrue();
  }
}

