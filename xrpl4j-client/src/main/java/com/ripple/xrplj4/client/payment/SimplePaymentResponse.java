package com.ripple.xrplj4.client.payment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

import java.util.Optional;

@Immutable
@JsonSerialize(as = ImmutableSimplePaymentResponse.class)
@JsonDeserialize(as = ImmutableSimplePaymentResponse.class)
public interface SimplePaymentResponse {

  static ImmutableSimplePaymentResponse.Builder builder() {
    return ImmutableSimplePaymentResponse.builder();
  }

  Optional<String> engineResult();

  Optional<String> transactionHash();

  Optional<String> error();

}
