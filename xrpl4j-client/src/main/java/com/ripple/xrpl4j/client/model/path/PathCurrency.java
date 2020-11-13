package com.ripple.xrpl4j.client.model.path;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.model.transactions.Address;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePathCurrency.class)
@JsonDeserialize(as = ImmutablePathCurrency.class)
public interface PathCurrency {

  static ImmutablePathCurrency.Builder builder() {
    return ImmutablePathCurrency.builder();
  }

  String currency();

  Optional<Address> issuer();

}
