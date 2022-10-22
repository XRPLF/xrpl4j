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

  Address ACCOUNT_ZERO = Address.of("rrrrrrrrrrrrrrrrrrrrrhoLvTp");

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableUnlModify.Builder}.
   */
  static ImmutableUnlModify.Builder builder() {
    return ImmutableUnlModify.builder();
  }


  /**
   * This field is overridden in this class because of a bug in rippled that causes this field to be missing
   * in API responses. In other pseudo-transactions such as {@link SetFee} and {@link EnableAmendment}, the rippled
   * API sets the {@code account} field to a special XRPL address called ACCOUNT_ZERO, which is the base58
   * encoding of the number zero. Because rippled does not set the {@code account} field of the {@link UnlModify}
   * pseudo-transaction, this override will always set the field to ACCOUNT_ZERO to avoid deserialization issues
   * and to be consistent with other pseudo-transactions.
   *
   * @return Always returns ACCOUNT_ZERO, which is the base58 encoding of the number zero.
   */
  @Override
  @JsonProperty("Account")
  @Value.Derived
  default Address account() {
    return ACCOUNT_ZERO;
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
