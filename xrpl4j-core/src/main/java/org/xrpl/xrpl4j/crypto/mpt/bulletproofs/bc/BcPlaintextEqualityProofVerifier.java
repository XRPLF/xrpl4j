package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc;

import com.google.common.hash.Hashing;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.PlaintextEqualityProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.PlaintextEqualityProofVerifier;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTClawbackContext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
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
public class BcPlaintextEqualityProofVerifier implements PlaintextEqualityProofVerifier {

  @Override
  public boolean verifyProof(
    EqualityPlaintextProof proof,
    ElGamalCiphertext ciphertext,
    PublicKey publicKey,
    UnsignedLong amount,
    ConfidentialMPTClawbackContext context
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(context, "context must not be null");

    // Same swapping as generateProof: c1 <- issuer's pk, c2 <- balance.c2, pk <- balance.c1
    ECPoint c1 = Secp256k1Operations.toEcPoint(publicKey);    // issuer's public key becomes c1
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
    byte[] e = ChallengeUtils.plaintextEqualityProofChallenge(c1, c2, pk, mG, T1, T2, context.toBytes());
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
}
