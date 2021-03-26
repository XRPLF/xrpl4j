package org.xrpl.xrpl4j.client.dex.model;

import org.immutables.value.Value;

import java.math.BigDecimal;

/**
 * LimitOrder
 */
@Value.Immutable
public interface LimitOrder {

    static ImmutableLimitOrder.Builder builder() {
        return ImmutableLimitOrder.builder();
    }

    // source=XRP, destination=USD
    Ticker ticker();

    Side side();

    /**
     * Price in the {@link Ticker#counterCurrency()}
     * @return
     */
    BigDecimal counterPrice();

    /**
     * Quantity of the {@link Ticker#baseCurrency()} ()}
     * @return
     */
    BigDecimal baseQuantity();

}