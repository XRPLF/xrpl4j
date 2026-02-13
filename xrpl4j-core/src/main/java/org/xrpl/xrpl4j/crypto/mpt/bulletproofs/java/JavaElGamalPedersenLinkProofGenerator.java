package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.ElGamalPedersenLinkProofGenerator;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Java implementation of the ElGamal-Pedersen Link Proof generator.
 *
 * <p>This implements the Sigma protocol for proving that an ElGamal ciphertext
 * and a Pedersen commitment encode the same plaintext amount.</p>
 */
public class JavaElGamalPedersenLinkProofGenerator implements ElGamalPedersenLinkProofGenerator {

  private static final String DOMAIN_SEPARATOR = "MPT_ELGAMAL_PEDERSEN_LINK";
  private static final String NUMS_DOMAIN_SEPARATOR = "MPT_BULLETPROOF_V1_NUMS";
  private static final String CURVE_LABEL = "secp256k1";
  private static final int TT_CONFIDENTIAL_MPT_SEND = 88;

  private final Secp256k1Operations secp256k1;
  private final AddressCodec addressCodec;
  private ECPoint cachedH;

  /**
   * Constructs a new generator with the given secp256k1 operations.
   *
   * @param secp256k1 The secp256k1 operations instance.
   */
  public JavaElGamalPedersenLinkProofGenerator(Secp256k1Operations secp256k1) {
    this.secp256k1 = Objects.requireNonNull(secp256k1, "secp256k1 must not be null");
    this.addressCodec = AddressCodec.getInstance();
  }

