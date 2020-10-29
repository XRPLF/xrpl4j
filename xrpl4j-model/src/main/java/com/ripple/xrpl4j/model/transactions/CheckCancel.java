package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.model.transactions.Flags.TransactionFlags;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

/**
 * Cancels an unredeemed Check, removing it from the ledger without sending any money. The source or the
 * destination of the check can cancel a Check at any time using this transaction type.
 * If the Check has expired, any address can cancel it.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCheckCancel.class)
@JsonDeserialize(as = ImmutableCheckCancel.class)
public interface CheckCancel extends Transaction<TransactionFlags> {

  static ImmutableCheckCancel.Builder builder() {
    return ImmutableCheckCancel.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link AccountDelete}, which only allows tfFullyCanonicalSig flag.
   * <p>
   * The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   */
  @JsonProperty("Flags")
  @Derived
  default TransactionFlags flags() {
    return new TransactionFlags.Builder().fullyCanonicalSig(true).build();
  }

  /**
   * The ID of the Check ledger object to cancel, as a 64-character hexadecimal string.
   * @return A {@link Hash256} containing the ID of the Check ledger object in hexadecimal form.
   */
  @JsonProperty("CheckID")
  Hash256 checkId();
}
