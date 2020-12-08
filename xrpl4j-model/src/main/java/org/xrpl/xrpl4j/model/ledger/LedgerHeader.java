package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.List;

/**
 * Contains the contents of a given ledger on the XRPL.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLedgerHeader.class)
@JsonDeserialize(as = ImmutableLedgerHeader.class)
public interface LedgerHeader {

  /**
   * The ledger index of the ledger.
   */
  @JsonProperty("ledger_index")
  String ledgerIndex();

  /**
   * The SHA-512Half of this ledger version. This serves as a unique identifier for this ledger and all its contents.
   */
  @JsonProperty("ledger_hash")
  Hash256 ledgerHash();

  /**
   * The SHA-512Half of this ledger's state tree information.
   */
  @JsonProperty("account_hash")
  Hash256 accountHash();

  /**
   * The approximate time this ledger version closed, as the number of
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
   * This value is rounded based on the {@link #closeTimeResolution()}}.
   */
  @JsonProperty("close_time")
  UnsignedLong closeTime();

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
   * The total number of drops of XRP owned by accounts in the ledger. This omits XRP that has been
   * destroyed by transaction fees. The actual amount of XRP in circulation is lower because some
   * accounts are "black holes" whose keys are not known by anyone.
   */
  @JsonProperty("total_coins")
  String totalCoins();

  /**
   * The SHA-512Half of the transactions included in this ledger.
   */
  @JsonProperty("transaction_hash")
  Hash256 transactionHash();

  /**
   * Transactions applied in this ledger version.
   */
  List<String> transactions();

  /**
   * An {@link UnsignedInteger} in the range [2,120] indicating the maximum number of seconds by which the
   * {@link #closeTime()} could be rounded.
   */
  @JsonProperty("close_time_resolution")
  UnsignedInteger closeTimeResolution();

}
