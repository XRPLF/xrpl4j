package org.xrpl.xrpl4j.model.flags;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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

import org.xrpl.xrpl4j.model.ledger.OfferObject;

/**
 * A set of static {@link Flags} which can be set on {@link OfferObject}s.
 */
public class OfferFlags extends Flags {

  /**
   * Constant {@link OfferFlags} for the {@code lsfPassive} flag.
   */
  protected static final OfferFlags PASSIVE = new OfferFlags(0x00010000);

  /**
   * Constant {@link OfferFlags} for the {@code lsfSell} flag.
   */
  protected static final OfferFlags SELL = new OfferFlags(0x00020000);

  /**
   * Constant {@link OfferFlags} for the {@code lsfHybrid} flag.
   */
  protected static final OfferFlags HYBRID = new OfferFlags(0x00040000);

  private OfferFlags(long value) {
    super(value);
  }

  /**
   * Construct {@link OfferFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link OfferFlags}.
   *
   * @return New {@link OfferFlags}.
   */
  public static OfferFlags of(long value) {
    return new OfferFlags(value);
  }

  /**
   * The object was placed as a passive offer. This has no effect on the object in the ledger.
   *
   * @return {@code true} if {@code lsfPassive} is set, otherwise {@code false}.
   */
  public boolean lsfPassive() {
    return this.isSet(PASSIVE);
  }

  /**
   * The object was placed as a sell offer. This has no effect on the object in the ledger (because tfSell only
   * matters if you get a better rate than you asked for, which cannot happen after the object enters the ledger).
   *
   * @return {@code true} if {@code lsfSell} is set, otherwise {@code false}.
   */
  public boolean lsfSell() {
    return this.isSet(SELL);
  }

  /**
   * Indicates the offer is hybrid. (meaning it is part of both a domain and open order book).
   *
   * @return {@code true} if {@code lsfHybrid} is set, otherwise {@code false}.
   */
  public boolean lsfHybrid() {
    return this.isSet(HYBRID);
  }
}
