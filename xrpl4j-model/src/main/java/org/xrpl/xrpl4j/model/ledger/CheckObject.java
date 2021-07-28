package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

/**
 * A Check object describes a check, similar to a paper personal check, which can be cashed by its destination to
 * get money from its sender. (The potential payment has already been approved by its sender, but no money moves
 * until it is cashed. Unlike an Escrow, the money for a Check is not set aside, so cashing the Check could
 * fail due to lack of funds.)
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCheckObject.class)
@JsonDeserialize(as = ImmutableCheckObject.class)
public interface CheckObject extends LedgerObject {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableCheckObject.Builder}.
   */
  static ImmutableCheckObject.Builder builder() {
    return ImmutableCheckObject.builder();
  }

  /**
   * The value 0x0043, mapped to the string "Check", indicates that this object is a {@link CheckObject} object.
   *
   * @return Always {@link org.xrpl.xrpl4j.model.ledger.LedgerObject.LedgerEntryType#CHECK}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.CHECK;
  }

  /**
   * The sender of the {@link CheckObject}. Cashing the {@link CheckObject} debits this address's balance.
   *
   * @return The {@link Address} of the sender.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * An arbitrary tag to further specify the source for this {@link CheckObject}, such as a hosted recipient at the
   * sender's address.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger}.
   */
  @JsonProperty("SourceTag")
  Optional<UnsignedInteger> sourceTag();

  /**
   * The intended recipient of the {@link CheckObject}. Only this address can cash
   * the {@link CheckObject},
   * using a {@link org.xrpl.xrpl4j.model.transactions.CheckCash} transaction.
   *
   * @return The {@link Address} of the destination.
   */
  @JsonProperty("Destination")
  Address destination();

  /**
   * An arbitrary tag to further specify the destination for this {@link CheckObject}, such as a hosted
   * recipient at the destination address.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger}.
   */
  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

  /**
   * A bit-map of boolean flags. No flags are defined for {@link CheckObject}, so this value is always 0.
   *
   * @return Always {@link Flags#UNSET}.
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
   *
   * @return A {@link String} containing the owner node hint.
   */
  @JsonProperty("OwnerNode")
  String ownerNode();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTxnId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return A {@link UnsignedInteger} representing the previous transaction sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * The maximum amount of currency this {@link CheckObject} can debit the {@link CheckObject#account()}.
   * If the {@link CheckObject} is successfully cashed, the {@link CheckObject#destination()} is credited
   * in the same currency for up to this amount.
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("SendMax")
  CurrencyAmount sendMax();

  /**
   * The sequence number of the {@link org.xrpl.xrpl4j.model.transactions.CheckCreate} transaction that created
   * this check.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  /**
   * A hint indicating which page of the {@link CheckObject#destination()}'s owner directory links to this object,
   * in case the directory consists of multiple pages.
   *
   * @return An {@link Optional} of type {@link String}.
   */
  @JsonProperty("DestinationNode")
  Optional<String> destinationNode();

  /**
   * Indicates the time after which this Check is considered expired, in
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger}.
   */
  @JsonProperty("Expiration")
  Optional<UnsignedInteger> expiration();

  /**
   * Arbitrary 256-bit hash provided by the {@link CheckObject#account()} as a specific reason or identifier for
   * this {@link CheckObject}.
   *
   * @return An {@link Optional} of type {@link Hash256}.
   */
  @JsonProperty("InvoiceID")
  Optional<Hash256> invoiceId();

  /**
   * The unique ID of the {@link CheckObject}.
   *
   * @return A {@link Hash256}.
   */
  Hash256 index();
}