  @Override
  public byte[] generateProof(
    ECPoint c1,
    ECPoint c2,
    ECPoint publicKey,
    ECPoint commitment,
    UnsignedLong amount,
    byte[] r,
    byte[] rho,
    byte[] contextHash,
    byte[] nonceKm,
    byte[] nonceKr,
    byte[] nonceKrho
  ) {
    Objects.requireNonNull(c1, "c1 must not be null");
    Objects.requireNonNull(c2, "c2 must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(commitment, "commitment must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(r, "r must not be null");
    Objects.requireNonNull(rho, "rho must not be null");
    Objects.requireNonNull(nonceKm, "nonceKm must not be null");
    Objects.requireNonNull(nonceKr, "nonceKr must not be null");
    Objects.requireNonNull(nonceKrho, "nonceKrho must not be null");

    validateScalar(r, "r");
    validateScalar(rho, "rho");
    validateScalar(nonceKm, "nonceKm");
    validateScalar(nonceKr, "nonceKr");
    validateScalar(nonceKrho, "nonceKrho");

    BigInteger kmInt = new BigInteger(1, nonceKm);
    BigInteger krInt = new BigInteger(1, nonceKr);
    BigInteger krhoInt = new BigInteger(1, nonceKrho);

    // 1. Compute Commitments
    // T1 = kr * G
    ECPoint T1 = secp256k1.multiplyG(krInt);

    // T2 = km * G + kr * Pk
    ECPoint kmG = secp256k1.multiplyG(kmInt);
    ECPoint krPk = secp256k1.multiply(publicKey, krInt);
    ECPoint T2 = secp256k1.add(kmG, krPk);

    // T3 = km * G + krho * H
    ECPoint H = getHGenerator();
    ECPoint krhoH = secp256k1.multiply(H, krhoInt);
    ECPoint T3 = secp256k1.add(kmG, krhoH);

    // 2. Compute Challenge
    byte[] e = computeChallenge(c1, c2, publicKey, commitment, T1, T2, T3, contextHash);
    BigInteger eInt = new BigInteger(1, e);

    // 3. Compute Responses
    byte[] mScalar = secp256k1.unsignedLongToScalar(amount);
    BigInteger mInt = new BigInteger(1, mScalar);
    BigInteger rInt = new BigInteger(1, r);
    BigInteger rhoInt = new BigInteger(1, rho);

    // sm = km + e * m (mod n)
    BigInteger smInt = kmInt.add(eInt.multiply(mInt)).mod(secp256k1.getCurveOrder());
    byte[] sm = secp256k1.toBytes32(smInt);

    // sr = kr + e * r (mod n)
    BigInteger srInt = krInt.add(eInt.multiply(rInt)).mod(secp256k1.getCurveOrder());
    byte[] sr = secp256k1.toBytes32(srInt);

    // srho = krho + e * rho (mod n)
    BigInteger srhoInt = krhoInt.add(eInt.multiply(rhoInt)).mod(secp256k1.getCurveOrder());
    byte[] srho = secp256k1.toBytes32(srhoInt);

    // 4. Serialize Proof (195 bytes)
    return serializeProof(T1, T2, T3, sm, sr, srho);
  }

  private void validateScalar(byte[] scalar, String name) {
    if (scalar.length != 32) {
      throw new IllegalArgumentException(name + " must be 32 bytes");
    }
    if (!secp256k1.isValidScalar(scalar)) {
      throw new IllegalArgumentException(name + " must be a valid scalar");
    }
  }

  private byte[] serializeProof(ECPoint T1, ECPoint T2, ECPoint T3, byte[] sm, byte[] sr, byte[] srho) {
    byte[] result = new byte[PROOF_SIZE];
    int offset = 0;

    byte[] t1Bytes = secp256k1.serializeCompressed(T1);
    System.arraycopy(t1Bytes, 0, result, offset, 33);
    offset += 33;

    byte[] t2Bytes = secp256k1.serializeCompressed(T2);
    System.arraycopy(t2Bytes, 0, result, offset, 33);
    offset += 33;

    byte[] t3Bytes = secp256k1.serializeCompressed(T3);
    System.arraycopy(t3Bytes, 0, result, offset, 33);
    offset += 33;

    System.arraycopy(sm, 0, result, offset, 32);
    offset += 32;
    System.arraycopy(sr, 0, result, offset, 32);
    offset += 32;
    System.arraycopy(srho, 0, result, offset, 32);

    return result;
  }

  @Override
  public boolean verify(
    byte[] proof,
    ECPoint c1,
    ECPoint c2,
    ECPoint publicKey,
    ECPoint commitment,
    byte[] contextHash
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(c1, "c1 must not be null");
    Objects.requireNonNull(c2, "c2 must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(commitment, "commitment must not be null");

    if (proof.length != PROOF_SIZE) {
      return false;
    }

    // 1. Deserialize proof
    int offset = 0;

    byte[] t1Bytes = new byte[33];
    System.arraycopy(proof, offset, t1Bytes, 0, 33);
    ECPoint T1 = secp256k1.deserialize(t1Bytes);
    offset += 33;

    byte[] t2Bytes = new byte[33];
    System.arraycopy(proof, offset, t2Bytes, 0, 33);
    ECPoint T2 = secp256k1.deserialize(t2Bytes);
    offset += 33;

    byte[] t3Bytes = new byte[33];
    System.arraycopy(proof, offset, t3Bytes, 0, 33);
    ECPoint T3 = secp256k1.deserialize(t3Bytes);
    offset += 33;

    byte[] sm = new byte[32];
    System.arraycopy(proof, offset, sm, 0, 32);
    if (!secp256k1.isValidScalar(sm)) return false;
    offset += 32;

    byte[] sr = new byte[32];
    System.arraycopy(proof, offset, sr, 0, 32);
    if (!secp256k1.isValidScalar(sr)) return false;
    offset += 32;

    byte[] srho = new byte[32];
    System.arraycopy(proof, offset, srho, 0, 32);
    if (!secp256k1.isValidScalar(srho)) return false;

    BigInteger smInt = new BigInteger(1, sm);
    BigInteger srInt = new BigInteger(1, sr);
    BigInteger srhoInt = new BigInteger(1, srho);

    // 2. Recompute challenge
    byte[] e = computeChallenge(c1, c2, publicKey, commitment, T1, T2, T3, contextHash);
    BigInteger eInt = new BigInteger(1, e);

    // 3. Verification equations

    // Eq 1: sr * G == T1 + e * C1
    ECPoint lhs1 = secp256k1.multiplyG(srInt);
    ECPoint eC1 = secp256k1.multiply(c1, eInt);
    ECPoint rhs1 = secp256k1.add(T1, eC1);
    if (!secp256k1.pointsEqual(lhs1, rhs1)) return false;

    // Eq 2: sm * G + sr * Pk == T2 + e * C2
    ECPoint smG = secp256k1.multiplyG(smInt);
    ECPoint srPk = secp256k1.multiply(publicKey, srInt);
    ECPoint lhs2 = secp256k1.add(smG, srPk);
    ECPoint eC2 = secp256k1.multiply(c2, eInt);
    ECPoint rhs2 = secp256k1.add(T2, eC2);
    if (!secp256k1.pointsEqual(lhs2, rhs2)) return false;

    // Eq 3: sm * G + srho * H == T3 + e * PCm
    ECPoint H = getHGenerator();
    ECPoint srhoH = secp256k1.multiply(H, srhoInt);
    ECPoint lhs3 = secp256k1.add(smG, srhoH);
    ECPoint ePcm = secp256k1.multiply(commitment, eInt);
    ECPoint rhs3 = secp256k1.add(T3, ePcm);
    if (!secp256k1.pointsEqual(lhs3, rhs3)) return false;

    return true;
  }

  private byte[] computeChallenge(
    ECPoint c1, ECPoint c2, ECPoint pk, ECPoint pcm,
    ECPoint T1, ECPoint T2, ECPoint T3,
    byte[] contextId
  ) {
    byte[] domainBytes = DOMAIN_SEPARATOR.getBytes(StandardCharsets.UTF_8);
    int contextLen = (contextId != null) ? 32 : 0;
    int bufferSize = domainBytes.length + (7 * 33) + contextLen;

    byte[] buffer = new byte[bufferSize];
    int offset = 0;

    System.arraycopy(domainBytes, 0, buffer, offset, domainBytes.length);
    offset += domainBytes.length;

    offset = appendPoint(buffer, offset, c1);
    offset = appendPoint(buffer, offset, c2);
    offset = appendPoint(buffer, offset, pk);
    offset = appendPoint(buffer, offset, pcm);
    offset = appendPoint(buffer, offset, T1);
    offset = appendPoint(buffer, offset, T2);
    offset = appendPoint(buffer, offset, T3);

    if (contextId != null) {
      System.arraycopy(contextId, 0, buffer, offset, 32);
    }

    byte[] hash = Hashing.sha256().hashBytes(buffer).asBytes();
    return reduceToScalar(hash);
  }

  private int appendPoint(byte[] buffer, int offset, ECPoint point) {
    byte[] pointBytes = secp256k1.serializeCompressed(point);
    System.arraycopy(pointBytes, 0, buffer, offset, 33);
    return offset + 33;
  }

  private byte[] reduceToScalar(byte[] hash) {
    BigInteger hashInt = new BigInteger(1, hash);
    BigInteger reduced = hashInt.mod(secp256k1.getCurveOrder());
    return secp256k1.toBytes32(reduced);
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
        ECPoint point = secp256k1.deserialize(compressed);
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

  @Override
  public byte[] generateSendContext(
    Address account,
    UnsignedInteger sequence,
    MpTokenIssuanceId issuanceId,
    Address destination,
    UnsignedInteger version
  ) {
    Objects.requireNonNull(account, "account must not be null");
    Objects.requireNonNull(sequence, "sequence must not be null");
    Objects.requireNonNull(issuanceId, "issuanceId must not be null");
    Objects.requireNonNull(destination, "destination must not be null");
    Objects.requireNonNull(version, "version must not be null");

    ByteBuffer buffer = ByteBuffer.allocate(74);
    buffer.order(ByteOrder.BIG_ENDIAN);

    buffer.putShort((short) TT_CONFIDENTIAL_MPT_SEND);

    UnsignedByteArray accountBytes = addressCodec.decodeAccountId(account);
    buffer.put(accountBytes.toByteArray());

    buffer.putInt(sequence.intValue());

    byte[] issuanceIdBytes = BaseEncoding.base16().decode(issuanceId.value().toUpperCase());
    buffer.put(issuanceIdBytes);

    UnsignedByteArray destinationBytes = addressCodec.decodeAccountId(destination);
    buffer.put(destinationBytes.toByteArray());

    buffer.putInt(version.intValue());

    return sha512Half(buffer.array());
  }

  private byte[] sha512Half(byte[] data) {
    try {
      MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
      byte[] fullHash = sha512.digest(data);
      byte[] halfHash = new byte[32];
      System.arraycopy(fullHash, 0, halfHash, 0, 32);
      return halfHash;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-512 algorithm not available", e);
    }
  }
}
