package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc;


import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTConvertContext;
import org.xrpl.xrpl4j.crypto.mpt.models.SecretKeyProof;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SecretKeyProofGenerator;

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
public class BcSecretKeyProofGenerator implements SecretKeyProofGenerator<PrivateKey> {

  @Override
  public SecretKeyProof generateProof(
    final PrivateKey privateKey,
    final ConfidentialMPTConvertContext context
  ) {
    Objects.requireNonNull(privateKey, "privateKey must not be null");
    Objects.requireNonNull(context, "context must not be null");

    // Get context bytes
    byte[] contextId = context.toBytes();

    // Get private key scalar
    BigInteger skInt = Secp256k1Operations.toScalar(privateKey);

    // Verify private key is valid
    if (!Secp256k1Operations.isValidPrivateKey(skInt)) {
      throw new IllegalArgumentException("privateKey is not a valid scalar");
    }

    // Derive public key from private key: P = sk * G
    ECPoint publicKeyPoint = Secp256k1Operations.multiplyG(skInt);

    // 1. Generate random nonce k (matching C implementation behavior)
    BlindingFactor nonce = BlindingFactor.generate();
    byte[] k = nonce.toBytes();
    BigInteger kInt = new BigInteger(1, k);

    // Compute T = k * G
    ECPoint T = Secp256k1Operations.multiplyG(kInt);

    // 2. Compute challenge e = reduce(SHA256(domainSeparator || PublicKey || T || contextId)) mod n
    // buildChallenge already returns the reduced value (mod curve order)
    byte[] eBytes = ChallengeUtils.secretKeyProofChallenge(publicKeyPoint, T, contextId);
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
}

