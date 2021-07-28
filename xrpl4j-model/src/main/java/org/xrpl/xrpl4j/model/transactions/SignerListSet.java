package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;

import java.util.List;

/**
 * The SignerListSet transaction creates, replaces, or removes a list of signers that can be used
 * to multi-sign a {@link Transaction}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSignerListSet.class)
@JsonDeserialize(as = ImmutableSignerListSet.class)
public interface SignerListSet extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSignerListSet.Builder}.
   */
  static ImmutableSignerListSet.Builder builder() {
    return ImmutableSignerListSet.builder();
  }

  /**
   * Set of {@link Flags.TransactionFlags}s for this {@link SignerListSet}, which only allows
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
   * A target number for the signer weights. A multi-signature from this list is valid only if the sum weights of
   * the signatures provided is greater than or equal to this value. To delete a signer list, use the value 0.
   *
   * @return An {@link UnsignedInteger} representing the singer quorum.
   */
  @JsonProperty("SignerQuorum")
  UnsignedInteger signerQuorum();

  /**
   * (Omitted when deleting) Array of {@link org.xrpl.xrpl4j.model.ledger.SignerEntry} objects, indicating the
   * addresses and weights of signers in this list. This signer list must have at least 1 member and no more
   * than 8 members. No {@link Address} may appear more than once in the list, nor may the {@link #account()}
   * submitting the transaction appear in the list.
   *
   * @return A {@link List} of {@link SignerEntryWrapper}s.
   */
  @JsonProperty("SignerEntries")
  List<SignerEntryWrapper> signerEntries();

}
