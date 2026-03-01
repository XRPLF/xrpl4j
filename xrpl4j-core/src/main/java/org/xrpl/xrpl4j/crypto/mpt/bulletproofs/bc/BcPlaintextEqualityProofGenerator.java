package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.PlaintextEqualityProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTClawbackContext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.tmp.EqualityPlaintextProof;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Java implementation of the Equality Plaintext Proof generator.
 *
 * <p>This implements the Sigma protocol (Chaum-Pedersen style) for proving that an ElGamal
 * ciphertext encrypts a specific known plaintext under a public key.</p>
 */
public class BcPlaintextEqualityProofGenerator implements PlaintextEqualityProofGenerator<PrivateKey> {

  @Override
  public EqualityPlaintextProof generateProof(
    ElGamalCiphertext ciphertext,
    PublicKey publicKey,
    UnsignedLong amount,
    PrivateKey privateKey,
    ConfidentialMPTClawbackContext context
  ) {
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(privateKey, "privateKey must not be null");
    Objects.requireNonNull(context, "context must not be null");

    // Rippled calls secp256k1_equality_plaintext_prove with: (&pk, &c2, &c1, ...)
    // The C function signature is: prove(ctx, proof, c1, c2, pk_recipient, ...)
    // So rippled maps: c1 <- issuer's pk, c2 <- balance.c2, pk_recipient <- balance.c1
    // We do this swapping internally so callers can pass the actual balance ciphertext and issuer's pk
    ECPoint c1 = Secp256k1Operations.toEcPoint(publicKey);    // issuer's public key becomes c1
    ECPoint c2 = ciphertext.c2();          // balance.c2 stays as c2
    ECPoint pk = ciphertext.c1();          // balance.c1 becomes pk_recipient

    // 1. Generate random nonce t internally (matching C implementation pattern)
    BlindingFactor nonceT = BlindingFactor.generate();
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
    byte[] e = ChallengeUtils.plaintextEqualityProofChallenge(c1, c2, pk, mG, T1, T2, context.toBytes());
    BigInteger eInt = new BigInteger(1, e);

    // 5. Compute s = t + e * r (mod n)
    BigInteger rInt = new BigInteger(1, privateKey.naturalBytes().toByteArray());
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
}
