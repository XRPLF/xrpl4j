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
 * A set of static {@link TransactionFlags} which can be set on
 * {@link org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceSet} transactions.
 */
@SuppressWarnings("abbreviationaswordinname")
public class MpTokenIssuanceSetFlags extends TransactionFlags {

  /**
   * Constant {@link MpTokenIssuanceSetFlags} for the {@code tfMPTLock} flag.
   */
  public static final MpTokenIssuanceSetFlags LOCK = new MpTokenIssuanceSetFlags(0x00000001);
  /**
   * Constant {@link MpTokenIssuanceSetFlags} for the {@code tfMPTUnlock} flag.
   */
  public static final MpTokenIssuanceSetFlags UNLOCK = new MpTokenIssuanceSetFlags(0x00000002);

  private MpTokenIssuanceSetFlags(long value) {
    super(value);
  }

  private MpTokenIssuanceSetFlags() {
  }

  /**
   * Construct {@link MpTokenIssuanceSetFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link MpTokenIssuanceSetFlags}.
   *
   * @return New {@link MpTokenIssuanceSetFlags}.
   */
  public static MpTokenIssuanceSetFlags of(long value) {
    return new MpTokenIssuanceSetFlags(value);
  }

  /**
   * Construct an empty instance of {@link MpTokenIssuanceSetFlags}. Transactions with empty flags will not be
   * serialized with a {@code Flags} field.
   *
   * @return An empty {@link MpTokenIssuanceSetFlags}.
   */
  public static MpTokenIssuanceSetFlags empty() {
    return new MpTokenIssuanceSetFlags();
  }

  /**
   * If set, indicates that all MPT balances for this asset should be locked.
   *
   * @return {@code true} if {@code tfMPTLock} is set, otherwise {@code false}.
   */
  public boolean tfMptLock() {
    return this.isSet(LOCK);
  }

  /**
   * If set, indicates that all MPT balances for this asset should be unlocked.
   *
   * @return {@code true} if {@code tfMPTUnlock} is set, otherwise {@code false}.
   */
  public boolean tfMptUnlock() {
    return this.isSet(UNLOCK);
  }

}
