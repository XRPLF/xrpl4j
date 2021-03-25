package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableBookOffersRequestParams.class)
@JsonDeserialize(as = ImmutableBookOffersRequestParams.class)
public interface BookOffersRequestParams extends XrplRequestParams {

  static ImmutableBookOffersRequestParams.Builder builder() {
    return ImmutableBookOffersRequestParams.builder();
  }

  @JsonProperty("ledger_hash")
  Optional<String> ledgerHash();

  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

  @JsonProperty("taker")
  Optional<Address> taker();

  @JsonProperty("taker_gets")
  PathCurrency takerGets();

  @JsonProperty("taker_pays")
  PathCurrency takerPays();

}
