package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * Object mapping for the {@code DIDDelete} transaction.
 *
 * <p>This constant will be marked {@link Beta} until the featureDID amendment is enabled on mainnet. Its API
 * is subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableDidDelete.class)
@JsonDeserialize(as = ImmutableDidDelete.class)
public interface DidDelete extends Transaction {

  /**
   * Construct a {@code DidDelete} builder.
   *
   * @return An {@link ImmutableDidDelete.Builder}.
   */
  static ImmutableDidDelete.Builder builder() {
    return ImmutableDidDelete.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link DidDelete}, which only allows the {@code tfFullyCanonicalSig}
   * flag, which is deprecated.
   *
   * <p>The value of the flags can be set manually, but exists mostly for JSON serialization/deserialization only and
   * for proper signature computation in rippled.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }


}
