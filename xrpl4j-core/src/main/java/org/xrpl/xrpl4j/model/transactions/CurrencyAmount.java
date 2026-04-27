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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.xrpl.xrpl4j.model.transactions.amount.Amount;
import org.xrpl.xrpl4j.model.transactions.amount.IouAmount;
import org.xrpl.xrpl4j.model.transactions.amount.IouTokenAmount;
import org.xrpl.xrpl4j.model.transactions.amount.MptAmount;
import org.xrpl.xrpl4j.model.transactions.amount.MptTokenAmount;
import org.xrpl.xrpl4j.model.transactions.amount.TokenAmount;
import org.xrpl.xrpl4j.model.transactions.amount.XrpAmount;
import org.xrpl.xrpl4j.model.transactions.amount.XrpTokenAmount;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Marker interface for XRPL Currency Amounts.
 *
 * @see "https://xrpl.org/basic-data-types.html#specifying-currency-amounts"
 * @deprecated Use {@link TokenAmount} instead. {@link CurrencyAmount} and its subtypes ({@link XrpCurrencyAmount},
 *   {@link IssuedCurrencyAmount}, {@link MptCurrencyAmount}) will be removed in a future version. Migrate to
 *   {@link TokenAmount} ({@link XrpTokenAmount}, {@link IouTokenAmount}, {@link MptTokenAmount}) and use {@link Amount}
 *   for scalar numeric values without asset identity.
 */
@Deprecated
public interface CurrencyAmount {

  /**
   * One XRP, in drops.
   *
   * @deprecated Use {@link XrpAmount#ONE_XRP_IN_DROPS} instead.
   */
  @Deprecated
  long ONE_XRP_IN_DROPS = 1_000_000L;

  /**
   * Maximum number of XRP.
   *
   * @deprecated Use {@link XrpAmount#MAX_XRP} instead.
   */
  @Deprecated
  long MAX_XRP = 100_000_000_000L; // <-- per https://xrpl.org/rippleapi-reference.html#value

  /**
   * Maximum number of XRP, in drops.
   *
   * @deprecated Use {@link XrpAmount#MAX_XRP_IN_DROPS} instead.
   */
  @Deprecated
  long MAX_XRP_IN_DROPS = MAX_XRP * ONE_XRP_IN_DROPS;

  /**
   * Maximum number of XRP, as a BigDecimal.
   *
   * @deprecated Use {@link XrpAmount#MAX_XRP_BD} instead.
   */
  @Deprecated
  BigDecimal MAX_XRP_BD = BigDecimal.valueOf(MAX_XRP);

  /**
   * Indicates whether this amount is positive or negative.
   *
   * @return {@code true} if this amount is negative; {@code false} otherwise (i.e., if the value is 0 or positive).
   */
  boolean isNegative();

  /**
   * Handle this {@link CurrencyAmount} depending on its actual polymorphic subtype.
   *
   * @param xrpCurrencyAmountHandler     A {@link Consumer} that is called if this instance is of type
   *                                     {@link XrpCurrencyAmount}.
   * @param issuedCurrencyAmountConsumer A {@link Consumer} that is called if this instance is of type
   *                                     {@link IssuedCurrencyAmount}.
   * @param mptCurrencyAmountConsumer    A {@link Consumer} that is called if this instance is of type
   *                                     {@link MptCurrencyAmount}.
   */
  default void handle(
    final Consumer<XrpCurrencyAmount> xrpCurrencyAmountHandler,
    final Consumer<IssuedCurrencyAmount> issuedCurrencyAmountConsumer,
    final Consumer<MptCurrencyAmount> mptCurrencyAmountConsumer
  ) {
    Objects.requireNonNull(xrpCurrencyAmountHandler);
    Objects.requireNonNull(issuedCurrencyAmountConsumer);
    Objects.requireNonNull(mptCurrencyAmountConsumer);

    if (XrpCurrencyAmount.class.isAssignableFrom(this.getClass())) {
      xrpCurrencyAmountHandler.accept((XrpCurrencyAmount) this);
    } else if (IssuedCurrencyAmount.class.isAssignableFrom(this.getClass())) {
      issuedCurrencyAmountConsumer.accept((IssuedCurrencyAmount) this);
    } else if (MptCurrencyAmount.class.isAssignableFrom(this.getClass())) {
      mptCurrencyAmountConsumer.accept((MptCurrencyAmount) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported CurrencyAmount Type: %s", this.getClass()));
    }
  }

  /**
   * Map this {@link CurrencyAmount} to an instance of {@link R}, depending on its actual polymorphic subtype.
   *
   * @param xrpCurrencyAmountMapper    A {@link Function} that is called if this instance is of type
   *                                   {@link XrpCurrencyAmount}.
   * @param issuedCurrencyAmountMapper A {@link Function} that is called if this instance is  of type
   *                                   {@link IssuedCurrencyAmount}.
   * @param <R>                        The type of object to return after mapping.
   *
   * @return A {@link R} that is constructed by the appropriate mapper function.
   */
  default <R> R map(
    final Function<XrpCurrencyAmount, R> xrpCurrencyAmountMapper,
    final Function<IssuedCurrencyAmount, R> issuedCurrencyAmountMapper,
    final Function<MptCurrencyAmount, R> mptCurrencyAmountMapper
  ) {
    Objects.requireNonNull(xrpCurrencyAmountMapper);
    Objects.requireNonNull(issuedCurrencyAmountMapper);
    Objects.requireNonNull(mptCurrencyAmountMapper);

    if (XrpCurrencyAmount.class.isAssignableFrom(this.getClass())) {
      return xrpCurrencyAmountMapper.apply((XrpCurrencyAmount) this);
    } else if (IssuedCurrencyAmount.class.isAssignableFrom(this.getClass())) {
      return issuedCurrencyAmountMapper.apply((IssuedCurrencyAmount) this);
    } else if (MptCurrencyAmount.class.isAssignableFrom(this.getClass())) {
      return mptCurrencyAmountMapper.apply((MptCurrencyAmount) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported CurrencyAmount Type: %s", this.getClass()));
    }
  }

  /**
   * Returns the scalar numeric {@link Amount} for this currency amount, stripping currency metadata (currency code,
   * issuer, MPT issuance ID). The concrete subtype determines which {@link Amount} implementation is returned:
   * <ul>
   *   <li>{@link XrpCurrencyAmount} → {@link XrpAmount} (drops, sign preserved)</li>
   *   <li>{@link IssuedCurrencyAmount} → {@link IouAmount} (value string passed verbatim)</li>
   *   <li>{@link MptCurrencyAmount} → {@link MptAmount} (magnitude + sign)</li>
   * </ul>
   *
   * @return An {@link Amount} representing the numeric value of this currency amount.
   */
  @JsonIgnore
  default Amount amount() {
    return map(
      xrp -> xrp.isNegative() ? XrpAmount.ofDrops(-xrp.value().longValue())
        : XrpAmount.ofDrops(xrp.value()),
      iou -> IouAmount.of(iou.value()),
      mpt -> MptAmount.of(mpt.unsignedLongValue(), mpt.isNegative())
    );
  }
}
