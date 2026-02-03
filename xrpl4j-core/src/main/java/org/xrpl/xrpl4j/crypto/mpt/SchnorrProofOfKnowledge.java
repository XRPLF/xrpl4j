package org.xrpl.xrpl4j.crypto.mpt;

import com.google.common.hash.Hashing;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * Generates Schnorr Proof of Knowledge (PoK) for ElGamal public key registration.
 *
 * <p>This proves knowledge of the private key corresponding to an ElGamal public key
 * without revealing the private key itself.</p>
 *
 * <p>The proof format is: T (33 bytes compressed point) || s (32 bytes scalar) = 65 bytes total.</p>
 *
 * <p>Algorithm (Schnorr Identification Scheme with Fiat-Shamir transform):
 * <ol>
 *   <li><b>Commitment:</b> Sample random nonce k and compute T = k * G</li>
 *   <li><b>Challenge (Fiat-Shamir):</b> e = reduce(SHA256("MPT_POK_SK_REGISTER" || P || T || contextId)) mod n</li>
 *   <li><b>Response:</b> s = k + e * sk (mod n)</li>
 *   <li><b>Proof:</b> Return T || s (65 bytes)</li>
 * </ol>
 * </p>
 *
 * <p>Verification checks: s * G == T + e * P</p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Schnorr_signature">Schnorr Signature</a>
 */
public class SchnorrProofOfKnowledge {

  private static final String DOMAIN_SEPARATOR = "MPT_POK_SK_REGISTER";
  private static final int PROOF_LENGTH = 65; // 33 bytes T + 32 bytes s

  private final Secp256k1Operations secp256k1;
  private final SecureRandom secureRandom;

  /**
   * Constructs a new SchnorrProofOfKnowledge generator.
   *
   * @param secp256k1    The secp256k1 operations utility.
   * @param secureRandom A secure random number generator.
   */
  public SchnorrProofOfKnowledge(Secp256k1Operations secp256k1, SecureRandom secureRandom) {
    this.secp256k1 = Objects.requireNonNull(secp256k1, "secp256k1 must not be null");
    this.secureRandom = Objects.requireNonNull(secureRandom, "secureRandom must not be null");
  }

  /**
   * Constructs a new SchnorrProofOfKnowledge generator with a default SecureRandom.
   *
   * @param secp256k1 The secp256k1 operations utility.
   */
  public SchnorrProofOfKnowledge(Secp256k1Operations secp256k1) {
    this(secp256k1, new SecureRandom());
  }

  /**
   * Generates a Schnorr Proof of Knowledge for the given private key without a contextId.
   *
   * @param privateKey The 32-byte private key (scalar).
   * @param publicKey  The corresponding public key point.
   *
   * @return A 65-byte proof (33 bytes T + 32 bytes s).
   *
   * @throws IllegalArgumentException if privateKey is not 32 bytes.
   */
  public byte[] generate(byte[] privateKey, ECPoint publicKey) {
    return generate(privateKey, publicKey, null);
  }

  /**
   * Generates a Schnorr Proof of Knowledge for the given private key.
   *
   * @param privateKey The 32-byte private key (scalar).
   * @param publicKey  The corresponding public key point.
   * @param contextId  An optional 32-byte context identifier for domain separation. Can be null.
   *
   * @return A 65-byte proof (33 bytes T + 32 bytes s).
   *
   * @throws IllegalArgumentException if privateKey is not 32 bytes or contextId is not 32 bytes (when provided).
   */
  public byte[] generate(byte[] privateKey, ECPoint publicKey, byte[] contextId) {
    Objects.requireNonNull(privateKey, "privateKey must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");

    if (privateKey.length != 32) {
      throw new IllegalArgumentException("privateKey must be 32 bytes");
    }
    if (contextId != null && contextId.length != 32) {
      throw new IllegalArgumentException("contextId must be 32 bytes when provided");
    }

    // Verify private key is valid
    BigInteger skInt = new BigInteger(1, privateKey);
    if (!secp256k1.isValidPrivateKey(skInt)) {
      throw new IllegalArgumentException("privateKey is not a valid scalar");
    }

    // 1. Sample random k and compute T = k * G
    byte[] k = RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    BigInteger kInt = new BigInteger(1, k);
    ECPoint T = secp256k1.multiplyG(kInt);

    // 2. Compute challenge e = reduce(SHA256(domainSeparator || PublicKey || T || contextId)) mod n
    byte[] eBytes = buildChallenge(publicKey, T, contextId);
    BigInteger eInt = new BigInteger(1, eBytes).mod(secp256k1.getCurveOrder());

    // 3. Compute response s = k + e * sk (mod n)
    // term = e * sk mod n
    BigInteger term = eInt.multiply(skInt).mod(secp256k1.getCurveOrder());
    // s = k + term mod n
    BigInteger sInt = kInt.add(term).mod(secp256k1.getCurveOrder());
    byte[] s = secp256k1.toBytes32(sInt);

    // 4. Serialize proof: T (33 bytes) || s (32 bytes)
    byte[] proof = new byte[PROOF_LENGTH];
    byte[] tCompressed = secp256k1.serializeCompressed(T);
    System.arraycopy(tCompressed, 0, proof, 0, 33);
    System.arraycopy(s, 0, proof, 33, 32);

    return proof;
  }

