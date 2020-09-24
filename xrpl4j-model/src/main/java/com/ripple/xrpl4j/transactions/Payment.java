package com.ripple.xrpl4j.transactions;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePayment.class)
@JsonDeserialize(as = ImmutablePayment.class)
public interface Payment {

  static ImmutablePayment.Builder builder() {
    return ImmutablePayment.builder();
  }

  CurrencyAmount amount();

  Address destination();

  Optional<UnsignedInteger> destinationTag();

  Optional<Hash256> invoiceId();

  List<List<Path>> paths();

  Optional<CurrencyAmount> sendMax();

  Optional<CurrencyAmount> deliverMin();

}
