package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.bc;

import com.google.common.base.Preconditions;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.ChallengeUtils;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SamePlaintextProofVerifier;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTSendContext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.tmp.SamePlaintextMultiProof;
import org.xrpl.xrpl4j.crypto.mpt.tmp.SamePlaintextParticipant;

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
public class BcSamePlaintextProofVerifier implements SamePlaintextProofVerifier {
  @Override
  public boolean verify(
    final SamePlaintextMultiProof proof,
    final List<SamePlaintextParticipant> participants,
    final ConfidentialMPTSendContext context
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(participants, "participants must not be null");
    Objects.requireNonNull(context, "context must not be null");
    Preconditions.checkArgument(participants.size() >= 2,
      "participants list must contain at least 2 elements, but had %s", participants.size());

    int n = participants.size();

    // Validate proof participant count matches
    if (proof.participantCount() != n) {
      return false;
    }

    // Extract ciphertexts and public keys
    List<ElGamalCiphertext> ciphertexts = new ArrayList<>(n);
    List<ECPoint> publicKeys = new ArrayList<>(n);
    for (SamePlaintextParticipant p : participants) {
      ciphertexts.add(p.ciphertext());
      publicKeys.add(Secp256k1Operations.toEcPoint(p.publicKey()));
    }

    byte[] proofBytes = proof.toBytes();

    // Deserialize proof
    int offset = 0;

    // Tm (33 bytes)
    byte[] tmBytes = new byte[33];
    System.arraycopy(proofBytes, offset, tmBytes, 0, 33);
    ECPoint Tm = Secp256k1Operations.deserialize(tmBytes);
    offset += 33;

    // TrG[0..N-1] (N * 33 bytes)
    List<ECPoint> TrG = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      byte[] trgBytes = new byte[33];
      System.arraycopy(proofBytes, offset, trgBytes, 0, 33);
      ECPoint trg = Secp256k1Operations.deserialize(trgBytes);
      TrG.add(trg);
      offset += 33;
    }

    // TrP[0..N-1] (N * 33 bytes)
    List<ECPoint> TrP = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      byte[] trpBytes = new byte[33];
      System.arraycopy(proofBytes, offset, trpBytes, 0, 33);
      ECPoint trp = Secp256k1Operations.deserialize(trpBytes);
      TrP.add(trp);
      offset += 33;
    }

    // sm (32 bytes)
    byte[] sm = new byte[32];
    System.arraycopy(proofBytes, offset, sm, 0, 32);
    if (!Secp256k1Operations.isValidScalar(sm)) {
      return false;
    }
    offset += 32;

    // sr[0..N-1] (N * 32 bytes)
    List<byte[]> sr = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      byte[] sri = new byte[32];
      System.arraycopy(proofBytes, offset, sri, 0, 32);
      if (!Secp256k1Operations.isValidScalar(sri)) {
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
    byte[] e = ChallengeUtils.samePlaintextProofChallenge(n, R, S, publicKeys, Tm, TrG, TrP, context.toBytes());
    BigInteger eInt = new BigInteger(1, e);

    // Precompute s_m * G
    BigInteger smInt = new BigInteger(1, sm);
    ECPoint SmG = Secp256k1Operations.multiplyG(smInt);

    // Verify equations for each i
    for (int i = 0; i < n; i++) {
      BigInteger sriInt = new BigInteger(1, sr.get(i));

      // Eq 1: s_ri * G == TrG[i] + e * R[i]
      ECPoint lhs1 = Secp256k1Operations.multiplyG(sriInt);
      ECPoint eRi = Secp256k1Operations.multiply(R.get(i), eInt);
      ECPoint rhs1 = Secp256k1Operations.add(TrG.get(i), eRi);

      if (!Secp256k1Operations.pointsEqual(lhs1, rhs1)) {
        return false;
      }

      // Eq 2: s_m * G + s_ri * Pk[i] == Tm + TrP[i] + e * S[i]
      ECPoint sriPki = Secp256k1Operations.multiply(publicKeys.get(i), sriInt);
      ECPoint lhs2 = Secp256k1Operations.add(SmG, sriPki);

      ECPoint eSi = Secp256k1Operations.multiply(S.get(i), eInt);
      ECPoint tmTrp = Secp256k1Operations.add(Tm, TrP.get(i));
      ECPoint rhs2 = Secp256k1Operations.add(tmTrp, eSi);

      if (!Secp256k1Operations.pointsEqual(lhs2, rhs2)) {
        return false;
      }
    }

    return true;
  }
}

