package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

/**
 * Object mapping for the {@code XChainCreateClaimId} transaction.
 *
 * <p>This interface will be marked {@link Beta} until the featureXChainBridge amendment is enabled on mainnet. Its API
 * is subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableXChainCreateClaimId.class)
@JsonDeserialize(as = ImmutableXChainCreateClaimId.class)
public interface XChainCreateClaimId extends Transaction {

  /**
   * Construct a {@code XChainCreateClaimId} builder.
   *
   * @return An {@link ImmutableXChainCreateClaimId.Builder}.
   */
  static ImmutableXChainCreateClaimId.Builder builder() {
    return ImmutableXChainCreateClaimId.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link XChainCreateClaimId}, which only allows the
   * {@code tfFullyCanonicalSig} flag, which is deprecated.
   *
   * @return A set of {@link TransactionFlags}, default is {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The account that must send the {@link XChainCommit} transaction on the source chain.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("OtherChainSource")
  Address otherChainSource();

  /**
   * The amount, in XRP, to reward the witness servers for providing signatures. This must match the amount on the
   * {@code Bridge} ledger object.
   *
   * @return An {@link XrpCurrencyAmount}.
   */
  @JsonProperty("SignatureReward")
  XrpCurrencyAmount signatureReward();

  /**
   * The bridge to create the claim ID for.
   *
   * @return An {@link XChainBridge}.
   */
  @JsonProperty("XChainBridge")
  @SuppressWarnings("MethodName")
  XChainBridge xChainBridge();

}
