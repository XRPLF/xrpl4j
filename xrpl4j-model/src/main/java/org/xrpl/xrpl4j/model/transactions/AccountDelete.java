package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.flags.Flags.TransactionFlags;

import java.util.Optional;

/**
 * An {@link AccountDelete} transaction deletes an account and any objects it owns in the XRP Ledger, if possible,
 * sending the account's remaining XRP to a specified destination account.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountDelete.class)
@JsonDeserialize(as = ImmutableAccountDelete.class)
public interface AccountDelete extends Transaction {

  static ImmutableAccountDelete.Builder builder() {
    return ImmutableAccountDelete.builder();
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
  default TransactionFlags flags() {
    return new TransactionFlags.Builder().tfFullyCanonicalSig(true).build();
  }

  /**
   * The {@link Address} of an account to receive any leftover XRP after deleting the sending account. Must be a funded
   * account in the ledger, and must not be the sending account.
   *
   * @return The {@link Address} of the leftover XRP destination account.
   */
  @JsonProperty("Destination")
  Address destination();

  /**
   * Arbitrary destination tag that identifies a hosted recipient or other information for the recipient of the deleted
   * account's leftover XRP.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the tag of the destination account.
   */
  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

}
