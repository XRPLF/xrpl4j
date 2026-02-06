package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java;

import com.google.common.hash.Hashing;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SamePlaintextMultiProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Java implementation of the Same Plaintext Multi Proof generator.
 *
 * <p>This implements the Sigma protocol for proving that N ElGamal ciphertexts
 * all encrypt the same plaintext amount.</p>
 */
public class JavaSamePlaintextMultiProofGenerator implements SamePlaintextMultiProofGenerator {

  private static final String DOMAIN_SEPARATOR = "MPT_POK_SAME_PLAINTEXT_PROOF";

  private final Secp256k1Operations secp256k1;

  /**
   * Constructs a new generator with the given secp256k1 operations.
   *
   * @param secp256k1 The secp256k1 operations instance.
   */
  public JavaSamePlaintextMultiProofGenerator(Secp256k1Operations secp256k1) {
    this.secp256k1 = Objects.requireNonNull(secp256k1, "secp256k1 must not be null");
  }

  @Override
  public byte[] generateProof(
    UnsignedLong amount,
    List<ElGamalCiphertext> ciphertexts,
    List<ECPoint> publicKeys,
    List<byte[]> blindingFactors,
    byte[] contextHash,
    byte[] nonceKm,
    List<byte[]> noncesKr
  ) {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(ciphertexts, "ciphertexts must not be null");
    Objects.requireNonNull(publicKeys, "publicKeys must not be null");
    Objects.requireNonNull(blindingFactors, "blindingFactors must not be null");
    Objects.requireNonNull(nonceKm, "nonceKm must not be null");
    Objects.requireNonNull(noncesKr, "noncesKr must not be null");

    int n = ciphertexts.size();
    if (publicKeys.size() != n || blindingFactors.size() != n || noncesKr.size() != n) {
      throw new IllegalArgumentException("All lists must have the same size");
    }
    if (nonceKm.length != 32) {
      throw new IllegalArgumentException("nonceKm must be 32 bytes");
    }

    // Extract R and S from ciphertexts
    List<ECPoint> R = new ArrayList<>(n);
    List<ECPoint> S = new ArrayList<>(n);
    for (ElGamalCiphertext ct : ciphertexts) {
      R.add(ct.c1());
      S.add(ct.c2());
    }

    // 1. Generate Commitments
    // Tm = km * G
    BigInteger kmInt = new BigInteger(1, nonceKm);
    ECPoint Tm = secp256k1.multiplyG(kmInt);

    List<ECPoint> TrG = new ArrayList<>(n);
    List<ECPoint> TrP = new ArrayList<>(n);

    for (int i = 0; i < n; i++) {
      byte[] kri = noncesKr.get(i);
      if (kri.length != 32) {
        throw new IllegalArgumentException("Each nonce in noncesKr must be 32 bytes");
      }
      BigInteger kriInt = new BigInteger(1, kri);

      // TrG[i] = kri * G
      TrG.add(secp256k1.multiplyG(kriInt));

      // TrP[i] = kri * Pk[i]
      TrP.add(secp256k1.multiply(publicKeys.get(i), kriInt));
    }

    // 2. Compute Challenge
    byte[] e = computeChallenge(n, R, S, publicKeys, Tm, TrG, TrP, contextHash);
    BigInteger eInt = new BigInteger(1, e);

    // 3. Compute Responses
    // s_m = k_m + e * m (mod n)
    byte[] mScalar = secp256k1.unsignedLongToScalar(amount);
    BigInteger mInt = new BigInteger(1, mScalar);
    BigInteger smInt = kmInt.add(eInt.multiply(mInt)).mod(secp256k1.getCurveOrder());
    byte[] sm = secp256k1.toBytes32(smInt);

    // s_ri = k_ri + e * r_i (mod n)
    List<byte[]> sr = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      BigInteger kriInt = new BigInteger(1, noncesKr.get(i));
      BigInteger riInt = new BigInteger(1, blindingFactors.get(i));
      BigInteger sriInt = kriInt.add(eInt.multiply(riInt)).mod(secp256k1.getCurveOrder());
      sr.add(secp256k1.toBytes32(sriInt));
    }

