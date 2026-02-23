package org.xrpl.xrpl4j.model.ledger;

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
 * Marker interface for XRPL asset identifiers (without amounts).
 *
 * <p>An Issue can be one of two types:
 * <ul>
 *   <li>{@link CurrencyIssue} - Represents XRP or an IOU token (identified by currency code and optional issuer)</li>
 *   <li>{@link MptIssue} - Represents an MPToken (identified by mpt_issuance_id)</li>
 * </ul>
 *
 * <p>This interface provides polymorphic helper methods to handle both types in a type-safe manner.
 * Use {@link #map(Function, Function)} to transform an Issue into another type, or
 * {@link #handle(Consumer, Consumer)} to perform side effects based on the Issue type.</p>
 *
 * @see CurrencyIssue
 * @see MptIssue
 * @see "https://xrpl.org/currency-formats.html"
 */
public interface Issue {

  /**
   * Constant {@link Issue} representing XRP.
   */
  Issue XRP = CurrencyIssue.XRP;

  /**
   * Handle this {@link Issue} depending on its actual polymorphic subtype.
   *
   * @param currencyIssueHandler A {@link Consumer} that is called if this instance is of type {@link CurrencyIssue}.
   * @param mptIssueHandler      A {@link Consumer} that is called if this instance is of type {@link MptIssue}.
   */
  default void handle(
    final Consumer<CurrencyIssue> currencyIssueHandler,
    final Consumer<MptIssue> mptIssueHandler
  ) {
    Objects.requireNonNull(currencyIssueHandler);
    Objects.requireNonNull(mptIssueHandler);

    if (CurrencyIssue.class.isAssignableFrom(this.getClass())) {
      currencyIssueHandler.accept((CurrencyIssue) this);
    } else if (MptIssue.class.isAssignableFrom(this.getClass())) {
      mptIssueHandler.accept((MptIssue) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported Issue Type: %s", this.getClass()));
    }
  }

  /**
   * Map this {@link Issue} to an instance of {@link R}, depending on its actual polymorphic subtype.
   *
   * @param currencyIssueMapper A {@link Function} that is called if this instance is of type {@link CurrencyIssue}.
   * @param mptIssueMapper      A {@link Function} that is called if this instance is of type {@link MptIssue}.
   * @param <R>                 The type of object to return after mapping.
   *
   * @return A {@link R} that is constructed by the appropriate mapper function.
   */
  default <R> R map(
    final Function<CurrencyIssue, R> currencyIssueMapper,
    final Function<MptIssue, R> mptIssueMapper
  ) {
    Objects.requireNonNull(currencyIssueMapper);
    Objects.requireNonNull(mptIssueMapper);

    if (CurrencyIssue.class.isAssignableFrom(this.getClass())) {
      return currencyIssueMapper.apply((CurrencyIssue) this);
    } else if (MptIssue.class.isAssignableFrom(this.getClass())) {
      return mptIssueMapper.apply((MptIssue) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported Issue Type: %s", this.getClass()));
    }
  }
}
