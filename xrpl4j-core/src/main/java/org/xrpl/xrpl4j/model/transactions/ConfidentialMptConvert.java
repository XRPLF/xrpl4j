package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * Converts a holder's own visible (public) MPT balance into confidential form. The converted amount is credited to the
 * holder's confidential inbox balance (CB_IN) to avoid immediate proof staleness, requiring an explicit merge into the
 * spending balance (CB_S) before use.
 *
 * <p>This transaction also serves as the opt-in mechanism for confidential MPT participation: by executing it
 * (including a zero-amount conversion), a holder's HolderEncryptionKey is recorded on their MPToken object, enabling
 * the holder to receive and manage confidential funds.</p>
 *
 * <p>This transaction is a self-conversion only. Issuers introduce supply exclusively through existing XLS-33
 * public issuance mechanisms.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableConfidentialMptConvert.class)
@JsonDeserialize(as = ImmutableConfidentialMptConvert.class)
public interface ConfidentialMptConvert extends Transaction {

  /**
   * The required length, in hex characters, of the Schnorr proof-of-knowledge {@link ZkProof} used to register a new
   * {@code HolderEncryptionKey} (64 bytes).
   */
  int SCHNORR_ZK_PROOF_HEX_LENGTH = 128;

  /**
   * Construct a {@code ConfidentialMptConvert} builder.
   *
   * @return An {@link ImmutableConfidentialMptConvert.Builder}.
   */
  static ImmutableConfidentialMptConvert.Builder builder() {
    return ImmutableConfidentialMptConvert.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link ConfidentialMptConvert}, which only allows the
   * {@code tfFullyCanonicalSig} flag, which is deprecated.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The unique identifier for the MPT issuance.
   *
   * @return An {@link MpTokenIssuanceId}.
   */
  @JsonProperty("MPTokenIssuanceID")
  MpTokenIssuanceId mpTokenIssuanceId();

  /**
   * The public plaintext amount to convert from public to confidential balance.
   *
   * @return An {@link MpTokenNumericAmount}.
   */
  @JsonProperty("MPTAmount")
  MpTokenNumericAmount mptAmount();

  /**
   * The holder's ElGamal encryption key. Mandatory if the account has not yet registered a key (initialization).
   * Forbidden if a key is already registered.
   *
   * <p>This is a 33-byte compressed EC public key.</p>
   *
   * @return An optionally-present {@link PublicKey}.
   */
  @JsonProperty("HolderEncryptionKey")
  Optional<PublicKey> holderEncryptionKey();

  /**
   * ElGamal ciphertext credited to the holder's confidential inbox balance (CB_IN).
   *
   * @return An {@link EncryptedAmount} containing the ciphertext.
   */
  @JsonProperty("HolderEncryptedAmount")
  EncryptedAmount holderEncryptedAmount();

  /**
   * ElGamal ciphertext credited to the issuer's mirror balance.
   *
   * @return An {@link EncryptedAmount} containing the ciphertext.
   */
  @JsonProperty("IssuerEncryptedAmount")
  EncryptedAmount issuerEncryptedAmount();

  /**
   * ElGamal ciphertext for the auditor. Required if {@code sfAuditorEncryptionKey} is present on the issuance.
   *
   * @return An optionally-present {@link EncryptedAmount} containing the ciphertext.
   */
  @JsonProperty("AuditorEncryptedAmount")
  Optional<EncryptedAmount> auditorEncryptedAmount();

  /**
   * The 32-byte scalar value used to encrypt the amount. Used by validators to verify the ciphertexts match the
   * plaintext MPTAmount.
   *
   * @return A {@link BlindingFactor} containing the blinding factor.
   */
  @JsonProperty("BlindingFactor")
  BlindingFactor blindingFactor();

  /**
   * A Schnorr Proof of Knowledge (PoK): proves the knowledge of the private key for the provided ElGamal Public Key.
   * Required when registering a new HolderEncryptionKey, and forbidden otherwise.
   *
   * <p>When present, this is a 64-byte (128 hex character) proof.</p>
   *
   * @return An optionally-present {@link ZkProof}.
   */
  @JsonProperty("ZKProof")
  Optional<ZkProof> zkProof();

  /**
   * Validates field-combination invariants for {@link ConfidentialMptConvert}, mirroring the {@code temMALFORMED} and
   * {@code temBAD_AMOUNT} checks in {@code rippled}'s {@code ConfidentialMPTConvert} preflight.
   *
   * <ul>
   *   <li>{@code MPTAmount} must be no greater than the maximum allowable supply ({@code temBAD_AMOUNT} in
   *       {@code rippled}). A zero amount is permitted, since a zero-amount conversion is the opt-in mechanism used to
   *       register a {@code HolderEncryptionKey}.</li>
   *   <li>{@code HolderEncryptionKey} and {@code ZKProof} must both be present (when registering a new encryption key)
   *       or both be absent — a proof of knowledge is required exactly when a key is being registered.</li>
   *   <li>When present, {@code ZKProof} must be exactly {@value #SCHNORR_ZK_PROOF_HEX_LENGTH} hex characters
   *       (64 bytes), the length of a Schnorr proof of knowledge.</li>
   * </ul>
   */
  @Value.Check
  default void validateFieldCombinations() {
    Preconditions.checkState(
      mptAmount().value().compareTo(MpTokenNumericAmount.MAX_AMOUNT) <= 0,
      "MPTAmount must not exceed the maximum allowable supply (%s).", MpTokenNumericAmount.MAX_AMOUNT
    );

    Preconditions.checkState(
      holderEncryptionKey().isPresent() == zkProof().isPresent(),
      "HolderEncryptionKey and ZKProof must both be present (when registering a new encryption key) or both be absent."
    );

    zkProof().ifPresent(proof -> Preconditions.checkState(
      proof.value().length() == SCHNORR_ZK_PROOF_HEX_LENGTH,
      "ZKProof must be %s bytes (%s hex characters) for ConfidentialMptConvert.",
      SCHNORR_ZK_PROOF_HEX_LENGTH / 2, SCHNORR_ZK_PROOF_HEX_LENGTH
    ));
  }
}
