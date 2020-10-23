package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

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

  @Override
  @JsonProperty("TransactionType")
  @Value.Derived
  default TransactionType transactionType() {
    return TransactionType.ACCOUNT_DELETE;
  }

  /**
   * The {@link Address} of an account to receive any leftover XRP after deleting the sending account.
   * Must be a funded account in the ledger, and must not be the sending account.
   */
  @JsonProperty("Destination")
  Address destination();

  /**
   * Arbitrary destination tag that identifies a hosted recipient or other information for the recipient of
   * the deleted account's leftover XRP.
   */
  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

}
