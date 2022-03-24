package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class IssuedCurrencyAmountTest {

  public static final Address ISSUER = Address.of("rU6K7V3Po4snVhBBaU29sesqs2qTQJWDw1");
  public static final String USD = "USD";

  @Test
  void constructByStringValue() {
    IssuedCurrencyAmount amount = IssuedCurrencyAmount.builder()
      .issuer(ISSUER)
      .currency(USD)
      .value("100")
      .build();

    assertThat(amount.value()).isEqualTo("100");
    assertThat(amount.bigDecimalValue()).isEqualTo(BigDecimal.valueOf(100));

    IssuedCurrencyAmount reallyBigAmount = IssuedCurrencyAmount.builder()
      .issuer(ISSUER)
      .currency(USD)
      .value("9999999999999999e80")
      .build();

    assertThat(reallyBigAmount.value()).isEqualTo("9999999999999999e80");
    assertThat(reallyBigAmount.bigDecimalValue()).isEqualTo(BigDecimal.valueOf(9999999999999999e80));
  }

  @Test
  void constructByBigDecimal() {
    IssuedCurrencyAmount amount = IssuedCurrencyAmount.builder()
      .issuer(ISSUER)
      .currency(USD)
      .bigDecimalValue(BigDecimal.valueOf(100))
      .build();

    assertThat(amount.value()).isEqualTo("100");
    assertThat(amount.bigDecimalValue()).isEqualTo(BigDecimal.valueOf(100));

    IssuedCurrencyAmount reallyBigAmount = IssuedCurrencyAmount.builder()
      .issuer(ISSUER)
      .currency(USD)
      .bigDecimalValue(IssuedCurrencyAmount.MAX_VALUE)
      .build();

    assertThat(reallyBigAmount.value()).isEqualTo("9999999999999999e80");
    assertThat(reallyBigAmount.bigDecimalValue()).isEqualTo(IssuedCurrencyAmount.MAX_VALUE);
  }
}
