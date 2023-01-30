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

import org.xrpl.xrpl4j.model.ledger.NfTokenOfferObject;

/**
 * A set of static {@link Flags} which can be set on {@link NfTokenOfferObject}s.
 */
public class NfTokenOfferFlags extends Flags {

  /**
   * Constant {@link NfTokenOfferFlags} for the {@code lsfBuyToken} flag.
   */
  public static final NfTokenOfferFlags BUY_TOKEN = new NfTokenOfferFlags(0x00000001);

  /**
   * Constant {@link NfTokenOfferFlags} for the {@code lsfAuthorized} flag.
   */
  public static final NfTokenOfferFlags AUTHORIZED = new NfTokenOfferFlags(0x00000002);


  private NfTokenOfferFlags(long value) {
    super(value);
  }

  /**
   * Construct {@link NfTokenOfferFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link NfTokenOfferFlags}.
   *
   * @return New {@link NfTokenOfferFlags}.
   */
  public static NfTokenOfferFlags of(long value) {
    return new NfTokenOfferFlags(value);
  }

  /**
   * Indicates the offer is a buy offer.
   *
   * @return {@code true} if {@code lsfBuyToken} is set, otherwise {@code false}.
   */
  public boolean lsfBuyToken() {
    return this.isSet(BUY_TOKEN);
  }

  /**
   * Indicates the offer has been approved by the issuer.
   *
   * @return {@code true} if {@code lsfAuthorized} is set, otherwise {@code false}.
   */
  public boolean lsfAuthorized() {
    return this.isSet(AUTHORIZED);
  }
}
