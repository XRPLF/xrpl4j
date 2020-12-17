package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Contains the contents of a given ledger on the XRPL.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLedgerHeader.class)
@JsonDeserialize(as = ImmutableLedgerHeader.class)
public interface LedgerHeader {

  static ImmutableLedgerHeader.Builder builder() {
    return ImmutableLedgerHeader.builder();
  }

  /**
   * The ledger index of the ledger. In other objects, this would be a
   * {@link org.xrpl.xrpl4j.model.client.common.LedgerIndex}, however the ledger
   * method returns the ledger_index as a {@link String} representing an unsigned 32 bit integer.
   */
  @JsonProperty("ledger_index")
  String ledgerIndex();

  /**
   * The SHA-512Half of this ledger version. This serves as a unique identifier for this ledger and all its contents.
   */
  @JsonProperty("ledger_hash")
  Optional<Hash256> ledgerHash();

  /**
   * The SHA-512Half of this ledger's state tree information.
   */
  @JsonProperty("account_hash")
  Optional<Hash256> accountHash();

  /**
   * The approximate time this ledger version closed, as the number of
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
   * This value is rounded based on the {@link #closeTimeResolution()}}.
   */
  @JsonProperty("close_time")
  Optional<UnsignedLong> closeTime();

  /**
   * The time this ledger was closed, in human-readable format. Always uses the UTC time zone.
   */
  @JsonProperty("close_time_human")
  @JsonFormat(pattern = "yyyy-MMM-dd HH:mm:ss.SSSSSSSSS z")
  ZonedDateTime closeTimeHuman();

  /**
   * If true, this ledger version is no longer accepting new transactions. (However, unless this ledger
   * version is validated, it might be replaced by a different ledger version with a different set of transactions.)
   */
  @Value.Default
  default boolean closed() {
    return false;
  }

  /**
   * The {@link #ledgerHash()} value of the previous ledger version that is the direct predecessor of this one.
   * If there are different versions of the previous ledger index, this indicates from which one the ledger was derived.
   */
  @JsonProperty("parent_hash")
  Hash256 parentHash();

  /**
   * The time at which the previous ledger was closed.
   */
  @JsonProperty("parent_close_time")
  UnsignedLong parentCloseTime();

  /**
   * The total number of drops of XRP owned by accounts in the ledger. This omits XRP that has been
   * destroyed by transaction fees. The actual amount of XRP in circulation is lower because some
   * accounts are "black holes" whose keys are not known by anyone.
   */
  @JsonProperty("total_coins")
  Optional<XrpCurrencyAmount> totalCoins();

  /**
   * The SHA-512Half of the transactions included in this ledger.
   */
  @JsonProperty("transaction_hash")
  Optional<Hash256> transactionHash();

  /**
   * Transactions applied in this ledger version.
   */
  List<TransactionResult<? extends Transaction>> transactions();

  /**
   * An {@link UnsignedInteger} in the range [2,120] indicating the maximum number of seconds by which the
   * {@link #closeTime()} could be rounded.
   */
  @JsonProperty("close_time_resolution")
  Optional<UnsignedInteger> closeTimeResolution();

}
