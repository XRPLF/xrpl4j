package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.transactions.ImmutableMpTokenIssuanceDestroy.Builder;

import java.util.Optional;

/**
 * Representation of the {@code MPTokenIssuanceDestroy} transaction.
 */
@Immutable
@JsonSerialize(as = ImmutableMpTokenIssuanceDestroy.class)
@JsonDeserialize(as = ImmutableMpTokenIssuanceDestroy.class)
public interface MpTokenIssuanceDestroy extends Transaction {

  /**
   * Construct a {@code MpTokenIssuanceDestroy} builder.
   *
   * @return An {@link Builder}.
   */
  static Builder builder() {
    return ImmutableMpTokenIssuanceDestroy.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link MpTokenIssuanceDestroy}, which only allows the
   * {@code tfFullyCanonicalSig} flag, which is deprecated.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The {@link MpTokenIssuanceId} of the issuance to destroy.
   *
   * @return An {@link MpTokenIssuanceId}.
   */
  @JsonProperty("MPTokenIssuanceID")
  MpTokenIssuanceId mpTokenIssuanceId();

}
