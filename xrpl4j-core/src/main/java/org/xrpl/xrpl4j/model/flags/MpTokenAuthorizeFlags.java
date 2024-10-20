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
 * {@link org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize} transactions.
 */
@SuppressWarnings("abbreviationaswordinname")
public class MpTokenAuthorizeFlags extends TransactionFlags {

  /**
   * Constant {@link MpTokenAuthorizeFlags} for the {@code tfMPTLock} flag.
   */
  public static final MpTokenAuthorizeFlags UNAUTHORIZE = new MpTokenAuthorizeFlags(0x00000001);

  private MpTokenAuthorizeFlags(long value) {
    super(value);
  }

  private MpTokenAuthorizeFlags() {
  }

  /**
   * Construct {@link MpTokenAuthorizeFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link MpTokenAuthorizeFlags}.
   *
   * @return New {@link MpTokenAuthorizeFlags}.
   */
  public static MpTokenAuthorizeFlags of(long value) {
    return new MpTokenAuthorizeFlags(value);
  }

  /**
   * Construct an empty instance of {@link MpTokenAuthorizeFlags}. Transactions with empty flags will not be serialized
   * with a {@code Flags} field.
   *
   * @return An empty {@link MpTokenAuthorizeFlags}.
   */
  public static MpTokenAuthorizeFlags empty() {
    return new MpTokenAuthorizeFlags();
  }

  /**
   * If set and transaction is submitted by a holder, it indicates that the holder no longer wants to hold the MPToken,
   * which will be deleted as a result. If the the holder's MPToken has non-zero balance while trying to set this flag,
   * the transaction will fail. On the other hand, if set and transaction is submitted by an issuer, it would mean that
   * the issuer wants to unauthorize the holder (only applicable for allow-listing), which would unset the
   * lsfMPTAuthorized flag on the MPToken.
   *
   * @return {@code true} if {@code tfMPTUnauthorize} is set, otherwise {@code false}.
   */
  public boolean tfMptUnauthorize() {
    return this.isSet(UNAUTHORIZE);
  }

}
