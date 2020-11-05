package com.ripple.xrplj4.client.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.CheckCash;
import com.ripple.xrpl4j.model.transactions.CheckCreate;
import com.ripple.xrpl4j.model.transactions.CurrencyAmount;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.Hash256;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * A Check object describes a check, similar to a paper personal check, which can be cashed by its destination to
 * get money from its sender. (The potential payment has already been approved by its sender, but no money moves
 * until it is cashed. Unlike an {@link Escrow}, the money for a Check is not set aside, so cashing the Check could
 * fail due to lack of funds.)
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCheck.class)
@JsonDeserialize(as = ImmutableCheck.class)
public interface Check extends LedgerObject {

  static ImmutableCheck.Builder builder() {
    return ImmutableCheck.builder();
  }

  /**
   * The value 0x0043, mapped to the string "Check", indicates that this object is a {@link Check} object.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default String ledgerEntryType() {
    return "Check";
  }

  /**
   * The sender of the {@link Check}. Cashing the {@link Check} debits this address's balance.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * An arbitrary tag to further specify the source for this {@link Check}, such as a hosted recipient at the
   * sender's address.
   */
  @JsonProperty("SourceTag")
  Optional<UnsignedInteger> sourceTag();

  /**
   * The intended recipient of the {@link Check}. Only this address can cash the {@link Check},
   * using a {@link CheckCash} transaction.
   */
  @JsonProperty("Destination")
  Address destination();

  /**
   * An arbitrary tag to further specify the destination for this {@link Check}, such as a hosted
   * recipient at the destination address.
   */
  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

  /**
   * A bit-map of boolean flags. No flags are defined for {@link Check}, so this value is always 0.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags flags() {
    return Flags.UNSET;
  }

  /**
   * A hint indicating which page of the sender's owner directory links to this object, in case the directory
   * consists of multiple pages.
   * Note: The object does not contain a direct link to the owner directory containing it,
   * since that value can be derived from the Account.
   */
  @JsonProperty("OwnerNode")
  String ownerNode();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTxnId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * The maximum amount of currency this {@link Check} can debit the {@link Check#account()}. If the {@link Check}
   * is successfully cashed, the {@link Check#destination()} is credited in the same currency for up to this amount.
   */
  @JsonProperty("SendMax")
  CurrencyAmount sendMax();

  /**
   * The sequence number of the {@link CheckCreate} transaction that created this check.
   */
  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  /**
   * A hint indicating which page of the {@link Check#destination()}'s owner directory links to this object,
   * in case the directory consists of multiple pages.
   */
  @JsonProperty("DestinationNode")
  Optional<String> destinationNode();

  /**
   * Indicates the time after which this Check is considered expired, in
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
   */
  @JsonProperty("Expiration")
  Optional<UnsignedInteger> expiration();

  /**
   * Arbitrary 256-bit hash provided by the {@link Check#account()} as a specific reason or identifier for
   * this {@link Check}.
   */
  @JsonProperty("InvoiceID")
  Optional<Hash256> invoiceId();

  /**
   * The unique ID of the {@link Check}.
   */
  Hash256 index();
}
