package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.List;
import java.util.Optional;

/**
 * Request parameters for a ripple_path_find rippled method call.
 *
 * <p>This method is only enabled in the JSON RPC API.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableRipplePathFindRequestParams.class)
@JsonDeserialize(as = ImmutableRipplePathFindRequestParams.class)
public interface RipplePathFindRequestParams extends XrplRequestParams {

  static ImmutableRipplePathFindRequestParams.Builder builder() {
    return ImmutableRipplePathFindRequestParams.builder();
  }

  /**
   * Unique {@link Address} of the account that would send funds in a transaction.
   */
  @JsonProperty("source_account")
  Address sourceAccount();

  /**
   * Unique {@link Address} of the account that would receive funds in a transaction.
   */
  @JsonProperty("destination_account")
  Address destinationAccount();

  /**
   * {@link CurrencyAmount} that the destination account would receive in a transaction.
   *
   * <p>Special case: You can specify "-1" (for XRP) or provide "-1" as the contents of
   * {@link org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount#value()} (for non-XRP currencies).
   * This requests a path to deliver as much as possible, while spending no more than the amount specified in
   * send_max (if provided).
   */
  @JsonProperty("destination_amount")
  CurrencyAmount destinationAmount();

  /**
   * {@link CurrencyAmount} that would be spent in the transaction. Cannot be used with {@link #sourceCurrencies()}.
   */
  @JsonProperty("send_max")
  Optional<CurrencyAmount> sendMax();

  /**
   * A {@link List} of {@link PathCurrency} that the source account might want to spend.
   *
   * <p>Cannot contain more than 18 source currencies.
   */
  @JsonProperty("source_currencies")
  List<PathCurrency> sourceCurrencies();

  /**
   * A 20-byte hex string for the ledger version to use.
   */
  @JsonProperty("ledger_hash")
  Optional<Hash256> ledgerHash();

  /**
   * The ledger index of the ledger to use, or a shortcut string to choose a ledger automatically.
   *
   * <p>Defaults to "current".
   */
  @JsonProperty("ledger_index")
  @Value.Default
  default LedgerIndex ledgerIndex() {
    return LedgerIndex.CURRENT;
  }

}
