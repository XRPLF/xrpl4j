package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * Object mapping for the {@code XChainClaim} transaction.
 *
 * <p>This interface will be marked {@link Beta} until the featureXChainBridge amendment is enabled on mainnet. Its API
 * is subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableXChainClaim.class)
@JsonDeserialize(as = ImmutableXChainClaim.class)
public interface XChainClaim extends Transaction {

  /**
   * Construct a {@code XChainClaim} builder.
   *
   * @return An {@link ImmutableXChainClaim.Builder}.
   */
  static ImmutableXChainClaim.Builder builder() {
    return ImmutableXChainClaim.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link XChainClaim}, which only allows the {@code tfFullyCanonicalSig}
   * flag, which is deprecated.
   *
   * @return A set of {@link TransactionFlags}, default is {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The amount to claim on the destination chain. This must match the amount attested to on the attestations associated
   * with {@link #xChainClaimId()}.
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("Amount")
  CurrencyAmount amount();

  /**
   * The destination account on the destination chain. It must exist or the transaction will fail. However, if the
   * transaction fails in this case, the sequence number and collected signatures won't be destroyed, and the
   * transaction can be rerun with a different destination.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Destination")
  Address destination();

  /**
   * A 32-bit unsigned integer destination tag.
   *
   * @return An optional {@link UnsignedInteger}
   */
  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

  /**
   * The bridge to use to transfer funds.
   *
   * @return An {@link XChainBridge}.
   */
  @JsonProperty("XChainBridge")
  @SuppressWarnings("MethodName")
  XChainBridge xChainBridge();

  /**
   * The unique integer ID for the cross-chain transfer that was referenced in the corresponding {@code XChainCommit}
   * transaction.
   *
   * @return An {@link XChainClaimId}.
   */
  @JsonProperty("XChainClaimID")
  @SuppressWarnings("MethodName")
  XChainClaimId xChainClaimId();

}
