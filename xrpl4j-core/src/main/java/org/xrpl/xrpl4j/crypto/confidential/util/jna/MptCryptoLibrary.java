package org.xrpl.xrpl4j.crypto.confidential.util.jna;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import com.google.common.annotations.VisibleForTesting;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.function.Supplier;

/**
 * JNA binding interface for the mpt-crypto native library.
 *
 * <p>This interface maps to the C functions declared in {@code mpt_utility.h} from the mpt-crypto library.
 * The native library provides ElGamal encryption, decryption, and zero-knowledge proof generation
 * for Confidential MPT transactions on the XRP Ledger.</p>
 *
 * <p>Success is signalled differently across these methods, so check each one's documented return value: most return
 * 0 on success and a negative value on failure, but {@link #secp256k1_elgamal_add} and
 * {@link #secp256k1_elgamal_subtract} return 1 on success, and the {@code ec_pair} methods return a boolean.</p>
 *
 * <p>Access the singleton instance via {@link #getInstance()}.</p>
 */
public interface MptCryptoLibrary extends Library {

  /**
   * Returns the singleton instance of the native library.
   *
   * @return The {@link MptCryptoLibrary} singleton.
   */
  static MptCryptoLibrary getInstance() {
    return Holder.INSTANCE;
  }

  // =========================================================================
  // Encryption / Decryption
  // =========================================================================

  /**
   * Encrypts an unsigned 64-bit amount under an ElGamal public key.
   *
   * @param amount         The integer value to encrypt.
   * @param publicKey      The 33-byte public key.
   * @param blindingFactor The 32-byte random blinding factor (scalar r).
   * @param outCiphertext  A 66-byte buffer to receive the ciphertext (C1 || C2).
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_encrypt_amount(long amount, byte[] publicKey, byte[] blindingFactor, byte[] outCiphertext);

  /**
   * Decrypts an MPT amount by brute-forcing the discrete log over the supplied range.
   *
   * <p>Cost scales linearly with {@code rangeHigh - rangeLow}; the native docs quote roughly 3 seconds for
   * [0, 1,000,000] on Apple Silicon, so ranges must be bounded to the caller's plausible balance.</p>
   *
   * @param ciphertext A 66-byte buffer containing the two points (C1, C2).
   * @param privateKey The 32-byte private key.
   * @param outAmount  A single-element array to receive the decrypted amount.
   * @param rangeLow   Lower bound of the search range, inclusive.
   * @param rangeHigh  Upper bound of the search range, inclusive; must be &gt;= {@code rangeLow} and less than
   *                   2^64 - 1.
   *
   * @return 0 on success, -1 on failure, or -2 if the range is inverted or {@code rangeHigh} is 2^64 - 1.
   */
  int mpt_decrypt_amount(byte[] ciphertext, byte[] privateKey, long[] outAmount, long rangeLow, long rangeHigh);

  /**
   * Generates a 32-byte blinding factor.
   *
   * @param outFactor A 32-byte buffer to receive the blinding factor.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_generate_blinding_factor(byte[] outFactor);

  /**
   * Computes a Pedersen commitment point.
   *
   * @param amount         The 64-bit unsigned value to commit to.
   * @param blindingFactor A 32-byte secret scalar (rho) that hides the amount.
   * @param outCommitment  A 33-byte buffer to receive the commitment.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_pedersen_commitment(long amount, byte[] blindingFactor, byte[] outCommitment);

  // =========================================================================
  // Homomorphic Ciphertext Operations
  // =========================================================================
  //
  // ElGamal is additively homomorphic: two same-key ciphertexts can be combined
  // into an encryption of the sum/difference of their plaintexts without
  // decrypting. The add/subtract routines operate on parsed secp256k1 points
  // (opaque 64-byte `secp256k1_pubkey` buffers), so a 66-byte wire ciphertext
  // (C1 || C2) is bridged to/from two points via mpt_make_ec_pair /
  // mpt_serialize_ec_pair. Unlike the mpt_* primitives above (0 == success),
  // these follow secp256k1's convention: they return 1 (true) on success.

  /**
   * The globally shared secp256k1 context owned by the native library.
   *
   * @return A {@link Pointer} to the shared {@code secp256k1_context}.
   */
  Pointer mpt_secp256k1_context();

  /**
   * Parses a 66-byte wire ciphertext into two internal secp256k1 public keys.
   *
   * @param buffer A 66-byte buffer containing two points.
   * @param out1   Receives the first point (C1), as an opaque 64-byte native point.
   * @param out2   Receives the second point (C2), as an opaque 64-byte native point.
   *
   * @return {@code true} on success, {@code false} if parsing fails.
   */
  boolean mpt_make_ec_pair(byte[] buffer, Pointer out1, Pointer out2);

