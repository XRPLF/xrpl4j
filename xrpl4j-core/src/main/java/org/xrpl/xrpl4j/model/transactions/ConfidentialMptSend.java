package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.HashSet;
import java.util.List;
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
   * The required length, in hex characters, of the {@link ZkProof} for a {@link ConfidentialMptSend} (946 bytes: a
   * 192-byte compact sigma proof plus a 754-byte double bulletproof).
   */
  int SEND_ZK_PROOF_HEX_LENGTH = 1892;

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
   * The unique identifier for the MPT issuance being transferred.
   *
   * @return An {@link MpTokenIssuanceId}.
   */
  @JsonProperty("MPTokenIssuanceID")
  MpTokenIssuanceId mpTokenIssuanceId();

  /**
   * The receiver's XRPL account.
   *
   * @return An {@link Address} representing the destination account.
   */
  @JsonProperty("Destination")
  Address destination();

  /**
   * An arbitrary tag that identifies the reason for the payment to the destination, or a hosted recipient to pay.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the destination tag.
   */
  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

  /**
   * Ciphertext used to homomorphically debit the sender's spending balance.
   *
   * @return An {@link EncryptedAmount} containing the ciphertext.
   */
  @JsonProperty("SenderEncryptedAmount")
  EncryptedAmount senderEncryptedAmount();

  /**
   * Ciphertext credited to the receiver's inbox balance.
   *
   * @return An {@link EncryptedAmount} containing the ciphertext.
   */
  @JsonProperty("DestinationEncryptedAmount")
  EncryptedAmount destinationEncryptedAmount();

  /**
   * Ciphertext used to update the issuer mirror balance.
   *
   * @return An {@link EncryptedAmount} containing the ciphertext.
   */
  @JsonProperty("IssuerEncryptedAmount")
  EncryptedAmount issuerEncryptedAmount();

  /**
   * Ciphertext for the auditor. Required if {@code sfAuditorEncryptionKey} is present on the issuance.
   *
   * @return An optionally-present {@link EncryptedAmount} containing the ciphertext.
   */
  @JsonProperty("AuditorEncryptedAmount")
  Optional<EncryptedAmount> auditorEncryptedAmount();

  /**
   * ZKP bundle establishing equality, linkage, and range sufficiency.
   *
   * @return A {@link ZkProof} containing the proof.
   */
  @JsonProperty("ZKProof")
  ZkProof zkProof();

  /**
   * A Pedersen commitment to the transfer amount.
   * Used to prove the amount is within valid range without revealing it.
   *
   * @return A {@link Commitment} containing the amount commitment.
   */
  @JsonProperty("AmountCommitment")
  Commitment amountCommitment();

  /**
   * A Pedersen commitment to the sender's new spending balance after the transfer.
   * Used to prove the balance remains non-negative without revealing it.
   *
   * @return A {@link Commitment} containing the balance commitment.
   */
  @JsonProperty("BalanceCommitment")
  Commitment balanceCommitment();

  /**
   * Set of Credentials to authorize a deposit made by this transaction. Each member of the array must be the ledger
   * entry ID of a Credential entry in the ledger.
   *
   * @return A {@link List} of type {@link Hash256}.
   */
  @JsonProperty("CredentialIDs")
  List<Hash256> credentialIds();

  /**
   * Validates invariants for {@link ConfidentialMptSend}, mirroring the {@code temMALFORMED} checks in {@code rippled}'s
   * {@code ConfidentialMPTSend} preflight.
   *
   * <ul>
   *   <li>The {@code Account} (sender) must not equal the {@code Destination} — an account cannot send to itself.</li>
   *   <li>{@code ZKProof} must be exactly {@value #SEND_ZK_PROOF_HEX_LENGTH} hex characters (946 bytes).</li>
   *   <li>{@code CredentialIDs}, when present, must contain at most 8 unique entries.</li>
   * </ul>
   */
  @Value.Check
  default void validateConfidentialMptSend() {
    Preconditions.checkState(
      !account().equals(destination()),
      "Account and Destination must not be the same (an account cannot send to itself)."
    );

    Preconditions.checkState(
      zkProof().value().length() == SEND_ZK_PROOF_HEX_LENGTH,
      "ZKProof must be %s bytes (%s hex characters) for ConfidentialMptSend.",
      SEND_ZK_PROOF_HEX_LENGTH / 2, SEND_ZK_PROOF_HEX_LENGTH
    );

    if (!credentialIds().isEmpty()) {
      Preconditions.checkState(
        credentialIds().size() <= 8,
        "CredentialIDs should have less than or equal to 8 items."
      );
      Preconditions.checkState(
        new HashSet<>(credentialIds()).size() == credentialIds().size(),
        "CredentialIDs should have unique values."
      );
    }
  }
}
