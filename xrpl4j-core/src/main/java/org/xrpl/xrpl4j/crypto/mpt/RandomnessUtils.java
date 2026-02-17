package org.xrpl.xrpl4j.crypto.mpt;

import org.xrpl.xrpl4j.crypto.SecureRandomUtils;
import org.xrpl.xrpl4j.crypto.keys.Seed;

import java.security.SecureRandom;

/**
 * Utility class for working with randomness.
 *
 * @deprecated Use {@link Seed} or something from xrpl4j core instead.
 */
@Deprecated
public class RandomnessUtils {

  /**
   * Generates a cryptographically secure random 32-byte scalar using {@link SecureRandomUtils}.
   *
   * @param secp256k1 The secp256k1 operations instance for scalar validation.
   *
   * @return A valid random scalar.
   */
  public static byte[] generateRandomScalar(final Secp256k1Operations secp256k1) {
    return generateRandomScalar(SecureRandomUtils.secureRandom(), secp256k1);
  }

  /**
   * Generates a cryptographically secure random 32-byte scalar.
   *
   * @param secureRandom The SecureRandom instance to use.
   * @param secp256k1    The secp256k1 operations instance for scalar validation.
   *
   * @return A valid random scalar.
   *
   * @deprecated Use {@link #generateRandomScalar(Secp256k1Operations)} instead.
   */
  @Deprecated
  public static byte[] generateRandomScalar(
    final SecureRandom secureRandom,
    final Secp256k1Operations secp256k1
  ) {

    // TODO: Ideally use Entropy, but that is only 16 bytes.
    // Option1: Use a 16-byte seed, but that's likely too small for MPT operations?
    // Option2: Expand Seed to use 32 bytes, but this likely doesn't conform to XRPL address generation stadards?
    // Option3: Something else?
//    Seed seed = Seed.secp256k1Seed();
//    return seed.decodedSeed().bytes().toByteArray();

    byte[] scalar = new byte[32];
    do {
      secureRandom.nextBytes(scalar);
    } while (!secp256k1.isValidScalar(scalar));
    return scalar;
  }
}
