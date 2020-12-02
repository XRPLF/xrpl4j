package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.ripple.xrpl4j.model.transactions.Flags.TransactionFlags;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.util.Optional;

/**
 * A {@link DepositPreAuth} transaction gives another account pre-approval to deliver payments to the sender of
 * this transaction. This is only useful if the sender of this transaction is using (or plans to use)
 * <a href="https://xrpl.org/depositauth.html">Deposit Authorization</a>.
 *
 * <p>You can use this transaction to preauthorize certain counterparties before you enable Deposit Authorization.
 * This may be useful to ensure a smooth transition from not requiring deposit authorization to requiring it.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDepositPreAuth.class)
@JsonDeserialize(as = ImmutableDepositPreAuth.class)
public interface DepositPreAuth extends Transaction {

  static ImmutableDepositPreAuth.Builder builder() {
    return ImmutableDepositPreAuth.builder();
  }

  /**
   * Set of {@link Flags.TransactionFlags}s for this {@link AccountDelete}, which only allows tfFullyCanonicalSig flag.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   */
  @JsonProperty("Flags")
  @Derived
  default TransactionFlags flags() {
    return new TransactionFlags.Builder().fullyCanonicalSig(true).build();
  }

  /**
   * The XRP Ledger {@link Address} of the sender to preauthorize.
   */
  @JsonProperty("Authorize")
  Optional<Address> authorize();

  /**
   * The XRP Ledger {@link Address} of a sender whose preauthorization should be revoked.
   */
  @JsonProperty("Unauthorize")
  Optional<Address> unauthorize();

  /**
   * Validate that either {@link DepositPreAuth#authorize()} or {@link DepositPreAuth#unauthorize()} is present,
   * but not both.
   */
  @Value.Check
  default void validateFieldPresence() {
    Preconditions.checkArgument((authorize().isPresent() || unauthorize().isPresent()) &&
            !(authorize().isPresent() && unauthorize().isPresent()),
        "The DepositPreAuth transaction must include either Authorize or Unauthorize, but not both.");
  }
}
