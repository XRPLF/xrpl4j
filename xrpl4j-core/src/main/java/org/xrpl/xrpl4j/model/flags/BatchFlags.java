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

import com.google.common.annotations.Beta;

/**
 * A set of {@link TransactionFlags} for {@link org.xrpl.xrpl4j.model.transactions.Batch} transactions.
 *
 * <p>Exactly one of the four batch mode flags must be set on a Batch transaction.</p>
 *
 * <p>This class will be marked {@link Beta} until the featureBatch amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 */
@Beta
public class BatchFlags extends TransactionFlags {

  /**
   * Constant {@link BatchFlags} for the {@code tfAllOrNothing} flag.
   * All transactions must succeed for any of them to succeed.
   */
  public static final BatchFlags ALL_OR_NOTHING = new BatchFlags(0x00010000L);

  /**
   * Constant {@link BatchFlags} for the {@code tfOnlyOne} flag.
   * The first transaction to succeed will be the only one to succeed.
   */
  public static final BatchFlags ONLY_ONE = new BatchFlags(0x00020000L);

  /**
   * Constant {@link BatchFlags} for the {@code tfUntilFailure} flag.
   * All transactions will be applied until the first failure.
   */
  public static final BatchFlags UNTIL_FAILURE = new BatchFlags(0x00040000L);

  /**
   * Constant {@link BatchFlags} for the {@code tfIndependent} flag.
   * All transactions will be applied, regardless of failure.
   */
  public static final BatchFlags INDEPENDENT = new BatchFlags(0x00080000L);

  private BatchFlags(long value) {
    super(value);
  }

  private BatchFlags() {
    super();
  }

  /**
   * Construct {@link BatchFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link BatchFlags}.
   * @return New {@link BatchFlags}.
   */
  public static BatchFlags of(long value) {
    return new BatchFlags(value);
  }

  /**
   * Construct {@link BatchFlags} for AllOrNothing mode.
   *
   * @return {@link BatchFlags} with tfAllOrNothing set.
   */
  public static BatchFlags ofAllOrNothing() {
    return ALL_OR_NOTHING;
  }

  /**
   * Construct {@link BatchFlags} for OnlyOne mode.
   *
   * @return {@link BatchFlags} with tfOnlyOne set.
   */
  public static BatchFlags ofOnlyOne() {
    return ONLY_ONE;
  }

  /**
   * Construct {@link BatchFlags} for UntilFailure mode.
   *
   * @return {@link BatchFlags} with tfUntilFailure set.
   */
  public static BatchFlags ofUntilFailure() {
    return UNTIL_FAILURE;
  }

  /**
   * Construct {@link BatchFlags} for Independent mode.
   *
   * @return {@link BatchFlags} with tfIndependent set.
   */
  public static BatchFlags ofIndependent() {
    return INDEPENDENT;
  }

  /**
   * Check if the {@code tfAllOrNothing} flag is set.
   *
   * @return {@code true} if {@code tfAllOrNothing} is set, otherwise {@code false}.
   */
  public boolean tfAllOrNothing() {
    return this.isSet(ALL_OR_NOTHING);
  }

  /**
   * Check if the {@code tfOnlyOne} flag is set.
   *
   * @return {@code true} if {@code tfOnlyOne} is set, otherwise {@code false}.
   */
  public boolean tfOnlyOne() {
    return this.isSet(ONLY_ONE);
  }

  /**
   * Check if the {@code tfUntilFailure} flag is set.
   *
   * @return {@code true} if {@code tfUntilFailure} is set, otherwise {@code false}.
   */
  public boolean tfUntilFailure() {
    return this.isSet(UNTIL_FAILURE);
  }

  /**
   * Check if the {@code tfIndependent} flag is set.
   *
   * @return {@code true} if {@code tfIndependent} is set, otherwise {@code false}.
   */
  public boolean tfIndependent() {
    return this.isSet(INDEPENDENT);
  }
}

