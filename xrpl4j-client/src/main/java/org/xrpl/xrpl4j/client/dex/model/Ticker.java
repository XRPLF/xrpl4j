package org.xrpl.xrpl4j.client.dex.model;

import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
public interface Ticker {

  static Ticker of(String baseCurrency, String counterCurrency) {
    return ImmutableTicker.builder()
      .baseCurrency(baseCurrency)
      .counterCurrency(counterCurrency)
      .build();
  }

  static ImmutableTicker.Builder builder() {
    return ImmutableTicker.builder();
  }

  String baseCurrency();

  String counterCurrency();

  @Value.Check
  default Ticker normalize() {
    if (Helper.isUpperCase(baseCurrency()) && Helper.isUpperCase(counterCurrency())) {
      return this;
    }
    return Ticker.builder().from(this)
      .baseCurrency(this.baseCurrency().toUpperCase())
      .counterCurrency(this.counterCurrency().toUpperCase())
      .build();
  }

  class Helper {
    public static boolean isUpperCase(String value) {
      return value.equals(value.toUpperCase());
    }
  }

}
