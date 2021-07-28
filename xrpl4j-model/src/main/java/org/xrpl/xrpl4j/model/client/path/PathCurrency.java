package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Optional;

/**
 * Represents a currency that an account holds on the XRPL, which can be used to specify the source currencies in
 * {@link RipplePathFindRequestParams}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePathCurrency.class)
@JsonDeserialize(as = ImmutablePathCurrency.class)
public interface PathCurrency {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutablePathCurrency.Builder}.
   */
  static ImmutablePathCurrency.Builder builder() {
    return ImmutablePathCurrency.builder();
  }

  /**
   * Construct a {@link PathCurrency} with the specified currency code and no issuer.
   *
   * @param currency A {@link String} of either a 3 character currency code, or a 40 character hexadecimal encoded
   *                 currency code value.
   *
   * @return A new {@link PathCurrency}.
   */
  static PathCurrency of(String currency) {
    return builder()
      .currency(currency)
      .build();
  }

  /**
   * Either a 3 character currency code, or a 40 character hexadecimal encoded currency code value.
   *
   * @return A {@link String} containing the currency code.
   */
  String currency();

  /**
   * The {@link Address} of the issuer of the currency.
   *
   * @return The optionally-present {@link Address} of the issuer account.
   */
  Optional<Address> issuer();

}
