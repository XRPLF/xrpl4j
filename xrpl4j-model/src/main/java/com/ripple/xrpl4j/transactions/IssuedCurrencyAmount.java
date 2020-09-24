package com.ripple.xrpl4j.transactions;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableIssuedCurrencyAmount.class)
@JsonDeserialize(as = ImmutableIssuedCurrencyAmount.class)
public interface IssuedCurrencyAmount extends CurrencyAmount {

  static ImmutableIssuedCurrencyAmount.Builder builder() {
    return ImmutableIssuedCurrencyAmount.builder();
  }

  String value();

  String currency();

  Address issuer();

}
