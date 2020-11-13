package com.ripple.xrpl4j.client.model.ledger.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Flags.RippleStateFlags;
import com.ripple.xrpl4j.model.transactions.Hash256;
import com.ripple.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Represents the <a href="https://xrpl.org/ripplestate.html">RippleState XRP Ledger Object</a>.
 *
 * <p>There can only be one {@link RippleStateObject} per currency for any given pair of accounts. Since no account
 * is privileged in the XRP Ledger, a {@link RippleStateObject} sorts account addresses numerically, to ensure
 * a canonical form. Whichever address is numerically lower when decoded is deemed the "low account" and the
 * other is the "high account". The net balance of the trust line is stored from the low account's perspective.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableRippleStateObject.class)
@JsonDeserialize(as = ImmutableRippleStateObject.class)
public interface RippleStateObject extends LedgerObject {

  /**
   * The type of ledger object. In this case, this is always "RippleState".
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.RIPPLE_STATE;
  }

  /**
   * A set of boolean {@link RippleStateFlags} containing options enabled for this object.
   */
  @JsonProperty("Flags")
  RippleStateFlags flags();

  /**
   * The balance of the trust line, from the perspective of the low account. A negative balance indicates that the
   * low account has issued currency to the high account. The issuer in this is always set to the neutral
   * value <a href="https://xrpl.org/accounts.html#special-addresses">ACCOUNT_ONE</a>.
   */
  @JsonProperty("Balance")
  IssuedCurrencyAmount balance();

  /**
   * The limit that the low account has set on the trust line. The issuer is the address of the
   * low account that set this limit.
   */
  @JsonProperty("LowLimit")
  IssuedCurrencyAmount lowLimit();

  /**
   * The limit that the high account has set on the trust line. The issuer is the address of the
   * high account that set this limit.
   */
  @JsonProperty("HighLimit")
  IssuedCurrencyAmount highLimit();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * (Omitted in some historical ledgers) A hint indicating which page of the low account's owner directory
   * links to this object, in case the directory consists of multiple pages.
   */
  @JsonProperty("LowNode")
  Optional<String> lowNode();

  /**
   * (Omitted in some historical ledgers) A hint indicating which page of the high account's owner directory
   * links to this object, in case the directory consists of multiple pages.
   */
  @JsonProperty("HighNode")
  Optional<String> highNode();

  /**
   * The inbound quality set by the low account, as an integer in the implied ratio
   * {@code LowQualityOut:1,000,000,000}. As a special case, the value 0 is equivalent to 1 billion, or face value.
   */
  @JsonProperty("LowQualityIn")
  Optional<UnsignedInteger> lowQualityIn();

  /**
   * The outbound quality set by the low account, as an integer in the implied ratio
   * {@code LowQualityOut:1,000,000,000}. As a special case, the value 0 is equivalent to 1 billion, or face value.
   */
  @JsonProperty("LowQualityOut")
  Optional<UnsignedInteger> lowQualityOut();

  /**
   * The inbound quality set by the high account, as an integer in the implied ratio
   * {@code LowQualityOut:1,000,000,000}. As a special case, the value 0 is equivalent to 1 billion, or face value.
   */
  @JsonProperty("HighQualityIn")
  Optional<UnsignedInteger> highQualityIn();

  /**
   * The outbound quality set by the high account, as an integer in the implied ratio
   * {@code LowQualityOut:1,000,000,000}. As a special case, the value 0 is equivalent to 1 billion, or face value.
   */
  @JsonProperty("HighQualityOut")
  Optional<UnsignedInteger> highQualityOut();

  /**
   * Unique identifier for this {@link RippleStateObject}.
   */
  String index();
}
