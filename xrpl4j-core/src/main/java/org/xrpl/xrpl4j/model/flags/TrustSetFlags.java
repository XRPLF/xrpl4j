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
import org.xrpl.xrpl4j.model.transactions.TrustSet;

/**
 * A set of static {@link TransactionFlags} which can be set on {@link TrustSet}
 * transactions.
 */
public class TrustSetFlags extends TransactionFlags {

  /**
   * Constant for an unset flag.
   */
  protected static final TrustSetFlags UNSET = new TrustSetFlags(0);

  /**
   * Constant {@link TrustSetFlags} for the {@code tfSetfAuth} flag.
   */
  protected static final TrustSetFlags SET_F_AUTH = new TrustSetFlags(0x00010000);

  /**
   * Constant {@link TrustSetFlags} for the {@code tfSetNoRipple} flag.
   */
  protected static final TrustSetFlags SET_NO_RIPPLE = new TrustSetFlags(0x00020000);

  /**
   * Constant {@link TrustSetFlags} for the {@code tfClearNoRipple} flag.
   */
  protected static final TrustSetFlags CLEAR_NO_RIPPLE = new TrustSetFlags(0x00040000);

  /**
   * Constant {@link TrustSetFlags} for the {@code tfSetFreeze} flag.
   */
  protected static final TrustSetFlags SET_FREEZE = new TrustSetFlags(0x00100000);

  /**
   * Constant {@link TrustSetFlags} for the {@code tfClearFreeze} flag.
   */
  protected static final TrustSetFlags CLEAR_FREEZE = new TrustSetFlags(0x00200000);

  private TrustSetFlags(long value) {
    super(value);
  }

  /**
   * Create a new {@link Builder}.
   *
   * @return A new {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  private static TrustSetFlags of(
    boolean tfFullyCanonicalSig,
    boolean tfSetfAuth,
    boolean tfSetNoRipple,
    boolean tfClearNoRipple,
    boolean tfSetFreeze,
    boolean tfClearFreeze
  ) {
    return new TrustSetFlags(
      Flags.of(
        tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
        tfSetfAuth ? SET_F_AUTH : UNSET,
        tfSetNoRipple ? SET_NO_RIPPLE : UNSET,
        tfClearNoRipple ? CLEAR_NO_RIPPLE : UNSET,
        tfSetFreeze ? SET_FREEZE : UNSET,
        tfClearFreeze ? CLEAR_FREEZE : UNSET).getValue()
    );
  }

  /**
   * Construct {@link TrustSetFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link TrustSetFlags}.
   *
   * @return New {@link TrustSetFlags}.
   */
  public static TrustSetFlags of(long value) {
    return new TrustSetFlags(value);
  }

  /**
   * Require a fully canonical signature.
   *
   * @return {@code true} if {@code tfFullyCanonicalSig} is set, otherwise {@code false}.
   */
  public boolean tfFullyCanonicalSig() {
    return this.isSet(TransactionFlags.FULLY_CANONICAL_SIG);
  }

  /**
   * Authorize the other party to hold currency issued by this account. (No effect unless using
   * {@link AccountSet.AccountSetFlag#REQUIRE_AUTH}). Cannot be unset.
   *
   * @return {@code true} if {@code tfSetfAuth} is set, otherwise {@code false}.
   */
  public boolean tfSetfAuth() {
    return this.isSet(SET_F_AUTH);
  }

  /**
   * Enable the No Ripple flag, which blocks rippling between two trust lines of the same currency if this
   * flag is enabled on both.
   *
   * @return {@code true} if {@code tfSetNoRipple} is set, otherwise {@code false}.
   */
  public boolean tfSetNoRipple() {
    return this.isSet(SET_NO_RIPPLE);
  }

  /**
   * Disable the No Ripple flag, allowing rippling on this trust line.
   *
   * @return {@code true} if {@code tfClearNoRipple} is set, otherwise {@code false}.
   */
  public boolean tfClearNoRipple() {
    return this.isSet(CLEAR_NO_RIPPLE);
  }

  /**
   * <a href="https://xrpl.org/freezes.html">Freeze</a> the trust line.
   *
   * @return {@code true} if {@code tfSetFreeze} is set, otherwise {@code false}.
   */
  public boolean tfSetFreeze() {
    return this.isSet(SET_FREEZE);
  }

  /**
   * <a href="https://xrpl.org/freezes.html">Unfreeze</a> the trust line.
   *
   * @return {@code true} if {@code tfClearFreeze} is set, otherwise {@code false}.
   */
  public boolean tfClearFreeze() {
    return this.isSet(CLEAR_FREEZE);
  }

  /**
   * A builder class for {@link TrustSetFlags}.
   */
  public static class Builder {
    private boolean tfSetfAuth = false;
    private boolean tfSetNoRipple = false;
    private boolean tfClearNoRipple = false;
    private boolean tfSetFreeze = false;
    private boolean tfClearFreeze = false;

    /**
     * Set {@code tfSetfAuth} to the given value.
     *
     * @param tfSetfAuth A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfSetfAuth(boolean tfSetfAuth) {
      this.tfSetfAuth = tfSetfAuth;
      return this;
    }

    /**
     * Set {@code tfSetNoRipple} to {@code true}.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfSetNoRipple() {
      this.tfSetNoRipple = true;
      return this;
    }

    /**
     * Set {@code tfClearNoRipple} to {@code true}.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfClearNoRipple() {
      this.tfClearNoRipple = true;
      return this;
    }

    /**
     * Set {@code tfSetFreeze} to {@code true}.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfSetFreeze() {
      this.tfSetFreeze = true;
      return this;
    }

    /**
     * Set {@code tfClearFreeze} to {@code true}.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfClearFreeze() {
      this.tfClearFreeze = true;
      return this;
    }

    /**
     * Build a new {@link TrustSetFlags} from the current boolean values.
     *
     * @return A new {@link TrustSetFlags}.
     */
    public TrustSetFlags build() {
      return TrustSetFlags.of(
        true,
        tfSetfAuth,
        tfSetNoRipple,
        tfClearNoRipple,
        tfSetFreeze,
        tfClearFreeze
      );
    }
  }
}
