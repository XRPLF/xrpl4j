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
 * Object mapping for the {@code DIDSet} transaction.
 *
 * <p>This constant will be marked {@link Beta} until the featureDID amendment is enabled on mainnet. Its API
 * is subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableDidSet.class)
@JsonDeserialize(as = ImmutableDidSet.class)
public interface DidSet extends Transaction {

  /**
   * Construct a {@code DidSet} builder.
   *
   * @return An {@link ImmutableDidSet.Builder}.
   */
  static ImmutableDidSet.Builder builder() {
    return ImmutableDidSet.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link DidSet}, which only allows the {@code tfFullyCanonicalSig} flag,
   * which is deprecated.
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

  /**
   * The DID document for this DID. This field should contain a DID Document per W3C standards, however its contents
   * are not checked for validity by the XRPL.
   *
   * @return An optionally-present {@link DidDocument}.
   */
  @JsonProperty("DIDDocument")
  Optional<DidDocument> didDocument();

  /**
   * The Universal Resource Identifier associated with the DID.
   *
   * @return An optionally-present {@link DidUri}.
   */
  @JsonProperty("URI")
  Optional<DidUri> uri();

  /**
   * The public attestations of identity credentials associated with the DID.
   *
   * @return An optionalyl-present {@link DidData}.
   */
  @JsonProperty("Data")
  Optional<DidData> data();

  @JsonProperty(value = "TransactionType")
  @Value.Derived
  @Override
  default TransactionType transactionType() {
    return TransactionType.DID_SET;
  }
}
