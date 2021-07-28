package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CurrencyAmount}.
 */
public class CurrencyAmountTest {

  @Test
  public void handleXrp() {
    XrpCurrencyAmount xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(0L);

    xrpCurrencyAmount.handle(
      ($) -> assertThat($.value()).isEqualTo(UnsignedLong.ZERO),
      ($) -> fail()
    );

    // null xrpCurrencyAmountHandler
    assertThrows(NullPointerException.class, () ->
      xrpCurrencyAmount.handle(null, ($) -> new Object())
    );

    // null issuedCurrencyAmountConsumer
    assertThrows(NullPointerException.class, () ->
      xrpCurrencyAmount.handle(($) -> new Object(), null)
    );

    // Unhandled...
    CurrencyAmount currencyAmount = new CurrencyAmount() {
    };
    assertThrows(IllegalStateException.class, () ->
      currencyAmount.handle(($) -> new Object(), ($) -> new Object())
    );
  }


  @Test
  public void handleIssuance() {
    final IssuedCurrencyAmount issuedCurrencyAmount = IssuedCurrencyAmount.builder()
      .issuer(Address.of("foo"))
      .currency("USD")
      .value("100")
      .build();

    issuedCurrencyAmount.handle(
      ($) -> fail(),
      ($) -> assertThat($.value()).isEqualTo("100")
    );

    // null xrpCurrencyAmountHandler
    assertThrows(NullPointerException.class, () ->
      issuedCurrencyAmount.handle(null, ($) -> new Object())
    );
    // null issuedCurrencyAmountConsumer
    assertThrows(NullPointerException.class, () ->
      issuedCurrencyAmount.handle(($) -> new Object(), null)
    );
  }

  @Test
  public void mapXrp() {
    XrpCurrencyAmount xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(0L);

    String actual = xrpCurrencyAmount.map(
      ($) -> "success",
      ($) -> "fail"
    );
    assertThat(actual).isEqualTo("success");

    // null xrpCurrencyAmountHandler
    assertThrows(NullPointerException.class, () ->
      xrpCurrencyAmount.map(null, ($) -> new Object())
    );
    // null issuedCurrencyAmountConsumer
    assertThrows(NullPointerException.class, () ->
      xrpCurrencyAmount.map(($) -> new Object(), null)
    );

    // Unhandled...
    CurrencyAmount currencyAmount = new CurrencyAmount() {
    };
    assertThrows(IllegalStateException.class, () ->
      currencyAmount.map(($) -> new Object(), ($) -> new Object())
    );
  }

  @Test
  public void mapIssuance() {
    final IssuedCurrencyAmount issuedCurrencyAmount = IssuedCurrencyAmount.builder()
      .issuer(Address.of("foo"))
      .currency("USD")
      .value("100")
      .build();

    String actual = issuedCurrencyAmount.map(
      ($) -> "fail",
      ($) -> "success"
    );
    assertThat(actual).isEqualTo("success");

    // null xrpCurrencyAmountHandler
    assertThrows(NullPointerException.class, () ->
      issuedCurrencyAmount.map(null, ($) -> new Object())
    );
    // null issuedCurrencyAmountConsumer
    assertThrows(NullPointerException.class, () ->
      issuedCurrencyAmount.map(($) -> new Object(), null)
    );
  }
}
