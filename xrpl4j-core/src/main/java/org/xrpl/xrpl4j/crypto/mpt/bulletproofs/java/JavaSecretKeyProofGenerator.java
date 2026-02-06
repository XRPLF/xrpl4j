package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.mpt.RandomnessUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SecretKeyProofGenerator;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * Java implementation of {@link SecretKeyProofGenerator} for Schnorr Proof of Knowledge.
 *
 * <p>This implementation generates proofs compatible with rippled's secp256k1_mpt_pok_sk_prove function.</p>
 */
public class JavaSecretKeyProofGenerator implements SecretKeyProofGenerator {

  private static final String DOMAIN_SEPARATOR = "MPT_POK_SK_REGISTER";

  /**
   * Transaction type for ConfidentialMPTConvert (from definitions.json).
   */
  private static final int TT_CONFIDENTIAL_MPT_CONVERT = 85;

  private final Secp256k1Operations secp256k1;
  private final SecureRandom secureRandom;
  private final AddressCodec addressCodec;

  /**
   * Constructs a new JavaSecretKeyProofGenerator.
   *
   * @param secp256k1    The secp256k1 operations utility.
   * @param secureRandom A secure random number generator.
   */
  public JavaSecretKeyProofGenerator(Secp256k1Operations secp256k1, SecureRandom secureRandom) {
    this.secp256k1 = Objects.requireNonNull(secp256k1, "secp256k1 must not be null");
    this.secureRandom = Objects.requireNonNull(secureRandom, "secureRandom must not be null");
    this.addressCodec = new AddressCodec();
  }

  /**
   * Constructs a new JavaSecretKeyProofGenerator with a default SecureRandom.
   *
   * @param secp256k1 The secp256k1 operations utility.
   */
  public JavaSecretKeyProofGenerator(Secp256k1Operations secp256k1) {
    this(secp256k1, new SecureRandom());
  }

  @Override
  public byte[] generateProof(byte[] privateKey, byte[] contextId, byte[] nonce) {
    Objects.requireNonNull(privateKey, "privateKey must not be null");

    if (privateKey.length != 32) {
      throw new IllegalArgumentException("privateKey must be 32 bytes");
    }
    if (contextId != null && contextId.length != 32) {
      throw new IllegalArgumentException("contextId must be 32 bytes when provided");
    }
    if (nonce != null && nonce.length != 32) {
      throw new IllegalArgumentException("nonce must be 32 bytes when provided");
    }

    // Verify private key is valid
    BigInteger skInt = new BigInteger(1, privateKey);
    if (!secp256k1.isValidPrivateKey(skInt)) {
      throw new IllegalArgumentException("privateKey is not a valid scalar");
    }

    // Derive public key from private key: P = sk * G
    ECPoint publicKey = secp256k1.multiplyG(skInt);

    // 1. Use provided nonce or generate random k, then compute T = k * G
    byte[] k = (nonce != null) ? nonce : RandomnessUtils.generateRandomScalar(secureRandom, secp256k1);
    BigInteger kInt = new BigInteger(1, k);

    // Verify nonce is valid scalar
    if (!secp256k1.isValidPrivateKey(kInt)) {
      throw new IllegalArgumentException("nonce is not a valid scalar");
    }

    ECPoint T = secp256k1.multiplyG(kInt);

    // 2. Compute challenge e = reduce(SHA256(domainSeparator || PublicKey || T || contextId)) mod n
    // buildChallenge already returns the reduced value (mod curve order)
    byte[] eBytes = buildChallenge(publicKey, T, contextId);
    BigInteger eInt = new BigInteger(1, eBytes);

    // 3. Compute response s = k + e * sk (mod n)
    BigInteger term = eInt.multiply(skInt).mod(secp256k1.getCurveOrder());
    BigInteger sInt = kInt.add(term).mod(secp256k1.getCurveOrder());
    byte[] s = secp256k1.toBytes32(sInt);

    // 4. Serialize proof: T (33 bytes) || s (32 bytes)
    byte[] proof = new byte[PROOF_LENGTH];
    byte[] tCompressed = secp256k1.serializeCompressed(T);
    System.arraycopy(tCompressed, 0, proof, 0, 33);
    System.arraycopy(s, 0, proof, 33, 32);

    return proof;
  }

  @Override
  public boolean verifyProof(byte[] proof, ECPoint publicKey, byte[] contextId) {
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
      return false;
    }

    BigInteger sInt = new BigInteger(1, sBytes);
    if (!secp256k1.isValidPrivateKey(sInt)) {
      return false;
    }

    // 2. Recompute challenge e (buildChallenge already returns reduced value)
    byte[] eBytes = buildChallenge(publicKey, T, contextId);
    BigInteger eInt = new BigInteger(1, eBytes);

    // 3. Verify equation: s * G == T + e * P
    ECPoint lhs = secp256k1.multiplyG(sInt);
    ECPoint ePk = secp256k1.multiply(publicKey, eInt);
    ECPoint rhs = secp256k1.add(T, ePk);

