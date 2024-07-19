package org.xrpl.xrpl4j.model.client.oracle;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.OracleLedgerEntryParams;

import java.util.List;
import java.util.Optional;

/**
 * Request parameters for the {@code get_aggregate_price} RPC method.
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableGetAggregatePriceRequestParams.class)
@JsonDeserialize(as = ImmutableGetAggregatePriceRequestParams.class)
public interface GetAggregatePriceRequestParams extends XrplRequestParams {

  /**
   * Construct a {@code GetAggregatePriceRequestParams} builder.
   *
   * @return An {@link ImmutableGetAggregatePriceRequestParams.Builder}.
   */
  static ImmutableGetAggregatePriceRequestParams.Builder builder() {
    return ImmutableGetAggregatePriceRequestParams.builder();
  }

  /**
   * Specifies the ledger version to request. A ledger version can be specified by ledger hash,
   * numerical ledger index, or a shortcut value.
   *
   * @return A {@link LedgerSpecifier} specifying the ledger version to request.
   */
  @JsonUnwrapped
  LedgerSpecifier ledgerSpecifier();

  /**
   * The currency code of the asset to be priced.
   *
   * @return A {@link String}.
   */
  @JsonProperty("base_asset")
  String baseAsset();

  /**
   * The currency code of the asset to quote the price of the base asset.
   *
   * @return A {@link String}.
   */
  @JsonProperty("quote_asset")
  String quoteAsset();

  /**
   * The percentage of outliers to trim. Valid trim range is 1-25. If included, the API returns statistics for the
   * trimmed mean.
   *
   * @return An {@link Optional} {@link UnsignedInteger}.
   */
  Optional<UnsignedInteger> trim();

  /**
   * Defines a time range in seconds for filtering out older price data. Default value is 0, which doesn't filter any
   * data.
   *
   * @return An {@link Optional} {@link UnsignedInteger}.
   */
  @JsonProperty("trim_threshold")
  Optional<UnsignedInteger> trimThreshold();

  /**
   * A list of oracle identifiers.
   *
   * @return A {@link List} of {@link OracleLedgerEntryParams}.
   */
  List<OracleLedgerEntryParams> oracles();

}
