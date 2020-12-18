package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * A {@link CurrencyAmount} for Issued Currencies on the XRP Ledger.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableIssuedCurrencyAmount.class)
@JsonDeserialize(as = ImmutableIssuedCurrencyAmount.class)
public interface IssuedCurrencyAmount extends CurrencyAmount {

  static ImmutableIssuedCurrencyAmount.Builder builder() {
    return ImmutableIssuedCurrencyAmount.builder();
  }

  /**
   * Quoted decimal representation of the amount of currency. This can include scientific notation, such as
   * 1.23e11 meaning 123,000,000,000. Both e and E may be used.
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
   * Unique account {@link Address} of the entity issuing the currency. In other words, the person or business where
   * the currency can be redeemed.
   *
   * @return The {@link Address} of the account of the issuer of this currency.
   */
  Address issuer();

}
