package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Represents an individual signer in a {@link SignerListObject}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSignerEntry.class)
@JsonDeserialize(as = ImmutableSignerEntry.class)
public interface SignerEntry {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSignerEntry.Builder}.
   */
  static ImmutableSignerEntry.Builder builder() {
    return ImmutableSignerEntry.builder();
  }

  /**
   * An XRP Ledger classic {@link Address} whose signature contributes to the multi-signature. It does not need to be a
   * funded address in the ledger.
   *
   * @return The {@link Address} of the signer.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * The weight of a signature from this signer. A multi-signature is only valid if the sum weight of the
   * signatures provided meets or exceeds the {@link SignerListObject#signerQuorum()} value.
   *
   * @return An {@link UnsignedInteger} representing the signer weight.
   */
  @JsonProperty("SignerWeight")
  UnsignedInteger signerWeight();

}
