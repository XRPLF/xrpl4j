package com.ripple.xrpl4j.model.client.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.model.client.rippled.XrplRequestParams;
import com.ripple.xrpl4j.model.jackson.modules.LedgerIndexSerializer;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.CurrencyAmount;
import com.ripple.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.immutables.value.Value;

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
   * {@link IssuedCurrencyAmount#value()} (for non-XRP currencies). This requests a path to deliver as much as
   * possible, while spending no more than the amount specified in send_max (if provided).
   */
  @JsonProperty("destination_amount")
  CurrencyAmount destinationAmount();

  /**
   * {@link CurrencyAmount} that would be spent in the transaction. Cannot be used with {@link #sourceCurrencies()}.
   */
  @JsonProperty("send_max")
  Optional<CurrencyAmount> sendMax();

  /**
   * A {@link List<PathCurrency>} that the source account might want to spend.
   *
   * <p>Cannot contain more than 18 source currencies.
   */
  @JsonProperty("source_currencies")
  List<PathCurrency> sourceCurrencies();

  /**
   * A 20-byte hex string for the ledger version to use.
   */
  @JsonProperty("ledger_hash")
  Optional<String> ledgerHash();

  /**
   * The ledger index of the ledger to use, or a shortcut string to choose a ledger automatically.
   *
   * <p>Defaults to "current".
   */
  @JsonProperty("ledger_index")
  @JsonSerialize(using = LedgerIndexSerializer.class)
  @Value.Default
  default String ledgerIndex() {
    return "current";
  }

}
