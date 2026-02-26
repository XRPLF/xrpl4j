package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc;

import com.google.common.hash.Hashing;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.LinkageProofType;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.PedersenLinkProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.PedersenLinkProofVerifier;
import org.xrpl.xrpl4j.crypto.mpt.context.LinkProofContext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.ElGamalPedersenLinkProof;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.PedersenCommitment;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Java implementation of the ElGamal-Pedersen Link Proof generator.
 *
 * <p>This implements the Sigma protocol for proving that an ElGamal ciphertext
 * and a Pedersen commitment encode the same plaintext amount.</p>
 */
public class BcPedersenLinkProofVerifier implements PedersenLinkProofVerifier {

  private static final String DOMAIN_SEPARATOR = "MPT_ELGAMAL_PEDERSEN_LINK";
  private static final String NUMS_DOMAIN_SEPARATOR = "MPT_BULLETPROOF_V1_NUMS";
  private static final String CURVE_LABEL = "secp256k1";
  private static final int PROOF_SIZE = 195;

  private ECPoint cachedH;

  @Override
  public boolean verify(
    LinkageProofType proofType,
    ElGamalPedersenLinkProof proof,
    ElGamalCiphertext ciphertext,
    PublicKey publicKey,
    PedersenCommitment commitment,
    LinkProofContext context
  ) {
    Objects.requireNonNull(proofType, "proofType must not be null");
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(commitment, "commitment must not be null");

    // Order parameters based on proof type
    ECPoint c1, c2, pk;
    if (proofType == LinkageProofType.AMOUNT_COMMITMENT) {
      c1 = ciphertext.c1();
      c2 = ciphertext.c2();
      pk = Secp256k1Operations.toEcPoint(publicKey);
    } else {
      c1 = Secp256k1Operations.toEcPoint(publicKey);
      c2 = ciphertext.c2();
      pk = ciphertext.c1();
    }

    ECPoint commitmentPoint = commitment.asEcPoint();
    byte[] contextHash = (context != null) ? context.toBytes() : null;
    byte[] proofBytes = proof.toBytes();

    // 1. Deserialize proof
    int offset = 0;

    byte[] t1Bytes = new byte[33];
    System.arraycopy(proofBytes, offset, t1Bytes, 0, 33);
    ECPoint T1 = Secp256k1Operations.deserialize(t1Bytes);
    offset += 33;

    byte[] t2Bytes = new byte[33];
    System.arraycopy(proofBytes, offset, t2Bytes, 0, 33);
    ECPoint T2 = Secp256k1Operations.deserialize(t2Bytes);
    offset += 33;

    byte[] t3Bytes = new byte[33];
    System.arraycopy(proofBytes, offset, t3Bytes, 0, 33);
    ECPoint T3 = Secp256k1Operations.deserialize(t3Bytes);
    offset += 33;

    byte[] sm = new byte[32];
    System.arraycopy(proofBytes, offset, sm, 0, 32);
    if (!Secp256k1Operations.isValidScalar(sm)) return false;
    offset += 32;

    byte[] sr = new byte[32];
    System.arraycopy(proofBytes, offset, sr, 0, 32);
    if (!Secp256k1Operations.isValidScalar(sr)) return false;
    offset += 32;

    byte[] srho = new byte[32];
    System.arraycopy(proofBytes, offset, srho, 0, 32);
    if (!Secp256k1Operations.isValidScalar(srho)) return false;

    BigInteger smInt = new BigInteger(1, sm);
    BigInteger srInt = new BigInteger(1, sr);
    BigInteger srhoInt = new BigInteger(1, srho);

    // 2. Recompute challenge
    byte[] e = ChallengeUtils.pedersenLinkProofChallenge(c1, c2, pk, commitmentPoint, T1, T2, T3, contextHash);
    BigInteger eInt = new BigInteger(1, e);

    // 3. Verification equations

    // Eq 1: sr * G == T1 + e * C1
    ECPoint lhs1 = Secp256k1Operations.multiplyG(srInt);
    ECPoint eC1 = Secp256k1Operations.multiply(c1, eInt);
    ECPoint rhs1 = Secp256k1Operations.add(T1, eC1);
    if (!Secp256k1Operations.pointsEqual(lhs1, rhs1)) return false;

    // Eq 2: sm * G + sr * Pk == T2 + e * C2
    ECPoint smG = Secp256k1Operations.multiplyG(smInt);
    ECPoint srPk = Secp256k1Operations.multiply(pk, srInt);
    ECPoint lhs2 = Secp256k1Operations.add(smG, srPk);
    ECPoint eC2 = Secp256k1Operations.multiply(c2, eInt);
    ECPoint rhs2 = Secp256k1Operations.add(T2, eC2);
    if (!Secp256k1Operations.pointsEqual(lhs2, rhs2)) return false;

    // Eq 3: sm * G + srho * H == T3 + e * PCm
    ECPoint H = getHGenerator();
    ECPoint srhoH = Secp256k1Operations.multiply(H, srhoInt);
    ECPoint lhs3 = Secp256k1Operations.add(smG, srhoH);
    ECPoint ePcm = Secp256k1Operations.multiply(commitmentPoint, eInt);
    ECPoint rhs3 = Secp256k1Operations.add(T3, ePcm);
    if (!Secp256k1Operations.pointsEqual(lhs3, rhs3)) return false;

    return true;
  }

  private ECPoint getHGenerator() {
    if (cachedH == null) {
      cachedH = hashToPointNums("H".getBytes(StandardCharsets.UTF_8), 0);
    }
    return cachedH;
  }

  private ECPoint hashToPointNums(byte[] label, int index) {
    byte[] domainBytes = NUMS_DOMAIN_SEPARATOR.getBytes(StandardCharsets.UTF_8);
    byte[] curveBytes = CURVE_LABEL.getBytes(StandardCharsets.UTF_8);
    byte[] indexBe = intToBigEndian(index);

    for (long ctr = 0; ctr < 0xFFFFFFFFL; ctr++) {
      byte[] ctrBe = intToBigEndian((int) ctr);

      int inputLen = domainBytes.length + curveBytes.length +
        (label != null ? label.length : 0) + 4 + 4;
      byte[] hashInput = new byte[inputLen];
      int offset = 0;

      System.arraycopy(domainBytes, 0, hashInput, offset, domainBytes.length);
      offset += domainBytes.length;
      System.arraycopy(curveBytes, 0, hashInput, offset, curveBytes.length);
      offset += curveBytes.length;
      if (label != null && label.length > 0) {
        System.arraycopy(label, 0, hashInput, offset, label.length);
        offset += label.length;
      }
      System.arraycopy(indexBe, 0, hashInput, offset, 4);
      offset += 4;
      System.arraycopy(ctrBe, 0, hashInput, offset, 4);

      byte[] hash = HashingUtils.sha256(hashInput).toByteArray();
      byte[] compressed = new byte[33];
      compressed[0] = 0x02;
      System.arraycopy(hash, 0, compressed, 1, 32);

      try {
        ECPoint point = Secp256k1Operations.deserialize(compressed);
        if (point != null && !point.isInfinity()) {
          return point;
        }
      } catch (Exception e) {
        // Invalid point, continue
      }
    }
    throw new IllegalStateException("Failed to derive NUMS point");
  }

  private byte[] intToBigEndian(int value) {
    return new byte[] {
      (byte) (value >> 24),
      (byte) (value >> 16),
      (byte) (value >> 8),
      (byte) value
    };
  }
}
