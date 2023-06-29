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

import org.xrpl.xrpl4j.model.transactions.AccountSet;

/**
 * A set of static {@link Flags} which can be set on {@link AccountSet} transactions.
 */
public class NfTokenFlags extends Flags {

  /**
   * Constant for an unset flag.
   */
  public static final NfTokenFlags UNSET = new NfTokenFlags(0);

  /**
   * Constant {@link NfTokenFlags} for the {@code lsfBurnable} account flag.
   */
  public static final NfTokenFlags BURNABLE = new NfTokenFlags(0x0001);

  /**
   * Constant {@link NfTokenFlags} for the {@code lsfOnlyXrp} account flag.
   */
  public static final NfTokenFlags ONLY_XRP = new NfTokenFlags(0x0002);

  /**
   * Constant {@link NfTokenFlags} for the {@code lsfTrustLine} account flag.
   */
  public static final NfTokenFlags TRUST_LINE = new NfTokenFlags(0x0004);

  /**
   * Constant {@link NfTokenFlags} for the {@code lsfTransferable} account flag.
   */
  public static final NfTokenFlags TRANSFERABLE = new NfTokenFlags(0x0008);

  /**
   * Required-args Constructor.
   *
   * @param value The long-number encoded flags value of this {@link NfTokenFlags}.
   */
  private NfTokenFlags(final long value) {
    super(value);
  }

  /**
   * Construct {@link NfTokenFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link NfTokenFlags}.
   * @return New {@link NfTokenFlags}.
   */
  public static NfTokenFlags of(long value) {
    return new NfTokenFlags(value);
  }

  /**
   * If enabled, the issuer (or an entity authorized by the issuer) can destroy this NFToken. The object's owner can
   * always do so.
   *
   * @return {@code true} if {@code lsfBurnable} is set, otherwise {@code false}.
   */
  public boolean lsfBurnable() {
    return this.isSet(NfTokenFlags.BURNABLE);
  }

  /**
   * If enabled, this NFToken can only be offered or sold for XRP.
   *
   * @return {@code true} if {@code lsfOnlyXrp} is set, otherwise {@code false}.
   */
  public boolean lsfOnlyXrp() {
    return this.isSet(NfTokenFlags.ONLY_XRP);
  }

  /**
   * If enabled, automatically create trust lines to hold transfer fees. Otherwise, buying or selling this NFToken for
   * a fungible token amount fails if the issuer does not have a trust line for that token. The
   * fixRemoveNFTokenAutoTrustLine amendment makes it invalid to enable this flag.
   *
   * @return {@code true} if {@code lsfTrustLine} is set, otherwise {@code false}.
   */
  public boolean lsfTrustLine() {
    return this.isSet(NfTokenFlags.TRUST_LINE);
  }

  /**
   * If enabled, this NFToken can be transferred from one holder to another. Otherwise, it can only be transferred to
   * or from the issuer.
   *
   * @return {@code true} if {@code lsfTransferable} is set, otherwise {@code false}.
   */
  public boolean lsfTransferable() {
    return this.isSet(NfTokenFlags.TRANSFERABLE);
  }

}
