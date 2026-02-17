package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java;

import com.google.common.hash.Hashing;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTConvertContext;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SecretKeyProof;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SecretKeyProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKey;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPublicKey;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Java implementation of {@link SecretKeyProofGenerator} for Schnorr Proof of Knowledge.
 *
 * <p>This implementation generates proofs compatible with rippled's secp256k1_mpt_pok_sk_prove function.</p>
 *
 * <p>This implementation works with in-memory {@link ElGamalPrivateKey} instances.</p>
 *
 * @see SecretKeyProofGenerator
 * @see ElGamalPrivateKey
 * @see ConfidentialMPTConvertContext
 * @see SecretKeyProof
 */
public class JavaSecretKeyProofGenerator implements SecretKeyProofGenerator<ElGamalPrivateKey> {

  private static final String DOMAIN_SEPARATOR = "MPT_POK_SK_REGISTER";

  /**
   * Constructs a new JavaSecretKeyProofGenerator.
   */
  public JavaSecretKeyProofGenerator() {
  }

  @Override
  public SecretKeyProof generateProof(
    final ElGamalPrivateKey privateKey,
    final ConfidentialMPTConvertContext context,
    final BlindingFactor nonce
  ) {
    Objects.requireNonNull(privateKey, "privateKey must not be null");
    Objects.requireNonNull(context, "context must not be null");
    Objects.requireNonNull(nonce, "nonce must not be null");

    // Get context bytes (null-safe)
    byte[] contextId = context.toBytes();

    // Get private key bytes
    byte[] privateKeyBytes = privateKey.naturalBytes().toByteArray();

    // Verify private key is valid
    BigInteger skInt = new BigInteger(1, privateKeyBytes);
    if (!Secp256k1Operations.isValidPrivateKey(skInt)) {
      throw new IllegalArgumentException("privateKey is not a valid scalar");
    }

    // Derive public key from private key: P = sk * G
    ECPoint publicKey = Secp256k1Operations.multiplyG(skInt);

    // 1. Use provided nonce or generate random k, then compute T = k * G
    // BlindingFactor is already validated as a valid scalar
    byte[] k = nonce.toBytes();
    BigInteger kInt = new BigInteger(1, k);

    ECPoint T = Secp256k1Operations.multiplyG(kInt);

    // 2. Compute challenge e = reduce(SHA256(domainSeparator || PublicKey || T || contextId)) mod n
    // buildChallenge already returns the reduced value (mod curve order)
    byte[] eBytes = buildChallenge(publicKey, T, contextId);
    BigInteger eInt = new BigInteger(1, eBytes);

    // 3. Compute response s = k + e * sk (mod n)
    BigInteger term = eInt.multiply(skInt).mod(Secp256k1Operations.getCurveOrder());
    BigInteger sInt = kInt.add(term).mod(Secp256k1Operations.getCurveOrder());
    byte[] s = Secp256k1Operations.toBytes32(sInt);

    // 4. Serialize proof: T (33 bytes) || s (32 bytes)
    byte[] proof = new byte[SecretKeyProof.PROOF_LENGTH];
    byte[] tCompressed = Secp256k1Operations.serializeCompressed(T);
    System.arraycopy(tCompressed, 0, proof, 0, 33);
    System.arraycopy(s, 0, proof, 33, 32);

    return SecretKeyProof.fromBytes(proof);
  }

  @Override
  public boolean verifyProof(
    final SecretKeyProof proof,
    final ElGamalPublicKey publicKey,
    final ConfidentialMPTConvertContext context
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");

    // Get context bytes (null-safe)
    byte[] contextId = (context != null) ? context.toBytes() : null;

    // Get proof bytes
    byte[] proofBytes = proof.toBytes();

    // 1. Parse T (33 bytes) and s (32 bytes) from proof
    byte[] tBytes = new byte[33];
    byte[] sBytes = new byte[32];
    System.arraycopy(proofBytes, 0, tBytes, 0, 33);
    System.arraycopy(proofBytes, 33, sBytes, 0, 32);

    ECPoint T;
    try {
      T = Secp256k1Operations.deserialize(tBytes);
    } catch (Exception e) {
      return false;
    }

    BigInteger sInt = new BigInteger(1, sBytes);
    if (!Secp256k1Operations.isValidPrivateKey(sInt)) {
      return false;
    }

    // Get public key as ECPoint
    ECPoint publicKeyPoint = publicKey.asEcPoint();

    // 2. Recompute challenge e (buildChallenge already returns reduced value)
    byte[] eBytes = buildChallenge(publicKeyPoint, T, contextId);
    BigInteger eInt = new BigInteger(1, eBytes);

    // 3. Verify equation: s * G == T + e * P
    ECPoint lhs = Secp256k1Operations.multiplyG(sInt);
    ECPoint ePk = Secp256k1Operations.multiply(publicKeyPoint, eInt);
    ECPoint rhs = Secp256k1Operations.add(T, ePk);

    return Secp256k1Operations.pointsEqual(lhs, rhs);
  }

  /**
   * Builds the challenge hash for the Schnorr proof.
   *
   * <p>The challenge is computed as: SHA256("MPT_POK_SK_REGISTER" || P || T [|| contextId])</p>
   *
   * @param publicKey The public key point P.
   * @param T         The commitment point (k * G).
   * @param contextId The optional 32-byte context identifier. Can be null.
   *
   * @return A 32-byte challenge hash.
   */
  private byte[] buildChallenge(ECPoint publicKey, ECPoint T, byte[] contextId) {
    byte[] domainBytes = DOMAIN_SEPARATOR.getBytes(StandardCharsets.UTF_8);
    byte[] pkBytes = Secp256k1Operations.serializeCompressed(publicKey);
    byte[] tBytes = Secp256k1Operations.serializeCompressed(T);

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

    byte[] sha256Hash = Hashing.sha256().hashBytes(hashInput).asBytes();

    // Reduce modulo curve order (equivalent to secp256k1_mpt_scalar_reduce32 in C)
    BigInteger hashInt = new BigInteger(1, sha256Hash);
    BigInteger reduced = hashInt.mod(Secp256k1Operations.getCurveOrder());
    return Secp256k1Operations.toBytes32(reduced);
  }
}

