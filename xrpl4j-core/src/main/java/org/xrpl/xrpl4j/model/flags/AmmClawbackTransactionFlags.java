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

import org.xrpl.xrpl4j.model.transactions.AmmClawback;

/**
 * {@link TransactionFlags} for {@link AmmClawback} transactions.
 */
public class AmmClawbackTransactionFlags extends TransactionFlags {
  /**
   * Constant for an unset flag.
   */
  protected static final AmmClawbackTransactionFlags UNSET = new AmmClawbackTransactionFlags(0);

  /**
   * Constant for the {@code tfClawTwoAssets} flag.
   */
  protected static final AmmClawbackTransactionFlags CLAW_TWO_ASSETS = new AmmClawbackTransactionFlags(0x00000001);

  private AmmClawbackTransactionFlags(long value) {
    super(value);
  }

  private AmmClawbackTransactionFlags() {
  }

  /**
   * Construct {@link AmmClawbackTransactionFlags} with a given value.
   *
   * @param tfFullyCanonicalSig The long-number encoded flags value of this {@link AmmClawbackTransactionFlags}.
   *
   * @return New {@link AmmClawbackTransactionFlags}.
   */
  public static AmmClawbackTransactionFlags of(boolean tfFullyCanonicalSig) {
    return new AmmClawbackTransactionFlags(
        Flags.of(
            tfFullyCanonicalSig ? CLAW_TWO_ASSETS : UNSET
        ).getValue()
    );
  }

  /**
   * Construct an empty instance of {@link AmmClawbackTransactionFlags}.
   *
   * @return An empty {@link AmmClawbackTransactionFlags}.
   */
  public static AmmClawbackTransactionFlags empty() {
    return new AmmClawbackTransactionFlags();
  }

  /**
   * Create a new {@link Builder}.
   *
   * @return A new {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Whether or not the {@code tfClawTwoAssets} flag is set.
   *
   * @return {@code true} if {@code tfClawTwoAssets} is set, otherwise {@code false}.
   */
  public boolean tfClawTwoAssets() {
    return this.isSet(CLAW_TWO_ASSETS);
  }

  /**
   * A builder class for {@link AmmClawbackTransactionFlags}.
   */
  public static class Builder {
    private boolean tfClawTwoAssets = false;

    /**
     * Set {@code tfClawTwoAssets} to the given value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfClawTwoAssets() {
      this.tfClawTwoAssets = true;
      return this;
    }

    /**
     * Build a new {@link AmmClawbackTransactionFlags} from the current boolean values.
     *
     * @return A new {@link AmmClawbackTransactionFlags}.
     */
    public AmmClawbackTransactionFlags build() {
      return AmmClawbackTransactionFlags.of(tfClawTwoAssets);
    }
  }
}
