package org.xrpl.xrpl4j.crypto.mpt.elgamal.java;

import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPublicKey;

/**
 * Base class for ElGamal tests.
 */
public abstract class AbstractElGamalTest {

  protected KeyPair keyPair;

  @BeforeEach
  void setUpKeyPair() {
    this.keyPair = this.randomKeyPair();
  }

  protected final KeyPair randomKeyPair() {
    return Seed.secp256k1Seed().deriveKeyPair();
  }

  protected final ECPoint toPublicKey(PublicKey publicKey) {
    return BcKeyUtils.toEcPublicKeyParameters(publicKey).getQ();
  }

  protected final ElGamalPublicKey toElGamalPublicKey(PublicKey publicKey) {
    ECPoint ecPoint = BcKeyUtils.toEcPublicKeyParameters(publicKey).getQ();
    return ElGamalPublicKey.fromEcPoint(ecPoint);
  }

}
