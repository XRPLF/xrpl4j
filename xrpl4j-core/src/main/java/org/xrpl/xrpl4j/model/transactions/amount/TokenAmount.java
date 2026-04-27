package org.xrpl.xrpl4j.model.transactions.amount;

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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.xrpl.xrpl4j.model.jackson.modules.TokenAmountDeserializer;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Polymorphic marker interface for XRPL token amounts. Represents an amount of a specific asset — XRP, an issued
 * currency (IOU), or a multi-purpose token (MPT).
 *
 * <p>This type is the successor to {@link CurrencyAmount}. Both coexist during the transition
 * period; {@link CurrencyAmount} is deprecated and will be removed in a future version in favour of this type and its
 * companion {@link Amount} (the scalar numeric value without asset identity).
 *
 * <p>The three concrete subtypes are:
 * <ul>
 *   <li>{@link XrpTokenAmount} — XRP denominated in drops</li>
 *   <li>{@link IouTokenAmount} — an issued currency with currency code and issuer</li>
 *   <li>{@link MptTokenAmount} — a multi-purpose token identified by its issuance ID</li>
 * </ul>
 *
 * <p>Use {@link #handle} or {@link #map} to dispatch on the concrete subtype.
 *
 * @see "https://xrpl.org/basic-data-types.html#specifying-currency-amounts"
 */
@JsonDeserialize(using = TokenAmountDeserializer.class)
public interface TokenAmount {

  /**
   * Handle this {@link TokenAmount} depending on its actual polymorphic subtype.
   *
   * @param xrpTokenAmountHandler A {@link Consumer} that is called if this instance is of type {@link XrpTokenAmount}.
   * @param iouTokenAmountHandler A {@link Consumer} that is called if this instance is of type {@link IouTokenAmount}.
   * @param mptTokenAmountHandler A {@link Consumer} that is called if this instance is of type {@link MptTokenAmount}.
   */
  default void handle(
    final Consumer<XrpTokenAmount> xrpTokenAmountHandler,
    final Consumer<IouTokenAmount> iouTokenAmountHandler,
    final Consumer<MptTokenAmount> mptTokenAmountHandler
  ) {
    Objects.requireNonNull(xrpTokenAmountHandler);
    Objects.requireNonNull(iouTokenAmountHandler);
    Objects.requireNonNull(mptTokenAmountHandler);

    if (XrpTokenAmount.class.isAssignableFrom(this.getClass())) {
      xrpTokenAmountHandler.accept((XrpTokenAmount) this);
    } else if (IouTokenAmount.class.isAssignableFrom(this.getClass())) {
      iouTokenAmountHandler.accept((IouTokenAmount) this);
    } else if (MptTokenAmount.class.isAssignableFrom(this.getClass())) {
      mptTokenAmountHandler.accept((MptTokenAmount) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported TokenAmount type: %s", this.getClass()));
    }
  }

  /**
   * Map this {@link TokenAmount} to an instance of {@link R}, depending on its actual polymorphic subtype.
   *
   * @param xrpTokenAmountMapper A {@link Function} that is called if this instance is of type {@link XrpTokenAmount}.
   * @param iouTokenAmountMapper A {@link Function} that is called if this instance is of type {@link IouTokenAmount}.
   * @param mptTokenAmountMapper A {@link Function} that is called if this instance is of type {@link MptTokenAmount}.
   * @param <R>                  The type of object to return after mapping.
   *
   * @return A {@link R} constructed by the appropriate mapper function.
   */
  default <R> R map(
    final Function<XrpTokenAmount, R> xrpTokenAmountMapper,
    final Function<IouTokenAmount, R> iouTokenAmountMapper,
    final Function<MptTokenAmount, R> mptTokenAmountMapper
  ) {
    Objects.requireNonNull(xrpTokenAmountMapper);
    Objects.requireNonNull(iouTokenAmountMapper);
    Objects.requireNonNull(mptTokenAmountMapper);

    if (XrpTokenAmount.class.isAssignableFrom(this.getClass())) {
      return xrpTokenAmountMapper.apply((XrpTokenAmount) this);
    } else if (IouTokenAmount.class.isAssignableFrom(this.getClass())) {
      return iouTokenAmountMapper.apply((IouTokenAmount) this);
    } else if (MptTokenAmount.class.isAssignableFrom(this.getClass())) {
      return mptTokenAmountMapper.apply((MptTokenAmount) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported TokenAmount type: %s", this.getClass()));
    }
  }
}
