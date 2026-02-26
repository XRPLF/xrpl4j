package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import com.google.common.hash.Hashing;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ChallengeUtils {

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
  public static byte[] secretKeyProofChallenge(ECPoint publicKey, ECPoint T, byte[] contextId) {
    String DOMAIN_SEPARATOR = "MPT_POK_SK_REGISTER";

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

  /**
   * Computes the Fiat-Shamir challenge hash.
   *
   * <p>Hash( Domain || {R_i, S_i, Pk_i} || Tm || {TrG_i, TrP_i} || TxID )</p>
   */
  public static byte[] samePlaintextProofChallenge(
    int n,
    List<ECPoint> R,
    List<ECPoint> S,
    List<ECPoint> Pk,
    ECPoint Tm,
    List<ECPoint> TrG,
    List<ECPoint> TrP,
    byte[] txId
  ) {
    String DOMAIN_SEPARATOR = "MPT_POK_SAME_PLAINTEXT_PROOF";
    // Calculate total size for buffer
    // Domain + n*(R+S+Pk) + Tm + n*(TrG+TrP) + txId
    int domainLen = DOMAIN_SEPARATOR.getBytes(StandardCharsets.UTF_8).length;
    int pointsLen = (3 * n + 1 + 2 * n) * 33; // (R,S,Pk)*n + Tm + (TrG,TrP)*n
    int txIdLen = (txId != null) ? 32 : 0;

    byte[] buffer = new byte[domainLen + pointsLen + txIdLen];
    int offset = 0;

    // Domain separator
    byte[] domainBytes = DOMAIN_SEPARATOR.getBytes(StandardCharsets.UTF_8);
    System.arraycopy(domainBytes, 0, buffer, offset, domainBytes.length);
    offset += domainBytes.length;

    // Public inputs: {R_i, S_i, Pk_i}
    for (int i = 0; i < n; i++) {
      byte[] rBytes = Secp256k1Operations.serializeCompressed(R.get(i));
      System.arraycopy(rBytes, 0, buffer, offset, 33);
      offset += 33;

      byte[] sBytes = Secp256k1Operations.serializeCompressed(S.get(i));
      System.arraycopy(sBytes, 0, buffer, offset, 33);
      offset += 33;

      byte[] pkBytes = Secp256k1Operations.serializeCompressed(Pk.get(i));
      System.arraycopy(pkBytes, 0, buffer, offset, 33);
      offset += 33;
    }

    // Commitments: Tm
    byte[] tmBytes = Secp256k1Operations.serializeCompressed(Tm);
    System.arraycopy(tmBytes, 0, buffer, offset, 33);
    offset += 33;

    // Commitments: {TrG_i, TrP_i}
    for (int i = 0; i < n; i++) {
      byte[] trgBytes = Secp256k1Operations.serializeCompressed(TrG.get(i));
      System.arraycopy(trgBytes, 0, buffer, offset, 33);
      offset += 33;

      byte[] trpBytes = Secp256k1Operations.serializeCompressed(TrP.get(i));
      System.arraycopy(trpBytes, 0, buffer, offset, 33);
      offset += 33;
    }

    // Context (tx_id)
    if (txId != null) {
      System.arraycopy(txId, 0, buffer, offset, 32);
    }

    // SHA256 and reduce to scalar
    byte[] hash = Hashing.sha256().hashBytes(buffer).asBytes();
    return Secp256k1Operations.reduceToScalar(hash);
  }


  /**
   * Computes the challenge hash for the equality plaintext proof.
   */
  public static byte[] plaintextEqualityProofChallenge(
    ECPoint c1,
    ECPoint c2,
    ECPoint publicKey,
    ECPoint mG,
    ECPoint T1,
    ECPoint T2,
    byte[] contextId
  ) {
    String DOMAIN_SEPARATOR = "MPT_POK_PLAINTEXT_PROOF";
    // Calculate total size: domain + c1 + c2 + pk + [mG] + T1 + T2 + context
    int size = DOMAIN_SEPARATOR.length() + 33 + 33 + 33 + 33 + 33 + 32;
    if (mG != null) {
      size += 33;
    }

    byte[] hashInput = new byte[size];
    int offset = 0;

    // Domain separator
    byte[] domainBytes = DOMAIN_SEPARATOR.getBytes(StandardCharsets.UTF_8);
    System.arraycopy(domainBytes, 0, hashInput, offset, domainBytes.length);
    offset += domainBytes.length;

    // C1, C2, Pk
    byte[] c1Bytes = Secp256k1Operations.serializeCompressed(c1);
    System.arraycopy(c1Bytes, 0, hashInput, offset, 33);
    offset += 33;

    byte[] c2Bytes = Secp256k1Operations.serializeCompressed(c2);
    System.arraycopy(c2Bytes, 0, hashInput, offset, 33);
    offset += 33;

    byte[] pkBytes = Secp256k1Operations.serializeCompressed(publicKey);
    System.arraycopy(pkBytes, 0, hashInput, offset, 33);
    offset += 33;

    // mG (only if nonzero)
    if (mG != null) {
      byte[] mGBytes = Secp256k1Operations.serializeCompressed(mG);
      System.arraycopy(mGBytes, 0, hashInput, offset, 33);
      offset += 33;
    }

    // T1, T2
    byte[] t1Bytes = Secp256k1Operations.serializeCompressed(T1);
    System.arraycopy(t1Bytes, 0, hashInput, offset, 33);
    offset += 33;

    byte[] t2Bytes = Secp256k1Operations.serializeCompressed(T2);
    System.arraycopy(t2Bytes, 0, hashInput, offset, 33);
    offset += 33;

    // Context ID (always required)
    System.arraycopy(contextId, 0, hashInput, offset, 32);

    // SHA256 and reduce mod curve order
    byte[] sha256Hash = Hashing.sha256().hashBytes(hashInput).asBytes();
    BigInteger hashInt = new BigInteger(1, sha256Hash);
    BigInteger reduced = hashInt.mod(Secp256k1Operations.getCurveOrder());
    return Secp256k1Operations.toBytes32(reduced);
  }

  public static byte[] pedersenLinkProofChallenge(
    ECPoint c1, ECPoint c2, ECPoint pk, ECPoint pcm,
    ECPoint T1, ECPoint T2, ECPoint T3,
    byte[] contextId
  ) {
    String DOMAIN_SEPARATOR = "MPT_ELGAMAL_PEDERSEN_LINK";
    byte[] domainBytes = DOMAIN_SEPARATOR.getBytes(StandardCharsets.UTF_8);
    int contextLen = (contextId != null) ? 32 : 0;
    int bufferSize = domainBytes.length + (7 * 33) + contextLen;

    byte[] buffer = new byte[bufferSize];
    int offset = 0;

    System.arraycopy(domainBytes, 0, buffer, offset, domainBytes.length);
    offset += domainBytes.length;

    offset = Secp256k1Operations.appendPoint(buffer, offset, c1);
    offset = Secp256k1Operations.appendPoint(buffer, offset, c2);
    offset = Secp256k1Operations.appendPoint(buffer, offset, pk);
    offset = Secp256k1Operations.appendPoint(buffer, offset, pcm);
    offset = Secp256k1Operations.appendPoint(buffer, offset, T1);
    offset = Secp256k1Operations.appendPoint(buffer, offset, T2);
    offset = Secp256k1Operations.appendPoint(buffer, offset, T3);

    if (contextId != null) {
      System.arraycopy(contextId, 0, buffer, offset, 32);
    }

    byte[] hash = Hashing.sha256().hashBytes(buffer).asBytes();
    return Secp256k1Operations.reduceToScalar(hash);
  }

}
