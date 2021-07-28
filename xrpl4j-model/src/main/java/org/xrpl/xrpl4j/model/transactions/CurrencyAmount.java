package org.xrpl.xrpl4j.model.transactions;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Marker interface for XRPL Currency Amounts.
 *
 * @see "https://xrpl.org/basic-data-types.html#specifying-currency-amounts"
 */
public interface CurrencyAmount {

  /**
   * Handle this {@link CurrencyAmount} depending on its actual polymorphic sub-type.
   *
   * @param xrpCurrencyAmountHandler     A {@link Consumer} that is called if this instance is of type {@link
   *                                     XrpCurrencyAmount}.
   * @param issuedCurrencyAmountConsumer A {@link Consumer} that is called if this instance is of type {@link
   *                                     IssuedCurrencyAmount}.
   */
  default void handle(
    final Consumer<XrpCurrencyAmount> xrpCurrencyAmountHandler,
    final Consumer<IssuedCurrencyAmount> issuedCurrencyAmountConsumer
  ) {
    Objects.requireNonNull(xrpCurrencyAmountHandler);
    Objects.requireNonNull(issuedCurrencyAmountConsumer);

    if (XrpCurrencyAmount.class.isAssignableFrom(this.getClass())) {
      xrpCurrencyAmountHandler.accept((XrpCurrencyAmount) this);
    } else if (IssuedCurrencyAmount.class.isAssignableFrom(this.getClass())) {
      issuedCurrencyAmountConsumer.accept((IssuedCurrencyAmount) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported CurrencyAmount Type: %s", this.getClass()));
    }
  }

  /**
   * Map this {@link CurrencyAmount} to an instance of {@link R}, depending on its actualy polymorphic sub-type.
   *
   * @param xrpCurrencyAmountMapper    A {@link Function} that is called if this instance is of type {@link
   *                                   XrpCurrencyAmount}.
   * @param issuedCurrencyAmountMapper A {@link Function} that is called if this instance is  of type {@link
   *                                   IssuedCurrencyAmount}.
   * @param <R>                        The type of object to return after mapping.
   * @return A {@link R} that is constructed by the appropriate mapper function.
   */
  default <R> R map(
    final Function<XrpCurrencyAmount, R> xrpCurrencyAmountMapper,
    final Function<IssuedCurrencyAmount, R> issuedCurrencyAmountMapper
  ) {
    Objects.requireNonNull(xrpCurrencyAmountMapper);
    Objects.requireNonNull(issuedCurrencyAmountMapper);

    if (XrpCurrencyAmount.class.isAssignableFrom(this.getClass())) {
      return xrpCurrencyAmountMapper.apply((XrpCurrencyAmount) this);
    } else if (IssuedCurrencyAmount.class.isAssignableFrom(this.getClass())) {
      return issuedCurrencyAmountMapper.apply((IssuedCurrencyAmount) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported CurrencyAmount Type: %s", this.getClass()));
    }
  }
}
