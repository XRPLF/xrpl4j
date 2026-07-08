package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * Converts confidential MPT value back into public (visible) MPT balance.
 *
 * <p>For a holder: restores public balance from the confidential spending balance (CB_S).
 * For the issuer's second account: returns confidential supply to issuer reserve.</p>
 *
 * <p>This transaction requires the holder to have a registered ElGamal public key and
 * a non-zero confidential spending balance.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableConfidentialMptConvertBack.class)
@JsonDeserialize(as = ImmutableConfidentialMptConvertBack.class)
public interface ConfidentialMptConvertBack extends Transaction {

  /**
   * The required length, in hex characters, of the {@link ZkProof} for a {@link ConfidentialMptConvertBack} (816 bytes:
   * a 128-byte compact sigma proof plus a 688-byte single bulletproof).
   */
  int CONVERT_BACK_ZK_PROOF_HEX_LENGTH = 1632;

  /**
   * Construct a {@code ConfidentialMptConvertBack} builder.
   *
   * @return An {@link ImmutableConfidentialMptConvertBack.Builder}.
   */
  static ImmutableConfidentialMptConvertBack.Builder builder() {
    return ImmutableConfidentialMptConvertBack.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link ConfidentialMptConvertBack}, which only allows the
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
   * The plaintext amount to credit to the public balance.
   *
   * @return An {@link MpTokenNumericAmount}.
   */
  @JsonProperty("MPTAmount")
  MpTokenNumericAmount mptAmount();

  /**
   * Ciphertext to be subtracted from the holder's confidential spending balance (sfConfidentialBalanceSpending).
   *
   * @return An {@link EncryptedAmount} containing the ciphertext.
   */
  @JsonProperty("HolderEncryptedAmount")
  EncryptedAmount holderEncryptedAmount();

  /**
   * Ciphertext to be subtracted from the issuer's mirror balance.
   *
   * @return An {@link EncryptedAmount} containing the ciphertext.
   */
  @JsonProperty("IssuerEncryptedAmount")
  EncryptedAmount issuerEncryptedAmount();

  /**
   * The 32-byte scalar value used to encrypt the amount. Used by validators to verify the ciphertexts match the
   * plaintext MPTAmount.
   *
   * @return A {@link BlindingFactor} containing the blinding factor.
   */
  @JsonProperty("BlindingFactor")
  BlindingFactor blindingFactor();

  /**
   * A cryptographic commitment to the user's confidential spending balance after the conversion. Used to prove the
   * balance remains non-negative without revealing it.
   *
   * @return A {@link Commitment} containing the Pedersen commitment.
   */
  @JsonProperty("BalanceCommitment")
  Commitment balanceCommitment();

  /**
   * A bundle containing the Pedersen Linkage Proof (linking the ElGamal balance to the commitment) and the Range Proof
   * (proving the remaining balance is non-negative).
   *
   * @return A {@link ZkProof} containing the proof bundle.
   */
  @JsonProperty("ZKProof")
  ZkProof zkProof();

  /**
   * Ciphertext for the auditor. Required if {@code sfAuditorEncryptionKey} is present on the issuance.
   *
   * @return An optionally-present {@link EncryptedAmount} containing the ciphertext.
   */
  @JsonProperty("AuditorEncryptedAmount")
  Optional<EncryptedAmount> auditorEncryptedAmount();

  /**
   * Validates invariants for {@link ConfidentialMptConvertBack}, mirroring the {@code temMALFORMED} and
   * {@code temBAD_AMOUNT} checks in {@code rippled}'s {@code ConfidentialMPTConvertBack} preflight.
   *
   * <ul>
   *   <li>{@code MPTAmount} must be non-zero and no greater than the maximum allowable supply
   *       ({@code temBAD_AMOUNT} in {@code rippled}).</li>
   *   <li>{@code ZKProof} must be exactly {@value #CONVERT_BACK_ZK_PROOF_HEX_LENGTH} hex characters (816 bytes).</li>
   * </ul>
   */
  @Value.Check
  default void validateConfidentialMptConvertBack() {
    Preconditions.checkState(
      !mptAmount().value().equals(UnsignedLong.ZERO),
      "MPTAmount must not be zero for ConfidentialMptConvertBack."
    );

    Preconditions.checkState(
      mptAmount().value().compareTo(MpTokenNumericAmount.MAX_AMOUNT) <= 0,
      "MPTAmount must not exceed the maximum allowable supply (%s).", MpTokenNumericAmount.MAX_AMOUNT
    );

    Preconditions.checkState(
      zkProof().value().length() == CONVERT_BACK_ZK_PROOF_HEX_LENGTH,
      "ZKProof must be %s bytes (%s hex characters) for ConfidentialMptConvertBack.",
      CONVERT_BACK_ZK_PROOF_HEX_LENGTH / 2, CONVERT_BACK_ZK_PROOF_HEX_LENGTH
    );
  }
}
