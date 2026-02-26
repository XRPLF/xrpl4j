package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc;

import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SamePlaintextMultiProof;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SamePlaintextProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SamePlaintextParticipant;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTSendContext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Java implementation of the Same Plaintext Multi Proof generator.
 *
 * <p>This implements the Sigma protocol for proving that N ElGamal ciphertexts
 * all encrypt the same plaintext amount.</p>
 */
public class BcSamePlaintextProofGenerator implements SamePlaintextProofGenerator {

  /**
   * Constructs a new generator.
   */
  public BcSamePlaintextProofGenerator() {
  }

  @Override
  public SamePlaintextMultiProof generateProof(
    final UnsignedLong amount,
    final List<SamePlaintextParticipant> participants,
    final ConfidentialMPTSendContext context
  ) {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(participants, "participants must not be null");
    Objects.requireNonNull(context, "context must not be null");
    Preconditions.checkArgument(participants.size() >= 2,
      "participants list must contain at least 2 elements, but had %s", participants.size());

    int n = participants.size();

    // Extract ciphertexts, public keys, and blinding factors
    List<ElGamalCiphertext> ciphertexts = new ArrayList<>(n);
    List<ECPoint> publicKeys = new ArrayList<>(n);
    List<byte[]> blindingFactors = new ArrayList<>(n);

    for (SamePlaintextParticipant p : participants) {
      ciphertexts.add(p.ciphertext());
      publicKeys.add(Secp256k1Operations.toEcPoint(p.publicKey()));
      blindingFactors.add(p.blindingFactor().toBytes());
    }

    // Extract R and S from ciphertexts
    List<ECPoint> R = new ArrayList<>(n);
    List<ECPoint> S = new ArrayList<>(n);
    for (ElGamalCiphertext ct : ciphertexts) {
      R.add(ct.c1());
      S.add(ct.c2());
    }

    // 1. Generate Randomness & Commitments (matching C implementation)

    // Generate km and compute Tm = km * G
    BlindingFactor nonceKm = BlindingFactor.generate();
    BigInteger kmInt = new BigInteger(1, nonceKm.toBytes());
    ECPoint Tm = Secp256k1Operations.multiplyG(kmInt);

    // Generate nonces kr_i for each participant
    List<byte[]> noncesKr = new ArrayList<>(n);
    List<ECPoint> TrG = new ArrayList<>(n);
    List<ECPoint> TrP = new ArrayList<>(n);

    for (int i = 0; i < n; i++) {
      // Generate random nonce kr_i
      BlindingFactor nonceKri = BlindingFactor.generate();
      byte[] kri = nonceKri.toBytes();
      noncesKr.add(kri);

      BigInteger kriInt = new BigInteger(1, kri);

      // TrG[i] = kri * G
      TrG.add(Secp256k1Operations.multiplyG(kriInt));

      // TrP[i] = kri * Pk[i]
      TrP.add(Secp256k1Operations.multiply(publicKeys.get(i), kriInt));
    }

    // 2. Compute Challenge
    byte[] e = ChallengeUtils.samePlaintextProofChallenge(n, R, S, publicKeys, Tm, TrG, TrP, context.toBytes());
    BigInteger eInt = new BigInteger(1, e);

    // 3. Compute Responses
    // s_m = k_m + e * m (mod n)
    byte[] mScalar = Secp256k1Operations.unsignedLongToScalar(amount);
    BigInteger mInt = new BigInteger(1, mScalar);
    BigInteger smInt = kmInt.add(eInt.multiply(mInt)).mod(Secp256k1Operations.getCurveOrder());
    byte[] sm = Secp256k1Operations.toBytes32(smInt);

    // s_ri = k_ri + e * r_i (mod n)
    List<byte[]> sr = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      BigInteger kriInt = new BigInteger(1, noncesKr.get(i));
      BigInteger riInt = new BigInteger(1, blindingFactors.get(i));
      BigInteger sriInt = kriInt.add(eInt.multiply(riInt)).mod(Secp256k1Operations.getCurveOrder());
      sr.add(Secp256k1Operations.toBytes32(sriInt));
    }

    // 4. Serialize proof: Tm || TrG[0..N-1] || TrP[0..N-1] || sm || sr[0..N-1]
    byte[] proofBytes = serializeProof(n, Tm, TrG, TrP, sm, sr);
    return SamePlaintextMultiProof.fromBytes(proofBytes, n);
  }

  /**
   * Serializes the proof components to bytes.
   */
  private byte[] serializeProof(int n, ECPoint Tm, List<ECPoint> TrG, List<ECPoint> TrP, byte[] sm, List<byte[]> sr) {
    int size = SamePlaintextProofGenerator.proofSize(n);
    byte[] result = new byte[size];
    int offset = 0;

    // Tm (33 bytes)
    byte[] tmBytes = Secp256k1Operations.serializeCompressed(Tm);
    System.arraycopy(tmBytes, 0, result, offset, 33);
    offset += 33;

    // TrG[0..N-1] (N * 33 bytes)
    for (ECPoint trg : TrG) {
      byte[] trgBytes = Secp256k1Operations.serializeCompressed(trg);
      System.arraycopy(trgBytes, 0, result, offset, 33);
      offset += 33;
    }

    // TrP[0..N-1] (N * 33 bytes)
    for (ECPoint trp : TrP) {
      byte[] trpBytes = Secp256k1Operations.serializeCompressed(trp);
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

}

