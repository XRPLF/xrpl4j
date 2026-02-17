package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
@JsonSerialize(as = ImmutableConfidentialMPTConvertBack.class)
@JsonDeserialize(as = ImmutableConfidentialMPTConvertBack.class)
public interface ConfidentialMPTConvertBack extends Transaction {

  /**
   * Construct a {@code ConfidentialMPTConvertBack} builder.
   *
   * @return An {@link ImmutableConfidentialMPTConvertBack.Builder}.
   */
  static ImmutableConfidentialMPTConvertBack.Builder builder() {
    return ImmutableConfidentialMPTConvertBack.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link ConfidentialMPTConvertBack}, which only allows the
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
   * @return A hex-encoded {@link String} containing the ciphertext.
   */
  @JsonProperty("HolderEncryptedAmount")
  String holderEncryptedAmount();

  /**
   * Ciphertext to be subtracted from the issuer's mirror balance.
   *
   * @return A hex-encoded {@link String} containing the ciphertext.
   */
  @JsonProperty("IssuerEncryptedAmount")
  String issuerEncryptedAmount();

  /**
   * The 32-byte scalar value used to encrypt the amount. Used by validators to verify the ciphertexts match the
   * plaintext MPTAmount.
   *
   * @return A hex-encoded {@link String} containing the blinding factor.
   */
  @JsonProperty("BlindingFactor")
  String blindingFactor();

  /**
   * A cryptographic commitment to the user's confidential spending balance after the conversion. Used to prove the
   * balance remains non-negative without revealing it.
   *
   * @return A hex-encoded string containing the Pedersen commitment.
   */
  @JsonProperty("BalanceCommitment")
  String balanceCommitment();

  /**
   * A bundle containing the Pedersen Linkage Proof (linking the ElGamal balance to the commitment) and the Range Proof
   * (proving the remaining balance is non-negative).
   *
   * @return A hex-encoded string containing the ZK proof bundle.
   */
  @JsonProperty("ZKProof")
  String zkProof();

  /**
   * Ciphertext for the auditor. Required if {@code sfAuditorElGamalPublicKey} is present on the issuance.
   *
   * @return An optionally-present hex-encoded {@link String} containing the ciphertext.
   */
  @JsonProperty("AuditorEncryptedAmount")
  Optional<String> auditorEncryptedAmount();
}