  /**
   * Serializes two internal secp256k1 public keys back into a 66-byte wire ciphertext.
   *
   * @param in1 The first point (C1), as an opaque 64-byte native point.
   * @param in2 The second point (C2), as an opaque 64-byte native point.
   * @param out A 66-byte buffer to receive the serialized points.
   *
   * @return {@code true} if both points were valid and serialized, {@code false} otherwise.
   */
  boolean mpt_serialize_ec_pair(Pointer in1, Pointer in2, byte[] out);

  /**
   * Homomorphically adds two ElGamal ciphertexts encrypted under the same key.
   *
   * @param ctx     The shared context from {@link #mpt_secp256k1_context()}.
   * @param sumC1   Receives the first point of the sum.
   * @param sumC2   Receives the second point of the sum.
   * @param leftC1  First point of the left operand.
   * @param leftC2  Second point of the left operand.
   * @param rightC1 First point of the right operand.
   * @param rightC2 Second point of the right operand.
   *
   * @return 1 on success, 0 on failure. Note this is inverted relative to most methods here.
   */
  int secp256k1_elgamal_add(
    Pointer ctx, Pointer sumC1, Pointer sumC2,
    Pointer leftC1, Pointer leftC2, Pointer rightC1, Pointer rightC2
  );

  /**
   * Homomorphically subtracts one ElGamal ciphertext from another, both under the same key.
   *
   * @param ctx     The shared context from {@link #mpt_secp256k1_context()}.
   * @param diffC1  Receives the first point of the difference.
   * @param diffC2  Receives the second point of the difference.
   * @param leftC1  First point of the minuend.
   * @param leftC2  Second point of the minuend.
   * @param rightC1 First point of the subtrahend.
   * @param rightC2 Second point of the subtrahend.
   *
   * @return 1 on success, 0 on failure. Note this is inverted relative to most methods here.
   */
  int secp256k1_elgamal_subtract(
    Pointer ctx, Pointer diffC1, Pointer diffC2,
    Pointer leftC1, Pointer leftC2, Pointer rightC1, Pointer rightC2
  );

  // =========================================================================
  // Proof Generation
  // =========================================================================

