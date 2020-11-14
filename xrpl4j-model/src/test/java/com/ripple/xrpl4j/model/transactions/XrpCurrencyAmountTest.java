package com.ripple.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit test for {@link XrpCurrencyAmount}.
 */
public class XrpCurrencyAmountTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testEmptyValue() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("XrpCurrencyAmount must be a whole number in drops.");

    XrpCurrencyAmount.of("");
  }

  @Test
  public void testInvalidValue() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("XrpCurrencyAmount must be a whole number in drops.");

    XrpCurrencyAmount.of("0.01");
  }

  @Test
  public void testValidValue() {
    assertThat(XrpCurrencyAmount.of("0").value()).isEqualTo("0");
    assertThat(XrpCurrencyAmount.of("1").value()).isEqualTo("1");
    assertThat(XrpCurrencyAmount.of("2").value()).isEqualTo("2");
    assertThat(XrpCurrencyAmount.of(0).value()).isEqualTo("0");
    assertThat(XrpCurrencyAmount.of(1).value()).isEqualTo("1");
    assertThat(XrpCurrencyAmount.of(2).value()).isEqualTo("2");
  }
}