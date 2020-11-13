package com.ripple.xrpl4j.model.transactions;

import com.google.common.primitives.UnsignedLong;

import java.math.BigDecimal;

/**
 * Empty interface for XRPL Currency Amounts.
 *
 * @see "https://xrpl.org/basic-data-types.html#specifying-currency-amounts"
 */
public interface CurrencyAmount {

  /**
   * Constructs an {@link XrpCurrencyAmount} using a number of drops.
   * @param drops
   * @return
   */
  static XrpCurrencyAmount ofDrops(long drops) {
    return ofDrops(UnsignedLong.valueOf(drops));
  }

  /**
   * Constructs an {@link XrpCurrencyAmount} using a number of drops.
   * @param drops
   * @return
   */
  static XrpCurrencyAmount ofDrops(UnsignedLong drops) {
    return XrpCurrencyAmount.of(drops.toString());
  }

  /**
   * Constructs an {@link XrpCurrencyAmount} using decimal amount of XRP.
   * @param amount
   * @return
   */
  static XrpCurrencyAmount ofXrp(BigDecimal amount) {
    return XrpCurrencyAmount.of(amount.scaleByPowerOfTen(6).toBigIntegerExact().toString());
  }


}