  /**
   * Generates the Schnorr proof of knowledge for a ConfidentialMPTConvert, proving the account holds the private key
   * for the given public key and binding that proof to one transaction via the context hash.
   *
   * @param publicKey   The 33-byte public key of the account.
   * @param privateKey  The 32-byte private key of the account.
   * @param contextHash The 32-byte transaction context hash, used as the challenge.
   * @param outProof    A 64-byte buffer to receive the compact Schnorr proof.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_convert_proof(byte[] publicKey, byte[] privateKey, byte[] contextHash, byte[] outProof);

  /**
   * Generates the proof for a ConfidentialMPTClawback.
   *
   * @param privateKey      The issuer's 32-byte private key.
   * @param publicKey       The issuer's 33-byte compressed public key.
   * @param contextHash     The 32-byte context hash binding the proof to the transaction.
   * @param amount          The publicly known amount being clawed back.
   * @param encryptedAmount The 66-byte {@code sfIssuerEncryptedBalance} blob from the ledger.
   * @param outProof        A 64-byte buffer to receive the compact sigma proof.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_clawback_proof(
    byte[] privateKey, byte[] publicKey, byte[] contextHash, long amount, byte[] encryptedAmount, byte[] outProof
  );

  /**
   * Generates the proof for a ConfidentialMPTConvertBack: a 128-byte compact AND-composed sigma proof over the balance
   * witness (balance, rho, private key), followed by a 688-byte Bulletproof range proof over the remainder commitment
   * {@code pc_rem = pc_b - m*G}. Total size 816 bytes.
   *
   * @param privateKey  The holder's 32-byte private key.
   * @param publicKey   The holder's 33-byte public key.
   * @param contextHash The 32-byte context hash binding the proof to the transaction.
   * @param amount      The publicly revealed conversion amount m.
   * @param params      The balance commitment (pc_b), balance, blinding factor (rho) and balance ciphertext.
   * @param outProof    An 816-byte buffer to receive the sigma proof followed by the range proof.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_convert_back_proof(
    byte[] privateKey, byte[] publicKey, byte[] contextHash, long amount,
    MptPedersenProofParams params, byte[] outProof
  );

  // numParticipants is `long` here but `byte` in mpt_verify_send_proof because the native header declares them
  // differently (size_t vs uint8_t); the JNA mappings must match the native ABI, so they are not unified.
  /**
   * Generates the proof for a ConfidentialMPTSend: a 192-byte compact AND-composed sigma proof that simultaneously
   * proves ciphertext equality, Pedersen commitment linkage and balance ownership under one Fiat-Shamir challenge,
   * followed by a 754-byte aggregated Bulletproof range proof. Total size is fixed at 946 bytes.
   *
   * <p>{@code amountCommitment} must be computed as {@code m*G + r*H} using {@code txBlindingFactor} as the blinding
   * factor rather than an independent scalar, because the sigma proof binds it to the ciphertext randomness r.</p>
   *
   * @param privateKey       The sender's 32-byte private key.
   * @param publicKey        The sender's 33-byte public key.
   * @param amount           The amount being sent.
   * @param participants     Sender, destination, issuer and optionally auditor; index 0 must be the sender.
   * @param numParticipants  The number of participants, 3 or 4.
   * @param txBlindingFactor The ElGamal randomness r, which also blinds {@code amountCommitment}.
   * @param contextHash      The 32-byte transaction context hash.
   * @param amountCommitment The Pedersen commitment {@code pc_m = m*G + r*H}.
   * @param balanceParams    The balance commitment (pc_b), balance, blinding factor (rho) and balance ciphertext.
   * @param outProof         A buffer to receive the proof blob.
   * @param outLen           On input the buffer capacity, which must be at least 946; on output the bytes written.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_confidential_send_proof(
    byte[] privateKey, byte[] publicKey, long amount,
    MptConfidentialParticipant[] participants, long numParticipants,
    byte[] txBlindingFactor, byte[] contextHash,
    byte[] amountCommitment, MptPedersenProofParams balanceParams,
    byte[] outProof, long[] outLen
  );

  // =========================================================================
  // Proof Verification
  // =========================================================================

  /**
   * Verifies a ConfidentialMPTConvert proof, i.e. that the prover holds the private key for the given public key.
   *
   * @param proof       The 64-byte compact Schnorr proof.
   * @param publicKey   The 33-byte compressed ElGamal public key.
   * @param contextHash The 32-byte transaction context hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_verify_convert_proof(byte[] proof, byte[] publicKey, byte[] contextHash);

  /**
   * Verifies a ConfidentialMPTConvertBack proof, i.e. that the hidden balance matches its commitment and that
   * subtracting the transparent amount leaves a non-negative remainder.
   *
   * @param proof             The 816-byte proof blob (compact sigma proof followed by the Bulletproof).
   * @param publicKey         The holder's 33-byte ElGamal public key.
   * @param ciphertext        The holder's 66-byte balance ciphertext.
   * @param balanceCommitment The 33-byte Pedersen commitment to the balance.
   * @param amount            The publicly revealed conversion amount m.
   * @param contextHash       The 32-byte transaction context hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_verify_convert_back_proof(
    byte[] proof, byte[] publicKey, byte[] ciphertext,
    byte[] balanceCommitment, long amount, byte[] contextHash
  );

  /**
   * Verifies a ConfidentialMPTSend proof: the 192-byte compact sigma proof covering ciphertext correctness, Pedersen
   * commitment linkage and balance ownership, followed by the 754-byte aggregated Bulletproof range proof.
   *
   * @param proof                    The 946-byte proof blob.
   * @param participants             The participants' public keys and ciphertexts; index 0 must be the sender.
   * @param numParticipants          The number of participants, 3 or 4.
   * @param senderSpendingCiphertext The sender's on-ledger balance ciphertext (b1 || b2).
   * @param amountCommitment         The Pedersen commitment pc_m to the transfer amount.
   * @param balanceCommitment        The Pedersen commitment pc_b to the sender's balance.
   * @param contextHash              The 32-byte transaction context hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_verify_send_proof(
    byte[] proof,
    MptConfidentialParticipant[] participants, byte numParticipants,
    byte[] senderSpendingCiphertext, byte[] amountCommitment,
    byte[] balanceCommitment, byte[] contextHash
  );

  /**
   * Verifies a ConfidentialMPTClawback proof, i.e. that the ciphertext decrypts under the issuer's key to exactly the
   * amount named in the transaction.
   *
   * @param proof       The 64-byte compact sigma proof.
   * @param amount      The publicly known amount being clawed back.
   * @param publicKey   The issuer's 33-byte compressed public key.
   * @param ciphertext  The 66-byte {@code sfIssuerEncryptedBalance} blob for the holder's account.
   * @param contextHash The 32-byte transaction context hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_verify_clawback_proof(byte[] proof, long amount, byte[] publicKey, byte[] ciphertext, byte[] contextHash);

  // =========================================================================
  // Context Hash Generation
  // =========================================================================
  //
  // Each hash binds a proof to one specific transaction. The inputs differ per
  // transaction type, which is what stops a proof built for one being replayed
  // in another. Every one writes a 32-byte hash.

  /**
   * Computes the context hash for a ConfidentialMPTConvert.
   *
   * @param account    The 20-byte account ID of the submitter.
   * @param issuanceId The 24-byte MPT issuance ID.
   * @param sequence   The transaction sequence number.
   * @param outHash    A 32-byte buffer to receive the hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_convert_context_hash(MptAccountId account, MptIssuanceId issuanceId, int sequence, byte[] outHash);

  /**
   * Computes the context hash for a ConfidentialMPTConvertBack. Includes the confidential balance version, so a proof
   * is invalidated by any intervening change to the holder's balance.
   *
   * @param account    The 20-byte account ID of the holder.
   * @param issuanceId The 24-byte MPT issuance ID.
   * @param sequence   The transaction sequence number.
   * @param version    The holder's current confidential balance version.
   * @param outHash    A 32-byte buffer to receive the hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_convert_back_context_hash(
    MptAccountId account, MptIssuanceId issuanceId, int sequence, int version, byte[] outHash
  );

  /**
   * Computes the context hash for a ConfidentialMPTSend. Binds the destination as well as the sender's balance
   * version, so a proof cannot be redirected to a different recipient.
   *
   * @param account     The 20-byte account ID of the sender.
   * @param issuanceId  The 24-byte MPT issuance ID.
   * @param sequence    The transaction sequence number.
   * @param destination The 20-byte account ID of the destination.
   * @param version     The sender's current confidential balance version.
   * @param outHash     A 32-byte buffer to receive the hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_send_context_hash(
    MptAccountId account, MptIssuanceId issuanceId, int sequence, MptAccountId destination, int version, byte[] outHash
  );

  /**
   * Computes the context hash for a ConfidentialMPTClawback.
   *
   * @param account    The 20-byte account ID of the issuer.
   * @param issuanceId The 24-byte MPT issuance ID.
   * @param sequence   The transaction sequence number.
   * @param holder     The 20-byte account ID of the holder being clawed back from.
   * @param outHash    A 32-byte buffer to receive the hash.
   *
   * @return 0 on success, -1 on failure.
   */
  int mpt_get_clawback_context_hash(
    MptAccountId account, MptIssuanceId issuanceId, int sequence, MptAccountId holder, byte[] outHash
  );

