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
 * Request parameters for the account_offers rippled method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountOffersRequestParams.class)
@JsonDeserialize(as = ImmutableAccountOffersRequestParams.class)
public interface AccountOffersRequestParams extends XrplRequestParams {

  static ImmutableAccountOffersRequestParams.Builder builder() {
    return ImmutableAccountOffersRequestParams.builder();
  }

  /**
   * A unique identifier for the account, most commonly the account's {@link Address}.
   *
   * @return The {@link Address} for the account.
   */
  Address account();

  /**
   * A 20-byte hex string for the ledger version to use.
   *
   * @return An optionally-present {@link Hash256}.
   */
  @JsonProperty("ledger_hash")
  Optional<Hash256> ledgerHash();

  /**
   * The ledger index of the ledger to use, or a shortcut string to choose a ledger automatically.
   *
   * @return A {@link LedgerIndex}.  Defaults to {@link LedgerIndex#CURRENT}.
   */
  @JsonProperty("ledger_index")
  @Value.Default
  default LedgerIndex ledgerIndex() {
    return LedgerIndex.CURRENT;
  }

  /**
   * A boolean indicating if the {@link #account()} field only accepts a public key or XRP Ledger {@link Address}.
   * Always true, as {@link #account()} is always an {@link Address}.
   *
   * @return {@code true} if the account field only accepts a public key or XRP Ledger address, otherwise {@code false}.
   * Defaults to {@code true}.
   */
  @Value.Derived
  default boolean strict() {
    return true;
  }

  /**
   * Limit the number of transactions to retrieve. Cannot be less than 10 or more than 400.
   * The server is not required to honor this value and the default varies.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  Optional<UnsignedInteger> limit();

  /**
   * Value from a previous paginated response. Resume retrieving data where that response left off.
   *
   * @return An optionally-present {@link String}.
   */
  Optional<Marker> marker();
}
