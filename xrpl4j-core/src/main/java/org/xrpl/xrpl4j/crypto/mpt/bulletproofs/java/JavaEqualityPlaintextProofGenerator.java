package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java;

import com.google.common.hash.Hashing;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.EqualityPlaintextProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTClawbackContext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPublicKey;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.EqualityPlaintextProof;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Java implementation of the Equality Plaintext Proof generator.
 *
 * <p>This implements the Sigma protocol (Chaum-Pedersen style) for proving that an ElGamal
 * ciphertext encrypts a specific known plaintext under a public key.</p>
 */
public class JavaEqualityPlaintextProofGenerator implements EqualityPlaintextProofGenerator {

  private static final String DOMAIN_SEPARATOR = "MPT_POK_PLAINTEXT_PROOF";

  /**
   * Constructs a new generator.
   */
  public JavaEqualityPlaintextProofGenerator() {
  }

  @Override
  public EqualityPlaintextProof generateProof(
    ElGamalCiphertext ciphertext,
    ElGamalPublicKey publicKey,
    UnsignedLong amount,
    BlindingFactor randomness,
    BlindingFactor nonceT,
    ConfidentialMPTClawbackContext context
  ) {
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(randomness, "randomness must not be null");
    Objects.requireNonNull(nonceT, "nonceT must not be null");
    Objects.requireNonNull(context, "context must not be null");

    // Rippled calls secp256k1_equality_plaintext_prove with: (&pk, &c2, &c1, ...)
    // The C function signature is: prove(ctx, proof, c1, c2, pk_recipient, ...)
    // So rippled maps: c1 <- issuer's pk, c2 <- balance.c2, pk_recipient <- balance.c1
    // We do this swapping internally so callers can pass the actual balance ciphertext and issuer's pk
    ECPoint c1 = publicKey.asEcPoint();    // issuer's public key becomes c1
    ECPoint c2 = ciphertext.c2();          // balance.c2 stays as c2
    ECPoint pk = ciphertext.c1();          // balance.c1 becomes pk_recipient

    // 1. Use provided nonce t
    BigInteger tInt = new BigInteger(1, nonceT.toBytes());

    // 2. Compute commitments T1 = t * G, T2 = t * Pk (where Pk = balance.c1)
    ECPoint T1 = Secp256k1Operations.multiplyG(tInt);
    ECPoint T2 = Secp256k1Operations.multiply(pk, tInt);

    // 3. Compute mG if amount > 0
    ECPoint mG = null;
    if (amount.longValue() > 0) {
      byte[] mScalar = Secp256k1Operations.unsignedLongToScalar(amount);
      BigInteger mInt = new BigInteger(1, mScalar);
      mG = Secp256k1Operations.multiplyG(mInt);
    }

    // 4. Compute challenge e (with swapped c1/c2)
    byte[] e = computeChallenge(c1, c2, pk, mG, T1, T2, context.toBytes());
    BigInteger eInt = new BigInteger(1, e);

    // 5. Compute s = t + e * r (mod n)
    BigInteger rInt = new BigInteger(1, randomness.toBytes());
    BigInteger sInt = tInt.add(eInt.multiply(rInt)).mod(Secp256k1Operations.getCurveOrder());
    byte[] s = Secp256k1Operations.toBytes32(sInt);

    // 6. Serialize proof: T1 (33) || T2 (33) || s (32) = 98 bytes
    byte[] proof = new byte[PROOF_LENGTH];
    byte[] t1Bytes = Secp256k1Operations.serializeCompressed(T1);
    byte[] t2Bytes = Secp256k1Operations.serializeCompressed(T2);
    System.arraycopy(t1Bytes, 0, proof, 0, 33);
    System.arraycopy(t2Bytes, 0, proof, 33, 33);
    System.arraycopy(s, 0, proof, 66, 32);

    return EqualityPlaintextProof.fromBytes(proof);
  }

  @Override
  public boolean verifyProof(
    EqualityPlaintextProof proof,
    ElGamalCiphertext ciphertext,
    ElGamalPublicKey publicKey,
    UnsignedLong amount,
    ConfidentialMPTClawbackContext context
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(context, "context must not be null");

    // Same swapping as generateProof: c1 <- issuer's pk, c2 <- balance.c2, pk <- balance.c1
    ECPoint c1 = publicKey.asEcPoint();    // issuer's public key becomes c1
    ECPoint c2 = ciphertext.c2();          // balance.c2 stays as c2
    ECPoint pk = ciphertext.c1();          // balance.c1 becomes pk_recipient
    byte[] proofBytes = proof.toBytes();

    // 1. Deserialize proof
    byte[] t1Bytes = new byte[33];
    byte[] t2Bytes = new byte[33];
    byte[] s = new byte[32];
    System.arraycopy(proofBytes, 0, t1Bytes, 0, 33);
    System.arraycopy(proofBytes, 33, t2Bytes, 0, 33);
    System.arraycopy(proofBytes, 66, s, 0, 32);

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
    byte[] e = computeChallenge(c1, c2, pk, mG, T1, T2, context.toBytes());
    BigInteger eInt = new BigInteger(1, e);

    // 4. Verify Eq 1: s * G == T1 + e * C1
    ECPoint lhs1 = Secp256k1Operations.multiplyG(sInt);
    ECPoint eC1 = Secp256k1Operations.multiply(c1, eInt);
    ECPoint rhs1 = Secp256k1Operations.add(T1, eC1);
    if (!Secp256k1Operations.pointsEqual(lhs1, rhs1)) {
      return false;
    }

    // 5. Verify Eq 2: s * Pk == T2 + e * (C2 - mG)
    ECPoint lhs2 = Secp256k1Operations.multiply(pk, sInt);
    ECPoint Y = c2;
    if (mG != null) {
      ECPoint negMG = mG.negate();
      Y = Secp256k1Operations.add(c2, negMG);
    }
    ECPoint eY = Secp256k1Operations.multiply(Y, eInt);
    ECPoint rhs2 = Secp256k1Operations.add(T2, eY);

    return Secp256k1Operations.pointsEqual(lhs2, rhs2);
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
}
