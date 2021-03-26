package org.xrpl.xrpl4j.client.dex.model;

import org.xrpl.xrpl4j.model.ledger.OfferObject;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;

public class OfferObjectToLimitOrderConverter {

  public LimitOrder convert(OfferObject offer, Ticker ticker, Side side) {
    return LimitOrder.builder()
      .ticker(ticker)
      .baseQuantity(side == Side.BUY ? getAmount(offer.takerGets()) : getAmount(offer.takerPays()))
      .counterPrice(side == Side.BUY ? getPrice(offer.takerPays()): getPrice(offer.takerGets()))
      .side(side)
      .build();
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



}
