package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.SecureRandomUtils;
import org.xrpl.xrpl4j.crypto.mpt.RandomnessUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.EqualityPlaintextProofGenerator;
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
 * Java implementation of the Equality Plaintext Proof generator.
 *
 * <p>This implements the Sigma protocol (Chaum-Pedersen style) for proving that an ElGamal
 * ciphertext encrypts a specific known plaintext under a public key.</p>
 */
public class JavaEqualityPlaintextProofGenerator implements EqualityPlaintextProofGenerator {

  private static final String DOMAIN_SEPARATOR = "MPT_POK_PLAINTEXT_PROOF";
  private static final int TT_CONFIDENTIAL_MPT_CLAWBACK = 89;

  private final AddressCodec addressCodec;

  /**
   * Constructs a new generator.
   */
  public JavaEqualityPlaintextProofGenerator() {
    this.addressCodec = AddressCodec.getInstance();
  }

  @Override
  public byte[] generateProof(
    ECPoint c1,
    ECPoint c2,
    ECPoint publicKey,
    UnsignedLong amount,
    byte[] randomness,
    byte[] contextId
  ) {
    Objects.requireNonNull(c1, "c1 must not be null");
    Objects.requireNonNull(c2, "c2 must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(randomness, "randomness must not be null");

    if (randomness.length != 32) {
      throw new IllegalArgumentException("randomness must be 32 bytes");
    }
    if (!Secp256k1Operations.isValidScalar(randomness)) {
      throw new IllegalArgumentException("randomness must be a valid scalar");
    }

    // 1. Generate random t
    byte[] t = RandomnessUtils.generateRandomScalar();
    BigInteger tInt = new BigInteger(1, t);

    // 2. Compute commitments T1 = t * G, T2 = t * Pk
    ECPoint T1 = Secp256k1Operations.multiplyG(tInt);
    ECPoint T2 = Secp256k1Operations.multiply(publicKey, tInt);

    // 3. Compute mG if amount > 0
    ECPoint mG = null;
    if (amount.longValue() > 0) {
      byte[] mScalar = Secp256k1Operations.unsignedLongToScalar(amount);
      BigInteger mInt = new BigInteger(1, mScalar);
      mG = Secp256k1Operations.multiplyG(mInt);
    }

    // 4. Compute challenge e
    byte[] e = computeChallenge(c1, c2, publicKey, mG, T1, T2, contextId);
    BigInteger eInt = new BigInteger(1, e);

    // 5. Compute s = t + e * r (mod n)
    BigInteger rInt = new BigInteger(1, randomness);
    BigInteger sInt = tInt.add(eInt.multiply(rInt)).mod(Secp256k1Operations.getCurveOrder());
    byte[] s = Secp256k1Operations.toBytes32(sInt);

    // 6. Serialize proof: T1 (33) || T2 (33) || s (32) = 98 bytes
    byte[] proof = new byte[PROOF_LENGTH];
    byte[] t1Bytes = Secp256k1Operations.serializeCompressed(T1);
    byte[] t2Bytes = Secp256k1Operations.serializeCompressed(T2);
    System.arraycopy(t1Bytes, 0, proof, 0, 33);
    System.arraycopy(t2Bytes, 0, proof, 33, 33);
    System.arraycopy(s, 0, proof, 66, 32);

    return proof;
  }

  @Override
  public boolean verifyProof(
    byte[] proof,
    ECPoint c1,
    ECPoint c2,
    ECPoint publicKey,
    UnsignedLong amount,
    byte[] contextId
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(c1, "c1 must not be null");
    Objects.requireNonNull(c2, "c2 must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(amount, "amount must not be null");

    if (proof.length != PROOF_LENGTH) {
      return false;
    }

    // 1. Deserialize proof
    byte[] t1Bytes = new byte[33];
    byte[] t2Bytes = new byte[33];
    byte[] s = new byte[32];
    System.arraycopy(proof, 0, t1Bytes, 0, 33);
    System.arraycopy(proof, 33, t2Bytes, 0, 33);
    System.arraycopy(proof, 66, s, 0, 32);

    ECPoint T1;
    ECPoint T2;
    try {
      T1 = Secp256k1Operations.deserialize(t1Bytes);
      T2 = Secp256k1Operations.deserialize(t2Bytes);
    } catch (Exception e) {
      return false;
    }

    if (!Secp256k1Operations.isValidScalar(s)) {
      return false;
    }
    BigInteger sInt = new BigInteger(1, s);

    // 2. Compute mG if amount > 0
    ECPoint mG = null;
    if (amount.longValue() > 0) {
      byte[] mScalar = Secp256k1Operations.unsignedLongToScalar(amount);
      BigInteger mInt = new BigInteger(1, mScalar);
      mG = Secp256k1Operations.multiplyG(mInt);
    }

    // 3. Recompute challenge e
    byte[] e = computeChallenge(c1, c2, publicKey, mG, T1, T2, contextId);
    BigInteger eInt = new BigInteger(1, e);

    // 4. Verify Eq 1: s * G == T1 + e * C1
    ECPoint lhs1 = Secp256k1Operations.multiplyG(sInt);
    ECPoint eC1 = Secp256k1Operations.multiply(c1, eInt);
    ECPoint rhs1 = Secp256k1Operations.add(T1, eC1);
    if (!Secp256k1Operations.pointsEqual(lhs1, rhs1)) {
      return false;
    }

    // 5. Verify Eq 2: s * Pk == T2 + e * (C2 - mG)
    ECPoint lhs2 = Secp256k1Operations.multiply(publicKey, sInt);
    ECPoint Y = c2;
    if (mG != null) {
      ECPoint negMG = mG.negate();
      Y = Secp256k1Operations.add(c2, negMG);
    }
    ECPoint eY = Secp256k1Operations.multiply(Y, eInt);
    ECPoint rhs2 = Secp256k1Operations.add(T2, eY);

    return Secp256k1Operations.pointsEqual(lhs2, rhs2);
  }

  @Override
  public byte[] generateClawbackContext(
    Address account,
    UnsignedInteger sequence,
    MpTokenIssuanceId issuanceId,
    UnsignedLong amount,
    Address holder
  ) {
    Objects.requireNonNull(account, "account must not be null");
    Objects.requireNonNull(sequence, "sequence must not be null");
    Objects.requireNonNull(issuanceId, "issuanceId must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(holder, "holder must not be null");

    // Total: 2 (txType) + 20 (account) + 4 (sequence) + 24 (issuanceId) + 8 (amount) + 20 (holder) = 78 bytes
    ByteBuffer buffer = ByteBuffer.allocate(78);
    buffer.order(ByteOrder.BIG_ENDIAN);

    // 1. add16(txType) - 2 bytes big-endian
    buffer.putShort((short) TT_CONFIDENTIAL_MPT_CLAWBACK);

    // 2. addBitString(account) - 20 bytes raw
    UnsignedByteArray accountBytes = addressCodec.decodeAccountId(account);
    buffer.put(accountBytes.toByteArray());

    // 3. add32(sequence) - 4 bytes big-endian
    buffer.putInt(sequence.intValue());

    // 4. addBitString(issuanceID) - 24 bytes raw
    byte[] issuanceIdBytes = BaseEncoding.base16().decode(issuanceId.value().toUpperCase());
    buffer.put(issuanceIdBytes);

    // 5. add64(amount) - 8 bytes big-endian
    buffer.putLong(amount.longValue());

    // 6. addBitString(holder) - 20 bytes raw
    UnsignedByteArray holderBytes = addressCodec.decodeAccountId(holder);
    buffer.put(holderBytes.toByteArray());

    // Compute SHA512Half (first 32 bytes of SHA512)
    return sha512Half(buffer.array());
  }

  /**
   * Computes the challenge hash for the equality plaintext proof.
   */
  private byte[] computeChallenge(
    ECPoint c1,
    ECPoint c2,
    ECPoint publicKey,
    ECPoint mG,
    ECPoint T1,
    ECPoint T2,
    byte[] contextId
  ) {
    // Calculate total size
    int size = DOMAIN_SEPARATOR.length() + 33 + 33 + 33 + 33 + 33;
    if (mG != null) {
      size += 33;
    }
    if (contextId != null) {
      size += 32;
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

    // Context ID
    if (contextId != null) {
      System.arraycopy(contextId, 0, hashInput, offset, 32);
    }

    // SHA256 and reduce mod curve order
    byte[] sha256Hash = Hashing.sha256().hashBytes(hashInput).asBytes();
    BigInteger hashInt = new BigInteger(1, sha256Hash);
    BigInteger reduced = hashInt.mod(Secp256k1Operations.getCurveOrder());
    return Secp256k1Operations.toBytes32(reduced);
  }

  /**
   * Computes SHA512Half - the first 32 bytes of SHA512.
   */
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
