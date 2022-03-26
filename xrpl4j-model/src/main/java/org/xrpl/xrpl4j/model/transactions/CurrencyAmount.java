package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

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
   *
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
