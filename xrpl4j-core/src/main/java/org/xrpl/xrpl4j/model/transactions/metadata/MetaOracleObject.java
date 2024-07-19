package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.OracleProvider;
import org.xrpl.xrpl4j.model.transactions.OracleUri;
import org.xrpl.xrpl4j.model.transactions.PriceDataWrapper;

import java.util.List;
import java.util.Optional;

/**
 * Transaction metadata for Oracle objects.
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableMetaOracleObject.class)
@JsonDeserialize(as = ImmutableMetaOracleObject.class)
public interface MetaOracleObject extends MetaLedgerObject {

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
  Optional<Address> owner();

  /**
   * An arbitrary value that identifies an oracle provider, such as Chainlink, Band, or DIA. This field is a string, up
   * to 256 ASCII hex encoded characters (0x20-0x7E).
   *
   * @return An {@link OracleProvider}.
   */
  @JsonProperty("Provider")
  Optional<OracleProvider> provider();

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
  Optional<String> assetClass();

  /**
   * The time the data was last updated, represented in Unix time.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("LastUpdateTime")
  Optional<UnsignedInteger> lastUpdateTime();

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
  Optional<String> ownerNode();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Optional<Hash256> previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link UnsignedInteger} representing the previous transaction ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  Optional<UnsignedInteger> previousTransactionLedgerSequence();
}
