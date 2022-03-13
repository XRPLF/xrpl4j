package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * <p>Similar to, but deliberately different from {@code IssuedCurrencyAmount}.</p>
 *
 * <p>A type for handling balances of an issued currency that may or may not have information available
 * in the object being deserialized as to the owner address or issuer address. The gateway_balances method returns one
 * set of values specifying the issuer but as a string based key to the array of values this type can deserialize to,
 * and another set of values specifying the holder but as a string based key to the array of values this type can
 * deserialize to.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGatewayBalancesIssuedCurrencyAmount.class)
@JsonDeserialize(as = ImmutableGatewayBalancesIssuedCurrencyAmount.class)
public interface GatewayBalancesIssuedCurrencyAmount {

  /**
   * Construct a builder.
   *
   * @return {@link ImmutableGatewayBalancesIssuedCurrencyAmount.Builder}
   */
  static ImmutableGatewayBalancesIssuedCurrencyAmount.Builder builder() {
    return ImmutableGatewayBalancesIssuedCurrencyAmount.builder();
  }

  /**
   * Quoted decimal representation of the amount of currency. This can include scientific notation, such as 1.23e11
   * meaning 123,000,000,000. Both e and E may be used. Note that while this implementation merely holds a {@link
   * String} with no value restrictions, the XRP Ledger does not tolerate unlimited precision values. Instead, non-XRP
   * values (i.e., values held in this object) can have up to 16 decimal digits of precision, with a maximum value of
   * 9999999999999999e80. The smallest positive non-XRP value is 1e-81.
   *
   * @return A {@link String} containing the amount of this issued currency.
   */
  String value();

  /**
   * Arbitrary code for currency to issue. Cannot be XRP.
   *
   * @return A {@link String} containing the currency code.
   */
  String currency();
}
