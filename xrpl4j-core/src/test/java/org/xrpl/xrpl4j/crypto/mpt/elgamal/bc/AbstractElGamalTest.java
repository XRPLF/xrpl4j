package org.xrpl.xrpl4j.crypto.mpt.elgamal.bc;

import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;

/**
 * Base class for ElGamal tests.
 */
public abstract class AbstractElGamalTest {

  protected KeyPair keyPair;

  @BeforeEach
  void setUpKeyPair() {
    this.keyPair = this.randomElGamalKeyPair();
  }

  protected final KeyPair randomElGamalKeyPair() {
    return Seed.elGamalSecp256k1Seed().deriveKeyPair();
  }

  protected final ECPoint toEcPoint(PublicKey publicKey) {
    return Secp256k1Operations.toEcPoint(publicKey);
  }

}
