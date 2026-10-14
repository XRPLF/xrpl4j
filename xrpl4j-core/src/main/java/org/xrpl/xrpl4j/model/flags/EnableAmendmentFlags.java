package org.xrpl.xrpl4j.model.flags;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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

import org.xrpl.xrpl4j.model.transactions.EnableAmendment;

/**
 * A set of static {@link TransactionFlags} which can be present on {@link EnableAmendment} pseudo-transactions.
 *
 * <p>xrpld emits an {@link EnableAmendment} at each of three amendment status transitions and uses these flags to
 * identify which one occurred: {@link #tfGotMajority()} and {@link #tfLostMajority()} mark the start and cancellation
 * of the activation countdown, while the <em>absence</em> of both marks the amendment becoming enabled (see
 * {@link #isEnabled()}). Because these pseudo-transactions are emitted by the ledger and never
 * submitted by users, this class offers no builder; instances are either {@link #empty()} or constructed via
 * {@link #of(long)} during deserialization.</p>
 *
 * @see "https://xrpl.org/enableamendment.html#enableamendment-flags"
 */
public class EnableAmendmentFlags extends TransactionFlags {

  /**
   * Constant {@link EnableAmendmentFlags} for the {@code tfGotMajority} flag, indicating that the amendment gained
   * support from more than 80% of trusted validators.
   */
  public static final EnableAmendmentFlags GOT_MAJORITY = new EnableAmendmentFlags(0x00010000L);

  /**
   * Constant {@link EnableAmendmentFlags} for the {@code tfLostMajority} flag, indicating that the amendment lost
   * support from more than 80% of trusted validators.
   */
  public static final EnableAmendmentFlags LOST_MAJORITY = new EnableAmendmentFlags(0x00020000L);

  private EnableAmendmentFlags(long value) {
    super(value);
  }

  private EnableAmendmentFlags() {
  }

  /**
   * Construct {@link EnableAmendmentFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link EnableAmendmentFlags}.
   *
   * @return New {@link EnableAmendmentFlags}.
   */
  public static EnableAmendmentFlags of(long value) {
    return new EnableAmendmentFlags(value);
  }

  /**
   * Construct an empty instance of {@link EnableAmendmentFlags}. Transactions with empty flags will not be serialized
   * with a {@code Flags} field.
   *
   * @return An empty {@link EnableAmendmentFlags}.
   */
  public static EnableAmendmentFlags empty() {
    return new EnableAmendmentFlags();
  }

  /**
   * Whether the {@code tfGotMajority} flag is set.
   *
   * @return {@code true} if {@code tfGotMajority} is set, otherwise {@code false}.
   */
  public boolean tfGotMajority() {
    return this.isSet(GOT_MAJORITY);
  }

  /**
   * Whether the {@code tfLostMajority} flag is set.
   *
   * @return {@code true} if {@code tfLostMajority} is set, otherwise {@code false}.
   */
  public boolean tfLostMajority() {
    return this.isSet(LOST_MAJORITY);
  }

  /**
   * Whether these flags indicate that the amendment became enabled on the ledger, as opposed to merely gaining or
   * losing validator majority. Per xrpld, an {@link EnableAmendment} signals "enabled" by carrying <em>neither</em>
   * {@code tfGotMajority} nor {@code tfLostMajority}; this helper encapsulates that convention so callers need not
   * reason about empty flags.
   *
   * @return {@code true} if neither {@code tfGotMajority} nor {@code tfLostMajority} is set, otherwise {@code false}.
   */
  public boolean isEnabled() {
    return !tfGotMajority() && !tfLostMajority();
  }
}