    // 4. Serialize proof: Tm || TrG[0..N-1] || TrP[0..N-1] || sm || sr[0..N-1]
    return serializeProof(n, Tm, TrG, TrP, sm, sr);
  }

  /**
   * Serializes the proof components to bytes.
   */
  private byte[] serializeProof(int n, ECPoint Tm, List<ECPoint> TrG, List<ECPoint> TrP, byte[] sm, List<byte[]> sr) {
    int size = SamePlaintextMultiProofGenerator.proofSize(n);
    byte[] result = new byte[size];
    int offset = 0;

    // Tm (33 bytes)
    byte[] tmBytes = secp256k1.serializeCompressed(Tm);
    System.arraycopy(tmBytes, 0, result, offset, 33);
    offset += 33;

    // TrG[0..N-1] (N * 33 bytes)
    for (ECPoint trg : TrG) {
      byte[] trgBytes = secp256k1.serializeCompressed(trg);
      System.arraycopy(trgBytes, 0, result, offset, 33);
      offset += 33;
    }

    // TrP[0..N-1] (N * 33 bytes)
    for (ECPoint trp : TrP) {
      byte[] trpBytes = secp256k1.serializeCompressed(trp);
      System.arraycopy(trpBytes, 0, result, offset, 33);
      offset += 33;
    }

    // sm (32 bytes)
    System.arraycopy(sm, 0, result, offset, 32);
    offset += 32;

    // sr[0..N-1] (N * 32 bytes)
    for (byte[] sri : sr) {
      System.arraycopy(sri, 0, result, offset, 32);
      offset += 32;
    }

    return result;
  }

  @Override
  public boolean verify(
    byte[] proof,
    List<ElGamalCiphertext> ciphertexts,
    List<ECPoint> publicKeys,
    byte[] contextHash
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(ciphertexts, "ciphertexts must not be null");
    Objects.requireNonNull(publicKeys, "publicKeys must not be null");

    int n = ciphertexts.size();
    if (publicKeys.size() != n) {
      return false;
    }

    // Validate proof size
    int expectedSize = SamePlaintextMultiProofGenerator.proofSize(n);
    if (proof.length != expectedSize) {
      return false;
    }

    // Deserialize proof
    int offset = 0;

    // Tm (33 bytes)
    byte[] tmBytes = new byte[33];
    System.arraycopy(proof, offset, tmBytes, 0, 33);
    ECPoint Tm = secp256k1.deserialize(tmBytes);
    offset += 33;

    // TrG[0..N-1] (N * 33 bytes)
    List<ECPoint> TrG = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      byte[] trgBytes = new byte[33];
      System.arraycopy(proof, offset, trgBytes, 0, 33);
      ECPoint trg = secp256k1.deserialize(trgBytes);
      TrG.add(trg);
      offset += 33;
    }

    // TrP[0..N-1] (N * 33 bytes)
    List<ECPoint> TrP = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      byte[] trpBytes = new byte[33];
      System.arraycopy(proof, offset, trpBytes, 0, 33);
      ECPoint trp = secp256k1.deserialize(trpBytes);
      TrP.add(trp);
      offset += 33;
    }

    // sm (32 bytes)
    byte[] sm = new byte[32];
    System.arraycopy(proof, offset, sm, 0, 32);
    if (!secp256k1.isValidScalar(sm)) {
      return false;
    }
    offset += 32;

    // sr[0..N-1] (N * 32 bytes)
    List<byte[]> sr = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      byte[] sri = new byte[32];
      System.arraycopy(proof, offset, sri, 0, 32);
      if (!secp256k1.isValidScalar(sri)) {
        return false;
      }
      sr.add(sri);
      offset += 32;
    }

    // Extract R and S from ciphertexts
    List<ECPoint> R = new ArrayList<>(n);
    List<ECPoint> S = new ArrayList<>(n);
    for (ElGamalCiphertext ct : ciphertexts) {
      R.add(ct.c1());
      S.add(ct.c2());
    }

    // Recompute challenge
    byte[] e = computeChallenge(n, R, S, publicKeys, Tm, TrG, TrP, contextHash);
    BigInteger eInt = new BigInteger(1, e);

    // Precompute s_m * G
    BigInteger smInt = new BigInteger(1, sm);
    ECPoint SmG = secp256k1.multiplyG(smInt);

    // Verify equations for each i
    for (int i = 0; i < n; i++) {
      BigInteger sriInt = new BigInteger(1, sr.get(i));

      // Eq 1: s_ri * G == TrG[i] + e * R[i]
      ECPoint lhs1 = secp256k1.multiplyG(sriInt);
      ECPoint eRi = secp256k1.multiply(R.get(i), eInt);
      ECPoint rhs1 = secp256k1.add(TrG.get(i), eRi);

      if (!secp256k1.pointsEqual(lhs1, rhs1)) {
        return false;
      }

      // Eq 2: s_m * G + s_ri * Pk[i] == Tm + TrP[i] + e * S[i]
      ECPoint sriPki = secp256k1.multiply(publicKeys.get(i), sriInt);
      ECPoint lhs2 = secp256k1.add(SmG, sriPki);

      ECPoint eSi = secp256k1.multiply(S.get(i), eInt);
      ECPoint tmTrp = secp256k1.add(Tm, TrP.get(i));
      ECPoint rhs2 = secp256k1.add(tmTrp, eSi);

      if (!secp256k1.pointsEqual(lhs2, rhs2)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Computes the Fiat-Shamir challenge hash.
   *
   * <p>Hash( Domain || {R_i, S_i, Pk_i} || Tm || {TrG_i, TrP_i} || TxID )</p>
   */
  private byte[] computeChallenge(
    int n,
    List<ECPoint> R,
    List<ECPoint> S,
    List<ECPoint> Pk,
    ECPoint Tm,
    List<ECPoint> TrG,
    List<ECPoint> TrP,
    byte[] txId
  ) {
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
      byte[] rBytes = secp256k1.serializeCompressed(R.get(i));
      System.arraycopy(rBytes, 0, buffer, offset, 33);
      offset += 33;

      byte[] sBytes = secp256k1.serializeCompressed(S.get(i));
      System.arraycopy(sBytes, 0, buffer, offset, 33);
      offset += 33;

      byte[] pkBytes = secp256k1.serializeCompressed(Pk.get(i));
      System.arraycopy(pkBytes, 0, buffer, offset, 33);
      offset += 33;
    }

    // Commitments: Tm
    byte[] tmBytes = secp256k1.serializeCompressed(Tm);
    System.arraycopy(tmBytes, 0, buffer, offset, 33);
    offset += 33;

    // Commitments: {TrG_i, TrP_i}
    for (int i = 0; i < n; i++) {
      byte[] trgBytes = secp256k1.serializeCompressed(TrG.get(i));
      System.arraycopy(trgBytes, 0, buffer, offset, 33);
      offset += 33;

      byte[] trpBytes = secp256k1.serializeCompressed(TrP.get(i));
      System.arraycopy(trpBytes, 0, buffer, offset, 33);
      offset += 33;
    }

    // Context (tx_id)
    if (txId != null) {
      System.arraycopy(txId, 0, buffer, offset, 32);
    }

    // SHA256 and reduce to scalar
    byte[] hash = Hashing.sha256().hashBytes(buffer).asBytes();
    return reduceToScalar(hash);
  }

  /**
   * Reduces a 32-byte hash to a valid scalar (mod curve order).
   */
  private byte[] reduceToScalar(byte[] hash) {
    BigInteger hashInt = new BigInteger(1, hash);
    BigInteger reduced = hashInt.mod(secp256k1.getCurveOrder());
    return secp256k1.toBytes32(reduced);
  }
}

