package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

/**
 * Moves all funds from the inbox balance into the spending balance, then resets the inbox to a canonical encrypted
 * zero (EncZero). This ensures that proofs reference only stable spending balances and prevents staleness from
 * incoming transfers.
 *
 * <p>This transaction is required after receiving confidential transfers via {@link ConfidentialMptConvert} or
 * {@link ConfidentialMptSend} before the funds can be spent.</p>
 *
 * <p>No ZK proofs are required for this transaction since the ledger knows the exact value being moved.</p>
 *
 * <p>This class will be marked {@link Beta} until the ConfidentialTransfer amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableConfidentialMptMergeInbox.class)
@JsonDeserialize(as = ImmutableConfidentialMptMergeInbox.class)
@Beta
public interface ConfidentialMptMergeInbox extends Transaction {

  /**
   * Construct a {@code ConfidentialMptMergeInbox} builder.
   *
   * @return An {@link ImmutableConfidentialMptMergeInbox.Builder}.
   */
  static ImmutableConfidentialMptMergeInbox.Builder builder() {
    return ImmutableConfidentialMptMergeInbox.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link ConfidentialMptMergeInbox}, which only allows the
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
}

