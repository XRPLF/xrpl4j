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

  static Builder builder() {
    return new Builder();
  }

  /**
   * Constant {@link BatchFlags} for the {@code tfAllOrNothing} flag. All transactions must succeed for any of them to
   * succeed.
   */
  public static final BatchFlags ALL_OR_NOTHING = new BatchFlags(0x00010000L);

  /**
   * Constant {@link BatchFlags} for the {@code tfOnlyOne} flag. The first transaction to succeed will be the only one
   * to succeed.
   */
  public static final BatchFlags ONLY_ONE = new BatchFlags(0x00020000L);

  /**
   * Constant {@link BatchFlags} for the {@code tfUntilFailure} flag. All transactions will be applied until the first
   * failure.
   */
  public static final BatchFlags UNTIL_FAILURE = new BatchFlags(0x00040000L);

  /**
   * Constant {@link BatchFlags} for the {@code tfIndependent} flag. All transactions will be applied, regardless of
   * failure.
   */
  public static final BatchFlags INDEPENDENT = new BatchFlags(0x00080000L);

  /**
   * Constant for an unset flag.
   */
  public static final BatchFlags UNSET = new BatchFlags(0L);

  /**
   * Constant empty {@link TransactionFlags}.
   */
  public static final BatchFlags EMPTY = new BatchFlags();

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
   *
   * @return New {@link BatchFlags}.
   */
  public static BatchFlags of(long value) {
    return new BatchFlags(value);
  }

  /**
   * Constructs a {@link BatchFlags} object based on the specified boolean flags. Each flag corresponds to a specific
   * mode or configuration for the batch processing.
   *
   * @param tfFullyCanonicalSig Indicates whether the `tfFullyCanonicalSig` flag should be set.
   * @param tfAllOrNothing      Indicates whether the `tfAllOrNothing` flag should be set.
   * @param tfOnlyOne           Indicates whether the `tfOnlyOne` flag should be set.
   * @param tfUntilFailure      Indicates whether the `tfUntilFailure` flag should be set.
   * @param tfIndependent       Indicates whether the `tfIndependent` flag should be set.
   *
   * @return A new {@link BatchFlags} instance with the corresponding flags set.
   */
  private static BatchFlags of(
    boolean tfFullyCanonicalSig,
    boolean tfAllOrNothing,
    boolean tfOnlyOne,
    boolean tfUntilFailure,
    boolean tfIndependent
  ) {
    return new BatchFlags(of(
      tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
      tfAllOrNothing ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
      tfOnlyOne ? ONLY_ONE : UNSET,
      tfUntilFailure ? UNTIL_FAILURE : UNSET,
      tfIndependent ? INDEPENDENT : UNSET
    ).getValue());
  }

  public BatchFlags with(BatchFlags flags) {
    return new BatchFlags(this.bitwiseOr(flags).getValue());
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

  /**
   * A builder class for {@link PaymentFlags} flags.
   */
  public static class Builder {

    boolean tfAllOrNothing = false;
    boolean tfOnlyOne = false;
    boolean tfUntilFailure = false;
    boolean tfIndependent = false;

    /**
     * Private constructor to prevent direct instantiation of the {@link Builder} class. This ensures that the Builder
     * can only be accessed through controlled methods within its enclosing class.
     *
     * <p>Use {@link BatchFlags#builder()} instead.
     */
    private Builder() {
      // To avoid direct instantiation.
    }

    /**
     * Set {@code tfAllOrNothing} to the given value.
     *
     * @param tfAllOrNothing A boolean value.
     *
     * @return The same {@link PaymentFlags.Builder}.
     */
    public BatchFlags.Builder tfAllOrNothing(boolean tfAllOrNothing) {
      this.tfAllOrNothing = tfAllOrNothing;
      return this;
    }

    /**
     * Set {@code tfOnlyOne} to the given value.
     *
     * @param tfOnlyOne A boolean value.
     *
     * @return The same {@link PaymentFlags.Builder}.
     */
    public BatchFlags.Builder tfOnlyOne(boolean tfOnlyOne) {
      this.tfOnlyOne = tfOnlyOne;
      return this;
    }

    /**
     * Set {@code tfLimitQuality} to the given value.
     *
     * @param tfUntilFailure A boolean value.
     *
     * @return The same {@link PaymentFlags.Builder}.
     */
    public BatchFlags.Builder tfUntilFailure(boolean tfUntilFailure) {
      this.tfUntilFailure = tfUntilFailure;
      return this;
    }

    /**
     * Set {@code tfIndependent} to the given value.
     *
     * @param tfIndependent A boolean value.
     *
     * @return The same {@link PaymentFlags.Builder}.
     */
    public BatchFlags.Builder tfIndependent(boolean tfIndependent) {
      this.tfIndependent = tfIndependent;
      return this;
    }

    /**
     * Build a new {@link PaymentFlags} from the current boolean values.
     *
     * @return A new {@link PaymentFlags}.
     */
    public BatchFlags build() {
      return BatchFlags.of(true, tfAllOrNothing, tfOnlyOne, tfUntilFailure, tfIndependent);
    }
  }

}
