package org.xrpl.xrpl4j.crypto.mpt;

import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalKeyPair;

import java.math.BigInteger;
import java.security.SecureRandom;

public class KeyPairUtils {

  /**
   * Generates a new ElGamal key pair.
   *
   * @return A new {@link ElGamalKeyPair}.
   */
  // TODO: Delete this and use existing xrpl4j instead.
  public static ElGamalKeyPair generateKeyPair(
    final SecureRandom secureRandom,
    final Secp256k1Operations secp256k1
  ) {

    //    KeyPair keyPair = Seed.secp256k1Seed().deriveKeyPair();
    //    ECPoint publicKey = secp256k1.deserialize(keyPair.publicKey().naturalBytes().toByteArray());
    //    return null;

    byte[] privateKey = new byte[32];
    BigInteger privateKeyScalar;
    //    // TODO: Limit this so it doesn't produce a runaway while loop
    // Generate random bytes until we get a valid private key
    do {
      secureRandom.nextBytes(privateKey);
      privateKeyScalar = new BigInteger(1, privateKey);
    } while (!secp256k1.isValidPrivateKey(privateKeyScalar));

    // Create the corresponding public key: Q = privateKey * G
    ECPoint publicKey = secp256k1.multiplyG(privateKeyScalar);

    return new ElGamalKeyPair(privateKey, publicKey);
  }

}
