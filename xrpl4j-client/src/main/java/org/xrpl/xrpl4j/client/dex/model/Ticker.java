package org.xrpl.xrpl4j.client.dex.model;

import org.immutables.value.Value.Immutable;

@Immutable
public interface Ticker {

  static ImmutableTicker.Builder builder() {
    return ImmutableTicker.builder();
  }

  String baseCurrency();

  String counterCurrency();

}
