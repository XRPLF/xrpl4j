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


/**
 * A set of static {@link Flags} which can be set on {@link org.xrpl.xrpl4j.model.ledger.MpTokenObject}s.
 */
public class MpTokenFlags extends Flags {

  /**
   * Constant for an unset flag.
   */
  public static final MpTokenFlags UNSET = new MpTokenFlags(0);

  /**
   * Constant {@link MpTokenFlags} for the {@code lsfMPTLocked} account flag.
   */
  public static final MpTokenFlags LOCKED = new MpTokenFlags(0x00000001);
  /**
   * Constant {@link MpTokenFlags} for the {@code lsfMPTAuthorized} account flag.
   */
  public static final MpTokenFlags AUTHORIZED = new MpTokenFlags(0x00000002);

  /**
   * Required-args Constructor.
   *
   * @param value The long-number encoded flags value of this {@link MpTokenFlags}.
   */
  private MpTokenFlags(final long value) {
    super(value);
  }

  /**
   * Construct {@link MpTokenFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link MpTokenFlags}.
   *
   * @return New {@link MpTokenFlags}.
   */
  public static MpTokenFlags of(long value) {
    return new MpTokenFlags(value);
  }

  /**
   * If set, indicates that all balances are locked.
   *
   * @return {@code true} if {@code lsfMPTLocked} is set, otherwise {@code false}.
   */
  public boolean lsfMptLocked() {
    return this.isSet(MpTokenFlags.LOCKED);
  }

  /**
   * (Only applicable for allow-listing) If set, indicates that the issuer has authorized the holder for the MPT. This
   * flag can be set using a MPTokenAuthorize transaction; it can also be "un-set" using a MPTokenAuthorize transaction
   * specifying the tfMPTUnauthorize flag.
   *
   * @return {@code true} if {@code lsfMPTAuthorized} is set, otherwise {@code false}.
   */
  public boolean lsfMptAuthorized() {
    return this.isSet(MpTokenFlags.AUTHORIZED);
  }

}
