package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * Performs a confidential transfer of MPT value between accounts while keeping the transfer amount hidden.
 * The transferred amount is credited to the receiver's confidential inbox balance (CB_IN) to avoid proof staleness;
 * the receiver may later merge these funds into the spending balance (CB_S) via ConfidentialMergeInbox.
 *
 * <p>This transaction requires both sender and receiver to have registered ElGamal public keys via
 * ConfidentialMptConvert.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableConfidentialMptSend.class)
@JsonDeserialize(as = ImmutableConfidentialMptSend.class)
public interface ConfidentialMptSend extends Transaction {

  /**
   * Construct a {@code ConfidentialMptSend} builder.
   *
   * @return An {@link ImmutableConfidentialMptSend.Builder}.
   */
  static ImmutableConfidentialMptSend.Builder builder() {
    return ImmutableConfidentialMptSend.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link ConfidentialMptSend}, which only allows the
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
   * The receiver's XRPL account.
   *
   * @return An {@link Address} representing the destination account.
   */
  @JsonProperty("Destination")
  Address destination();

  /**
   * The unique identifier for the MPT issuance being transferred.
   *
   * @return An {@link MpTokenIssuanceId}.
   */
  @JsonProperty("MPTokenIssuanceID")
  MpTokenIssuanceId mpTokenIssuanceId();

  /**
   * Ciphertext used to homomorphically debit the sender's spending balance.
   *
   * @return A hex-encoded {@link String} containing the ciphertext.
   */
  @JsonProperty("SenderEncryptedAmount")
  String senderEncryptedAmount();

  /**
   * Ciphertext credited to the receiver's inbox balance.
   *
   * @return A hex-encoded {@link String} containing the ciphertext.
   */
  @JsonProperty("DestinationEncryptedAmount")
  String destinationEncryptedAmount();

  /**
   * Ciphertext used to update the issuer mirror balance.
   *
   * @return A hex-encoded {@link String} containing the ciphertext.
   */
  @JsonProperty("IssuerEncryptedAmount")
  String issuerEncryptedAmount();

  /**
   * ZKP bundle establishing equality, linkage, and range sufficiency.
   *
   * @return A hex-encoded string containing the proof.
   */
  @JsonProperty("ZKProof")
  String zkProof();

  /**
   * A Pedersen commitment to the transfer amount.
   * Used to prove the amount is within valid range without revealing it.
   *
   * @return A hex-encoded string containing the amount commitment.
   */
  @JsonProperty("AmountCommitment")
  String amountCommitment();

  /**
   * A Pedersen commitment to the sender's new spending balance after the transfer.
   * Used to prove the balance remains non-negative without revealing it.
   *
   * @return A hex-encoded string containing the balance commitment.
   */
  @JsonProperty("BalanceCommitment")
  String balanceCommitment();

  /**
   * Ciphertext for the auditor. Required if {@code sfAuditorElGamalPublicKey} is present on the issuance.
   *
   * @return An optionally-present hex-encoded {@link String} containing the ciphertext.
   */
  @JsonProperty("AuditorEncryptedAmount")
  Optional<String> auditorEncryptedAmount();
}

