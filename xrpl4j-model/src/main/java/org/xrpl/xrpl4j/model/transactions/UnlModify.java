package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

/**
 * A {@link UnlModify} pseudo-transaction marks a change to the Negative UNL,
 * indicating that a trusted validator has gone offline or come back online.
 *
 * @see "https://xrpl.org/unlmodify.html"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableUnlModify.class)
@JsonDeserialize(as = ImmutableUnlModify.class)
public interface UnlModify extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableUnlModify.Builder}.
   */
  static ImmutableUnlModify.Builder builder() {
    return ImmutableUnlModify.builder();
  }


  /**
   * The {@link org.xrpl.xrpl4j.model.client.common.LedgerIndex} where this pseudo-transaction appears.
   * This distinguishes the pseudo-transaction from other occurrences of the same change.
   *
   * @return A {@link LedgerIndex} to indicates where the tx appears.
   */
  @JsonProperty("LedgerSequence")
  LedgerIndex ledgerSequence();

  /**
   * If 1, this change represents adding a validator to the Negative UNL. If 0, this change represents
   * removing a validator from the Negative UNL.
   *
   * @return An {@link UnsignedInteger} denoting either 0 or 1.
   */
  @JsonProperty("UNLModifyDisabling")
  UnsignedInteger unlModifyDisabling();

  /**
   * The validator to add or remove, as identified by its master public key.
   *
   * @return An {@link String} denoting master public key of the validator.
   */
  @JsonProperty("UNLModifyValidator")
  String unlModifyValidator();
}
