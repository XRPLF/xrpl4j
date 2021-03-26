package org.xrpl.xrpl4j.client.dex.model;

import org.immutables.value.Value.Immutable;

import java.util.List;

@Immutable
public interface OrderBook {

  static ImmutableOrderBook.Builder builder() {
    return ImmutableOrderBook.builder();
  }

  Ticker ticker();

  List<LimitOrder> bids();

  List<LimitOrder> asks();

}
