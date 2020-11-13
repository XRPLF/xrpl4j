package com.ripple.xrpl4j.client.model.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.client.rippled.JsonRpcRequestParams;
import com.ripple.xrpl4j.model.jackson.modules.LedgerIndexSerializer;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.CurrencyAmount;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableRipplePathFindRequestParams.class)
@JsonDeserialize(as = ImmutableRipplePathFindRequestParams.class)
public interface RipplePathFindRequestParams extends JsonRpcRequestParams {

  static ImmutableRipplePathFindRequestParams.Builder builder() {
    return ImmutableRipplePathFindRequestParams.builder();
  }

  @JsonProperty("source_account")
  Address sourceAccount();

  @JsonProperty("destination_account")
  Address destinationAccount();

  @JsonProperty("destination_amount")
  CurrencyAmount destinationAmount();

  @JsonProperty("send_max")
  Optional<CurrencyAmount> sendMax();

  @JsonProperty("source_currencies")
  List<PathCurrency> sourceCurrencies();

  @JsonProperty("ledger_hash")
  Optional<String> ledgerHash();

  @JsonProperty("ledger_index")
  @JsonSerialize(using = LedgerIndexSerializer.class)
  @Value.Default
  default String ledgerIndex() {
    return "current";
  }

}