    return secp256k1.pointsEqual(lhs, rhs);
  }

  @Override
  public byte[] generateConvertContext(Address account, UnsignedInteger sequence,
    MpTokenIssuanceId issuanceId, UnsignedLong amount) {
    Objects.requireNonNull(account, "account must not be null");
    Objects.requireNonNull(sequence, "sequence must not be null");
    Objects.requireNonNull(issuanceId, "issuanceId must not be null");
    Objects.requireNonNull(amount, "amount must not be null");

    // Serialize fields matching rippled's addCommonZKPFields + add64(amount)
    // Total: 2 (txType) + 20 (account) + 4 (sequence) + 24 (issuanceId) + 8 (amount) = 58 bytes
    ByteBuffer buffer = ByteBuffer.allocate(58);
    buffer.order(ByteOrder.BIG_ENDIAN);

    System.out.println("=== getConvertContextHash Debug ===");

    // 1. add16(txType) - 2 bytes big-endian
    buffer.putShort((short) TT_CONFIDENTIAL_MPT_CONVERT);
    byte[] txTypeBytes = new byte[2];
    buffer.position(0);
    buffer.get(txTypeBytes);
    System.out.println("txType (2 bytes): " + BaseEncoding.base16().lowerCase().encode(txTypeBytes));

    // 2. addBitString(account) - 20 bytes raw
    buffer.position(2);
    UnsignedByteArray accountBytes = addressCodec.decodeAccountId(account);
    buffer.put(accountBytes.toByteArray());
    System.out.println("account (20 bytes): " + BaseEncoding.base16().lowerCase().encode(accountBytes.toByteArray()));

    // 3. add32(sequence) - 4 bytes big-endian
    buffer.position(22);
    buffer.putInt(sequence.intValue());
    byte[] seqBytes = new byte[4];
    buffer.position(22);
    buffer.get(seqBytes);
    System.out.println("sequence (4 bytes): " + BaseEncoding.base16().lowerCase().encode(seqBytes));

    // 4. addBitString(issuanceID) - 24 bytes raw
    buffer.position(26);
    byte[] issuanceIdBytes = BaseEncoding.base16().decode(issuanceId.value().toUpperCase());
    buffer.put(issuanceIdBytes);
    System.out.println("issuanceID (24 bytes): " + BaseEncoding.base16().lowerCase().encode(issuanceIdBytes));

    // 5. add64(amount) - 8 bytes big-endian
    buffer.position(50);
    buffer.putLong(amount.longValue());
    byte[] amountBytes = new byte[8];
    buffer.position(50);
    buffer.get(amountBytes);
    System.out.println("amount (8 bytes): " + BaseEncoding.base16().lowerCase().encode(amountBytes));

    // Print full serialized data
    byte[] fullData = buffer.array();
    System.out.println("Full serialized (58 bytes): " + BaseEncoding.base16().lowerCase().encode(fullData));

    // Compute SHA512Half (first 32 bytes of SHA512)
    byte[] hash = sha512Half(fullData);
    System.out.println("SHA512Half result: " + BaseEncoding.base16().upperCase().encode(hash));
    System.out.println("===================================");

    return hash;
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
    byte[] pkBytes = secp256k1.serializeCompressed(publicKey);
    byte[] tBytes = secp256k1.serializeCompressed(T);

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
    BigInteger reduced = hashInt.mod(secp256k1.getCurveOrder());
    byte[] challenge = secp256k1.toBytes32(reduced);

    // Debug output for comparison with C implementation
    System.out.println("=== buildChallenge Debug ===");
    System.out.println("domainSeparator (" + domainBytes.length + " bytes): " + DOMAIN_SEPARATOR);
    System.out.println("domainSeparator (hex): " + BaseEncoding.base16().lowerCase().encode(domainBytes));
    System.out.println("publicKey (33 bytes): " + BaseEncoding.base16().lowerCase().encode(pkBytes));
    System.out.println("T (33 bytes): " + BaseEncoding.base16().lowerCase().encode(tBytes));
    if (contextId != null) {
      System.out.println("contextId (32 bytes): " + BaseEncoding.base16().lowerCase().encode(contextId));
    } else {
      System.out.println("contextId: null");
    }
    System.out.println("hashInput (" + hashInput.length + " bytes): " + BaseEncoding.base16().lowerCase().encode(hashInput));
    System.out.println("SHA256 (before reduce): " + BaseEncoding.base16().upperCase().encode(sha256Hash));
    System.out.println("challenge (after reduce mod n): " + BaseEncoding.base16().upperCase().encode(challenge));
    System.out.println("============================");

    return challenge;
  }

  /**
   * Computes SHA512Half - the first 32 bytes of SHA512.
   *
   * @param data The data to hash.
   *
   * @return The first 32 bytes of the SHA512 hash.
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

