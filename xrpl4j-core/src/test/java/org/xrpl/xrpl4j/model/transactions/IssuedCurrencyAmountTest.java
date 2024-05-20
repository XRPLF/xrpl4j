package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link IssuedCurrencyAmount}.
 */
class IssuedCurrencyAmountTest {

  @Test
  void toStringIssuedCurrentAmount() {
    // Negative values.
    IssuedCurrencyAmount issuedCurrency = IssuedCurrencyAmount.builder()
      .currency("USD")
      .issuer(Address.of("rP9JR5JTEqaVYbXHtiqR5YvBeoWQeMBipS"))
      .value("-1.00")
      .build();
    assertThat(issuedCurrency.toString()).isEqualTo(
      "IssuedCurrencyAmount{" +
        "value=-1.00, " +
        "currency=USD, " +
        "issuer=rP9JR5JTEqaVYbXHtiqR5YvBeoWQeMBipS, " +
        "isNegative=true" +
        "}"
    );
    assertThat(issuedCurrency.isNegative()).isTrue();

    // Positive values
    issuedCurrency = IssuedCurrencyAmount.builder()
      .currency("USD")
      .issuer(Address.of("rP9JR5JTEqaVYbXHtiqR5YvBeoWQeMBipS"))
      .value("1.00")
      .build();
    assertThat(issuedCurrency.toString()).isEqualTo(
      "IssuedCurrencyAmount{" +
        "value=1.00, " +
        "currency=USD, " +
        "issuer=rP9JR5JTEqaVYbXHtiqR5YvBeoWQeMBipS, " +
        "isNegative=false" +
        "}"
    );
    assertThat(issuedCurrency.isNegative()).isFalse();

  }

  @Test
  void isNegative() {
  }
}