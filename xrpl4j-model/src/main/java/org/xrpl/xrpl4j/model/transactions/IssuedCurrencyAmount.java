package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.accounts.ImmutableAccountChannelsRequestParams;

/**
 * A {@link CurrencyAmount} for Issued Currencies on the XRP Ledger.
 *
 * @see "https://xrpl.org/rippleapi-reference.html#value"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableIssuedCurrencyAmount.class)
@JsonDeserialize(as = ImmutableIssuedCurrencyAmount.class)
public interface IssuedCurrencyAmount extends CurrencyAmount {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableIssuedCurrencyAmount.Builder}.
   */
  static ImmutableIssuedCurrencyAmount.Builder builder() {
    return ImmutableIssuedCurrencyAmount.builder();
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

  /**
   * Unique account {@link Address} of the entity issuing the currency. In other words, the person or business where the
   * currency can be redeemed.
   *
   * @return The {@link Address} of the account of the issuer of this currency.
   */
  Address issuer();

}
