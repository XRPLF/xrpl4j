package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

import java.util.Optional;

/**
 * An {@link EnableAmendment} pseudo-transaction marks a change in status of an amendment
 * to the XRP Ledger protocol
 *
 * @see "https://xrpl.org/enableamendment.html"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableEnableAmendment.class)
@JsonDeserialize(as = ImmutableEnableAmendment.class)
public interface EnableAmendment extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableEnableAmendment.Builder}.
   */
  static ImmutableEnableAmendment.Builder builder() {
    return ImmutableEnableAmendment.builder();
  }

  /**
   * A unique identifier for the amendment. This is not intended to be a human-readable name.
   *
   * @return A {@link Hash256} value indentifying an amendment.
   */
  @JsonProperty("Amendment")
  Hash256 amendment();

  /**
   * The ledger index where this pseudo-transaction appears. This distinguishes the
   * pseudo-transaction from other occurrences of the same change.
   *
   * @return A {@link LedgerIndex} to indicates where the tx appears.
   */
  @JsonProperty("LedgerSequence")
  Optional<LedgerIndex> ledgerSequence();
}
