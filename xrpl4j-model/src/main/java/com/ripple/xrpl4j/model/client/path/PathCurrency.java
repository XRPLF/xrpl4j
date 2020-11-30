package com.ripple.xrpl4j.model.client.path;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.model.transactions.Address;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Represents a currency that an account holds on the XRPL.
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePathCurrency.class)
@JsonDeserialize(as = ImmutablePathCurrency.class)
public interface PathCurrency {

  static ImmutablePathCurrency.Builder builder() {
    return ImmutablePathCurrency.builder();
  }

  /**
   * Either a 3 characters currency code, or a 40 character hexadecimal encoded currency code value.
   */
  String currency();

  /**
   * The {@link Address} of the issuer of the currency.
   */
  Optional<Address> issuer();

}
