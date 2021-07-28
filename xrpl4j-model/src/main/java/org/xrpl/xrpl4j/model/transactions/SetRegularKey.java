package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;

import java.util.Optional;

/**
 * A SetRegularKey transaction assigns, changes, or removes the regular key pair associated with an account.
 *
 * <p>You can protect your account by assigning a regular key pair to it and using it instead of the master key
 * pair to sign transactions whenever possible. If your regular key pair is compromised, but your master key
 * pair is not, you can use a SetRegularKey transaction to regain control of your account.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSetRegularKey.class)
@JsonDeserialize(as = ImmutableSetRegularKey.class)
public interface SetRegularKey extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSetRegularKey.Builder}.
   */
  static ImmutableSetRegularKey.Builder builder() {
    return ImmutableSetRegularKey.builder();
  }

  /**
   * Set of {@link Flags.TransactionFlags}s for this {@link SetRegularKey}, which only allows
   * {@code tfFullyCanonicalSig} flag.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   *
   * @return Always {@link Flags.TransactionFlags} with {@code tfFullyCanonicalSig} set.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags.TransactionFlags flags() {
    return new Flags.TransactionFlags.Builder().tfFullyCanonicalSig(true).build();
  }

  /**
   * An {@link Address} that indicates the regular key pair to be assigned to the account. If omitted,
   * removes any existing regular key pair from the account. Must not match the master key pair for the address.
   *
   * @return The {@link Optional} {@link Address} indicating the regular key pair to use.
   */
  @JsonProperty("RegularKey")
  Optional<Address> regularKey();

}