  // =========================================================================
  // JNA Struct Types
  // =========================================================================

  @Structure.FieldOrder({"bytes"})
  class MptAccountId extends Structure implements Structure.ByValue {
    public byte[] bytes = new byte[20];
  }

  @Structure.FieldOrder({"bytes"})
  class MptIssuanceId extends Structure implements Structure.ByValue {
    public byte[] bytes = new byte[24];
  }

  @Structure.FieldOrder({"pedersenCommitment", "amount", "encryptedAmount", "blindingFactor"})
  class MptPedersenProofParams extends Structure {
    public byte[] pedersenCommitment = new byte[33];
    public long amount;
    public byte[] encryptedAmount = new byte[66];
    public byte[] blindingFactor = new byte[32];
  }

  @Structure.FieldOrder({"publicKey", "ciphertext"})
  class MptConfidentialParticipant extends Structure {
    public byte[] publicKey = new byte[33];
    public byte[] ciphertext = new byte[66];
  }

  // =========================================================================
  // Singleton Holder (lazy initialization)
  // =========================================================================

  /**
   * Loads the native library via {@code loader}, wrapping any {@link UnsatisfiedLinkError} with actionable guidance on
   * making the library discoverable. Exposed only so the error handling can be exercised without the native library
   * present; production code loads via {@link #getInstance()}.
   *
   * @param loader Supplies the native library (in production, {@code Native.load(...)}).
   *
   * @return The loaded {@link MptCryptoLibrary}.
   *
   * @throws UnsatisfiedLinkError wrapping the loader's error (with guidance) if loading fails.
   */
  @VisibleForTesting
  static MptCryptoLibrary load(final Supplier<MptCryptoLibrary> loader) {
    try {
      return loader.get();
    } catch (UnsatisfiedLinkError e) {
      UnsatisfiedLinkError error = new UnsatisfiedLinkError(
        "Unable to load the native 'mpt-crypto' library. Ensure it is installed and discoverable, " +
          "e.g. via the 'jna.library.path' system property or the system library path. " +
          "Original error: " + e.getMessage()
      );
      error.initCause(e);
      throw error;
    }
  }

  /**
   * Holder class for lazy initialization of the native library singleton.
   */
  final class Holder {
    private static final MptCryptoLibrary INSTANCE =
      load(() -> Native.load("mpt-crypto", MptCryptoLibrary.class));
  }
}
