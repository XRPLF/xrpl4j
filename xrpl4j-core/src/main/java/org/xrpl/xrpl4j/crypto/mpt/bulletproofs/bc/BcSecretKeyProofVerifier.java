package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc;


import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SecretKeyProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SecretKeyProofVerifier;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTConvertContext;
import org.xrpl.xrpl4j.crypto.mpt.tmp.SecretKeyProof;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Java implementation of {@link SecretKeyProofGenerator} for Schnorr Proof of Knowledge.
 *
 * <p>This implementation generates proofs compatible with rippled's secp256k1_mpt_pok_sk_prove function.</p>
 *
 * <p>This implementation works with in-memory {@link PrivateKey} instances.</p>
 *
 * @see SecretKeyProofGenerator
 * @see PrivateKey
 * @see ConfidentialMPTConvertContext
 * @see SecretKeyProof
 */
public class BcSecretKeyProofVerifier implements SecretKeyProofVerifier {
  @Override
  public boolean verifyProof(
    final SecretKeyProof proof,
    final PublicKey publicKey,
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
    ECPoint publicKeyPoint = Secp256k1Operations.toEcPoint(publicKey);

    // 2. Recompute challenge e (buildChallenge already returns reduced value)
    byte[] eBytes = ChallengeUtils.secretKeyProofChallenge(publicKeyPoint, T, contextId);
    BigInteger eInt = new BigInteger(1, eBytes);

    // 3. Verify equation: s * G == T + e * P
    ECPoint lhs = Secp256k1Operations.multiplyG(sInt);
    ECPoint ePk = Secp256k1Operations.multiply(publicKeyPoint, eInt);
    ECPoint rhs = Secp256k1Operations.add(T, ePk);

    return Secp256k1Operations.pointsEqual(lhs, rhs);
  }

}

