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
 * ConfidentialMPTConvert.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableConfidentialMPTSend.class)
@JsonDeserialize(as = ImmutableConfidentialMPTSend.class)
public interface ConfidentialMPTSend extends Transaction {

  /**
   * Construct a {@code ConfidentialMPTSend} builder.
   *
   * @return An {@link ImmutableConfidentialMPTSend.Builder}.
   */
  static ImmutableConfidentialMPTSend.Builder builder() {
    return ImmutableConfidentialMPTSend.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link ConfidentialMPTSend}, which only allows the
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
   * @return An {@link EncryptedAmount} containing the hex-encoded ciphertext.
   */
  @JsonProperty("SenderEncryptedAmount")
  EncryptedAmount senderEncryptedAmount();

  /**
   * Ciphertext credited to the receiver's inbox balance.
   *
   * @return An {@link EncryptedAmount} containing the hex-encoded ciphertext.
   */
  @JsonProperty("DestinationEncryptedAmount")
  EncryptedAmount destinationEncryptedAmount();

  /**
   * Ciphertext used to update the issuer mirror balance.
   *
   * @return An {@link EncryptedAmount} containing the hex-encoded ciphertext.
   */
  @JsonProperty("IssuerEncryptedAmount")
  EncryptedAmount issuerEncryptedAmount();

  /**
   * ZKP bundle establishing equality, linkage, and range sufficiency.
   *
   * @return A hex-encoded string containing the proof.
   */
  @JsonProperty("ZKProof")
  String zkProof();

  /**
   * A cryptographic commitment to the user's confidential spending balance.
   *
   * @return A hex-encoded string containing the commitment.
   */
  @JsonProperty("PedersenCommitment")
  String pedersenCommitment();

  /**
   * Ciphertext for the auditor. Required if {@code sfAuditorElGamalPublicKey} is present on the issuance.
   *
   * @return An optionally-present {@link EncryptedAmount} containing the hex-encoded ciphertext.
   */
  @JsonProperty("AuditorEncryptedAmount")
  Optional<EncryptedAmount> auditorEncryptedAmount();
}

