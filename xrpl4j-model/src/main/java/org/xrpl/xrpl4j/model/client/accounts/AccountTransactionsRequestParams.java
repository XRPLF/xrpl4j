package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.util.Optional;

/**
 * Request parameters for the account_tx rippled method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountTransactionsRequestParams.class)
@JsonDeserialize(as = ImmutableAccountTransactionsRequestParams.class)
public interface AccountTransactionsRequestParams extends XrplRequestParams {

  static ImmutableAccountTransactionsRequestParams.Builder builder() {
    return ImmutableAccountTransactionsRequestParams.builder();
  }

  /**
   * A unique {@link Address} for the account.
   *
   * @return The {@link Address} of the account.
   */
  Address account();

  /**
   * The earliest ledger to include transactions from. A value of {@code -1} instructs the server to use the
   * earliest validated ledger version available.
   *
   * @return A {@link LedgerIndex} with a default of empty.
   */
  @JsonProperty("ledger_index_min")
  Optional<LedgerIndex> ledgerIndexMin();

  /**
   * The most recent ledger to include transactions from. A value of {@code -1} instructs the server to use the most
   * recent validated ledger version available.
   *
   * @return A {@link LedgerIndex} with a default of empty.
   */
  @JsonProperty("ledger_index_max")
  Optional<LedgerIndex> ledgerIndexMax();

  /**
   * Return transactions from the ledger with this hash only.
   *
   * @return An optionally-present {@link Hash256} containing the ledger hash.
   */
  @JsonProperty("ledger_hash")
  Optional<Hash256> ledgerHash();

  /**
   * Return transactions from the ledger with this index only.
   *
   * @return A {@link LedgerIndex} containing the ledger index, defaults to "current".
   */
  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

  /**
   * Whether or not to return transactions as JSON or binary-encoded hex strings. Always {@code false}.
   *
   * @return Always {@code false}.
   */
  @Value.Derived
  default boolean binary() {
    return false;
  }

  /**
   * If set to {@code true}, returns values indexed with the oldest ledger first. Otherwise, the results are indexed
   * with the newest ledger first. (Each page of results may not be internally ordered, but the pages are overall
   * ordered.)
   *
   * @return {@code true} if values should be indexed with the oldest ledger first, otherwise {@code false}. Defaults
   * to {@code false}.
   */
  @Value.Default
  default boolean forward() {
    return false;
  }

  /**
   * Limit the number of transactions to retrieve. The server is not required to honor this value.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the number of transactions to return.
   */
  Optional<UnsignedInteger> limit();

  /**
   * Value from a previous paginated response. Resume retrieving data where that response left off.
   * This value is stable even if there is a change in the server's range of available ledgers.
   *
   * @return An optionally-present {@link String} containing the marker.
   */
  Optional<Marker> marker();

}
