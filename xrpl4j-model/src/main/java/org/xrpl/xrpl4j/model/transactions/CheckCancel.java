package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.xrpl.xrpl4j.model.client.accounts.ImmutableAccountChannelsRequestParams;
import org.xrpl.xrpl4j.model.flags.Flags;

/**
 * Cancels an unredeemed Check, removing it from the ledger without sending any money. The source or the
 * destination of the check can cancel a Check at any time using this transaction type.
 * If the Check has expired, any address can cancel it.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCheckCancel.class)
@JsonDeserialize(as = ImmutableCheckCancel.class)
public interface CheckCancel extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableCheckCancel.Builder}.
   */
  static ImmutableCheckCancel.Builder builder() {
    return ImmutableCheckCancel.builder();
  }

  /**
   * Set of {@link Flags.TransactionFlags}s for this {@link AccountDelete}, which only allows the
   * {@code tfFullyCanonicalSig} flag.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   *
   * @return Always {@link Flags.TransactionFlags} with {@code tfFullyCanonicalSig} set.
   */
  @JsonProperty("Flags")
  @Derived
  default Flags.TransactionFlags flags() {
    return new Flags.TransactionFlags.Builder().tfFullyCanonicalSig(true).build();
  }

  /**
   * The ID of the Check ledger object to cancel, as a 64-character hexadecimal string.
   *
   * @return A {@link Hash256} containing the ID of the Check ledger object in hexadecimal form.
   */
  @JsonProperty("CheckID")
  Hash256 checkId();
}
