package org.xrpl.xrpl4j.client.dex.model;

import org.xrpl.xrpl4j.model.ledger.OfferObject;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OfferObjectToLimitOrderConverter {

  public LimitOrder convert(OfferObject offer, Ticker ticker, Side side) {
    BigDecimal baseQuantity = getBaseQuantity(offer, side);
    BigDecimal counterQuantity = getCounterQuantity(offer, side);
    return LimitOrder.builder()
      .ticker(ticker)
      .baseQuantity(baseQuantity)
      .counterPrice(exchangeRate(baseQuantity, counterQuantity))
      .side(side)
      .build();
  }

  private BigDecimal getCounterQuantity(OfferObject offer, Side side) {
    return side == Side.BUY ? getPrice(offer.takerGets()): getPrice(offer.takerPays());
  }

  private BigDecimal getBaseQuantity(OfferObject offer, Side side) {
    return side == Side.BUY ? getAmount(offer.takerPays()) : getAmount(offer.takerGets());
  }

  private BigDecimal getAmount(CurrencyAmount amount) {
    if (amount instanceof XrpCurrencyAmount) {
      return ((XrpCurrencyAmount) amount).toXrp();
    } else {
      return new BigDecimal(((IssuedCurrencyAmount) amount).value());
    }
  }

  private BigDecimal getPrice(CurrencyAmount amount) {
    if (amount instanceof XrpCurrencyAmount) {
      return ((XrpCurrencyAmount) amount).toXrp();
    } else {
      return new BigDecimal(((IssuedCurrencyAmount) amount).value());
    }
  }

  private BigDecimal exchangeRate(BigDecimal baseQuantity, BigDecimal counterQuantity) {
    return counterQuantity.divide(baseQuantity, 6, RoundingMode.HALF_UP).stripTrailingZeros();
  }

}
