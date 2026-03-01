package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.PedersenLinkProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.LinkageProofType;
import org.xrpl.xrpl4j.crypto.mpt.context.LinkProofContext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.tmp.ElGamalPedersenLinkProof;
import org.xrpl.xrpl4j.crypto.mpt.tmp.PedersenCommitment;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Java implementation of the ElGamal-Pedersen Link Proof generator.
 *
 * <p>This implements the Sigma protocol for proving that an ElGamal ciphertext
 * and a Pedersen commitment encode the same plaintext amount.</p>
 */
public class BcPedersenLinkProofGenerator implements PedersenLinkProofGenerator {

  private static final String NUMS_DOMAIN_SEPARATOR = "MPT_BULLETPROOF_V1_NUMS";
  private static final String CURVE_LABEL = "secp256k1";
  private static final int PROOF_SIZE = 195;

  private ECPoint cachedH;

  @Override
  public ElGamalPedersenLinkProof generateProof(
    LinkageProofType proofType,
    ElGamalCiphertext ciphertext,
    PublicKey publicKey,
    PedersenCommitment commitment,
    UnsignedLong amount,
    BlindingFactor elGamalBlindingFactor,
    BlindingFactor pedersenBlindingFactor,
    LinkProofContext context
  ) {
    Objects.requireNonNull(proofType, "proofType must not be null");
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(commitment, "commitment must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(elGamalBlindingFactor, "elGamalBlindingFactor must not be null");
    Objects.requireNonNull(pedersenBlindingFactor, "pedersenBlindingFactor must not be null");
    Objects.requireNonNull(context, "context must not be null");

    // Order parameters based on proof type
    ECPoint c1, c2, pk;
    if (proofType == LinkageProofType.AMOUNT_COMMITMENT) {
      // Amount linkage: c1=ciphertext.c1, c2=ciphertext.c2, pk=publicKey
      c1 = ciphertext.c1();
      c2 = ciphertext.c2();
      pk = Secp256k1Operations.toEcPoint(publicKey);
    } else {
      // Balance linkage: c1=publicKey (sk*G), c2=ciphertext.c2, pk=ciphertext.c1
      c1 = Secp256k1Operations.toEcPoint(publicKey);
      c2 = ciphertext.c2();
      pk = ciphertext.c1();
    }

    byte[] r = elGamalBlindingFactor.toBytes();
    byte[] rho = pedersenBlindingFactor.toBytes();
    ECPoint commitmentPoint = commitment.asEcPoint();
    byte[] contextHash = context.toBytes();

    // Generate nonces internally (matching C implementation)
    BlindingFactor nonceKm = BlindingFactor.generate();
    BlindingFactor nonceKr = BlindingFactor.generate();
    BlindingFactor nonceKrho = BlindingFactor.generate();

    BigInteger kmInt = new BigInteger(1, nonceKm.toBytes());
    BigInteger krInt = new BigInteger(1, nonceKr.toBytes());
    BigInteger krhoInt = new BigInteger(1, nonceKrho.toBytes());

    // 1. Compute Commitments
    // T1 = kr * G
    ECPoint T1 = Secp256k1Operations.multiplyG(krInt);

    // T2 = km * G + kr * Pk
    ECPoint kmG = Secp256k1Operations.multiplyG(kmInt);
    ECPoint krPk = Secp256k1Operations.multiply(pk, krInt);
    ECPoint T2 = Secp256k1Operations.add(kmG, krPk);

    // T3 = km * G + krho * H
    ECPoint H = getHGenerator();
    ECPoint krhoH = Secp256k1Operations.multiply(H, krhoInt);
    ECPoint T3 = Secp256k1Operations.add(kmG, krhoH);

    // 2. Compute Challenge
    byte[] e = ChallengeUtils.pedersenLinkProofChallenge(c1, c2, pk, commitmentPoint, T1, T2, T3, contextHash);
    BigInteger eInt = new BigInteger(1, e);

    // 3. Compute Responses
    byte[] mScalar = Secp256k1Operations.unsignedLongToScalar(amount);
    BigInteger mInt = new BigInteger(1, mScalar);
    BigInteger rInt = new BigInteger(1, r);
    BigInteger rhoInt = new BigInteger(1, rho);

    // sm = km + e * m (mod n)
    BigInteger smInt = kmInt.add(eInt.multiply(mInt)).mod(Secp256k1Operations.getCurveOrder());
    byte[] sm = Secp256k1Operations.toBytes32(smInt);

    // sr = kr + e * r (mod n)
    BigInteger srInt = krInt.add(eInt.multiply(rInt)).mod(Secp256k1Operations.getCurveOrder());
    byte[] sr = Secp256k1Operations.toBytes32(srInt);

    // srho = krho + e * rho (mod n)
    BigInteger srhoInt = krhoInt.add(eInt.multiply(rhoInt)).mod(Secp256k1Operations.getCurveOrder());
    byte[] srho = Secp256k1Operations.toBytes32(srhoInt);

    // 4. Serialize Proof (195 bytes)
    byte[] proofBytes = serializeProof(T1, T2, T3, sm, sr, srho);
    return ElGamalPedersenLinkProof.fromBytes(proofBytes);
  }

  private byte[] serializeProof(ECPoint T1, ECPoint T2, ECPoint T3, byte[] sm, byte[] sr, byte[] srho) {
    byte[] result = new byte[PROOF_SIZE];
    int offset = 0;

    byte[] t1Bytes = Secp256k1Operations.serializeCompressed(T1);
    System.arraycopy(t1Bytes, 0, result, offset, 33);
    offset += 33;

    byte[] t2Bytes = Secp256k1Operations.serializeCompressed(T2);
    System.arraycopy(t2Bytes, 0, result, offset, 33);
    offset += 33;

    byte[] t3Bytes = Secp256k1Operations.serializeCompressed(T3);
    System.arraycopy(t3Bytes, 0, result, offset, 33);
    offset += 33;

    System.arraycopy(sm, 0, result, offset, 32);
    offset += 32;
    System.arraycopy(sr, 0, result, offset, 32);
    offset += 32;
    System.arraycopy(srho, 0, result, offset, 32);

    return result;
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
