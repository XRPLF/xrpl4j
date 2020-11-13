package com.ripple.xrpl4j.client.model.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.client.model.JsonRpcResult;
import com.ripple.xrpl4j.model.transactions.Address;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableRipplePathFindResult.class)
@JsonDeserialize(as = ImmutableRipplePathFindResult.class)
public interface RipplePathFindResult extends JsonRpcResult {

  List<PathAlternative> alternatives();

  @JsonProperty("destination_account")
  Address destinationAccount();

  @JsonProperty("destination_currencies")
  List<String> destinationCurrencies();

}