  /**
   * Builds the challenge hash for the Schnorr proof.
   *
   * <p>The challenge is computed as: SHA256("MPT_POK_SK_REGISTER" || P || T [|| contextId])</p>
   * <p>If contextId is null, it is not included in the hash.</p>
   *
   * @param publicKey The public key point P.
   * @param T         The commitment point (k * G).
   * @param contextId The optional 32-byte context identifier. Can be null.
   *
   * @return A 32-byte challenge hash (not yet reduced mod n).
   */
  private byte[] buildChallenge(ECPoint publicKey, ECPoint T, byte[] contextId) {
    byte[] domainBytes = DOMAIN_SEPARATOR.getBytes(StandardCharsets.UTF_8);
    byte[] pkBytes = secp256k1.serializeCompressed(publicKey);
    byte[] tBytes = secp256k1.serializeCompressed(T);

    // Hash: domainSeparator || PublicKey || T [|| contextId]
    int contextIdLength = (contextId != null) ? 32 : 0;
    byte[] hashInput = new byte[domainBytes.length + 33 + 33 + contextIdLength];
    int offset = 0;
    System.arraycopy(domainBytes, 0, hashInput, offset, domainBytes.length);
    offset += domainBytes.length;
    System.arraycopy(pkBytes, 0, hashInput, offset, 33);
    offset += 33;
    System.arraycopy(tBytes, 0, hashInput, offset, 33);
    offset += 33;
    if (contextId != null) {
      System.arraycopy(contextId, 0, hashInput, offset, 32);
    }

    return Hashing.sha256().hashBytes(hashInput).asBytes();
  }

  /**
   * Verifies a Schnorr Proof of Knowledge without a contextId.
   *
   * <p>Verification equation: s * G == T + e * P</p>
   *
   * @param proof     The 65-byte proof to verify.
   * @param publicKey The public key point P.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  public boolean verify(byte[] proof, ECPoint publicKey) {
    return verify(proof, publicKey, null);
  }

  /**
   * Verifies a Schnorr Proof of Knowledge.
   *
   * <p>Verification equation: s * G == T + e * P</p>
   *
   * @param proof     The 65-byte proof to verify.
   * @param publicKey The public key point P.
   * @param contextId The optional 32-byte context identifier. Can be null.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  public boolean verify(byte[] proof, ECPoint publicKey, byte[] contextId) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");

    if (proof.length != PROOF_LENGTH) {
      return false;
    }
    if (contextId != null && contextId.length != 32) {
      return false;
    }

    // 1. Parse T (33 bytes) and s (32 bytes) from proof
    byte[] tBytes = new byte[33];
    byte[] sBytes = new byte[32];
    System.arraycopy(proof, 0, tBytes, 0, 33);
    System.arraycopy(proof, 33, sBytes, 0, 32);

    ECPoint T;
    try {
      T = secp256k1.deserialize(tBytes);
    } catch (Exception e) {
      return false; // Invalid point encoding
    }

    BigInteger sInt = new BigInteger(1, sBytes);

    // Verify s is a valid scalar (0 < s < n)
    if (!secp256k1.isValidPrivateKey(sInt)) {
      return false;
    }

    // 2. Recompute challenge e = reduce(SHA256(...)) mod n
    byte[] eBytes = buildChallenge(publicKey, T, contextId);
    BigInteger eInt = new BigInteger(1, eBytes).mod(secp256k1.getCurveOrder());

    // 3. Verify equation: s * G == T + e * P
    // LHS = s * G
    ECPoint lhs = secp256k1.multiplyG(sInt);

    // RHS = T + e * P
    ECPoint ePk = secp256k1.multiply(publicKey, eInt);
    ECPoint rhs = secp256k1.add(T, ePk);

    return secp256k1.pointsEqual(lhs, rhs);
  }
}

