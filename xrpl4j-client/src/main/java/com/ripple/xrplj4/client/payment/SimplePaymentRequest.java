package com.ripple.xrplj4.client.payment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.transactions.Address;
import com.ripple.xrpl4j.transactions.XrpCurrencyAmount;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableSimplePaymentRequest.class)
@JsonDeserialize(as = ImmutableSimplePaymentRequest.class)
public interface SimplePaymentRequest {

  static ImmutableSimplePaymentRequest.Builder builder() {
    return ImmutableSimplePaymentRequest.builder();
  }

  XrpCurrencyAmount amount();

  Address sourceAddress();

  Address destinationAddress();

  @Value.Default
  default XrpCurrencyAmount fee() {
    return XrpCurrencyAmount.of("1200");
  }


}
