package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.DidData;
import org.xrpl.xrpl4j.model.transactions.DidDocument;
import org.xrpl.xrpl4j.model.transactions.DidUri;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.OracleProvider;
import org.xrpl.xrpl4j.model.transactions.OracleUri;
import org.xrpl.xrpl4j.model.transactions.PriceDataWrapper;

import java.util.List;
import java.util.Optional;

/**
 * An Oracle ledger entry holds data associated with a single price oracle object.
 *
 * <p>This interface will be marked {@link Beta} until the featurePriceOracle amendment is enabled on mainnet. Its API
 * is subject to change.</p>
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableOracleObject.class)
@JsonDeserialize(as = ImmutableOracleObject.class)
public interface OracleObject extends LedgerObject {

  /**
   * Construct a {@code OracleObject} builder.
   *
   * @return An {@link ImmutableOracleObject.Builder}.
   */
  static ImmutableOracleObject.Builder builder() {
    return ImmutableOracleObject.builder();
  }

  /**
   * The type of ledger object, which will always be "Oracle" in this case.
   *
   * @return Always returns {@link LedgerEntryType#ORACLE}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.ORACLE;
  }

  /**
   * A bit-map of boolean flags. No flags are defined for {@link OracleObject}, so this value is always 0.
   *
   * @return Always {@link Flags#UNSET}.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags flags() {
    return Flags.UNSET;
  }

  /**
   * The XRPL account with update and delete privileges for the oracle. It's recommended to set up multi-signing on this
   * account.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Owner")
  Address owner();

  /**
   * An arbitrary value that identifies an oracle provider, such as Chainlink, Band, or DIA. This field is a string, up
   * to 256 ASCII hex encoded characters (0x20-0x7E).
   *
   * @return An {@link OracleProvider}.
   */
  @JsonProperty("Provider")
  OracleProvider provider();

  /**
   * An array of up to 10 PriceData objects, each representing the price information for a token pair. More than five
   * PriceData objects require two owner reserves.
   *
   * @return A {@link List} of {@link PriceDataWrapper}.
   */
  @JsonProperty("PriceDataSeries")
  List<PriceDataWrapper> priceDataSeries();

  /**
   * Describes the type of asset, such as "currency", "commodity", or "index". This field is a string, up to 16 ASCII
   * hex encoded characters (0x20-0x7E).
   *
   * @return A {@link String}.
   */
  @JsonProperty("AssetClass")
  String assetClass();

  /**
   * The time the data was last updated, represented in Unix time.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("LastUpdateTime")
  UnsignedInteger lastUpdateTime();

  /**
   * An optional Universal Resource Identifier to reference price data off-chain. This field is limited to 256 bytes.
   *
   * @return An {@link Optional} {@link OracleUri}.
   */
  @JsonProperty("URI")
  Optional<OracleUri> uri();

  /**
   * A hint indicating which page of the sender's owner directory links to this object, in case the directory consists
   * of multiple pages.
   *
   * <p>Note: The object does not contain a direct link to the owner directory containing it, since that value can be
   * derived from the Account.
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
  Hash256 previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link UnsignedInteger} representing the previous transaction ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * The unique ID of the {@link OracleObject}.
   *
   * @return A {@link Hash256} containing the ID.
   */
  Hash256 index();
}
