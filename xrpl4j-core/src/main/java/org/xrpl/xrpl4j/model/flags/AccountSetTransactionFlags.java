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

import com.google.common.base.Preconditions;
import org.xrpl.xrpl4j.model.transactions.AccountSet;

/**
 * {@link TransactionFlags} for {@link AccountSet} transactions. Note that using these directly is
 * discouraged, but can be useful when setting multiple flags for an account.
 */
public class AccountSetTransactionFlags extends TransactionFlags {
  /**
   * Constant for an unset flag.
   */
  protected static final AccountSetTransactionFlags UNSET = new AccountSetTransactionFlags(0);

  /**
   * Constant for the {@code tfRequireDestTag} flag.
   */
  protected static final AccountSetTransactionFlags REQUIRE_DEST_TAG = new AccountSetTransactionFlags(0x00010000);

  /**
   * Constant for the {@code tfOptionalDestTag} flag.
   */
  protected static final AccountSetTransactionFlags OPTIONAL_DEST_TAG = new AccountSetTransactionFlags(0x00020000);

  /**
   * Constant for the {@code tfRequireAuth} flag.
   */
  protected static final AccountSetTransactionFlags REQUIRE_AUTH = new AccountSetTransactionFlags(0x00040000);

  /**
   * Constant for the {@code tfOptionalAuth} flag.
   */
  protected static final AccountSetTransactionFlags OPTIONAL_AUTH = new AccountSetTransactionFlags(0x00080000);

  /**
   * Constant for the {@code tfDisallowXRP} flag.
   */
  protected static final AccountSetTransactionFlags DISALLOW_XRP = new AccountSetTransactionFlags(0x00100000);

  /**
   * Constant for the {@code tfAllowXRP} flag.
   */
  protected static final AccountSetTransactionFlags ALLOW_XRP = new AccountSetTransactionFlags(0x00200000);

  private AccountSetTransactionFlags(long value) {
    super(value);
  }

  private AccountSetTransactionFlags() {}

  private static AccountSetTransactionFlags of(
    boolean tfFullyCanonicalSig,
    boolean tfRequireDestTag,
    boolean tfOptionalDestTag,
    boolean tfRequireAuth,
    boolean tfOptionalAuth,
    boolean tfDisallowXrp,
    boolean tfAllowXrp
  ) {
    Preconditions.checkArgument(
      !(tfRequireDestTag && tfOptionalDestTag),
      "tfRequireDestTag and tfOptionalDestTag cannot both be set to true."
    );

    Preconditions.checkArgument(
      !(tfRequireAuth && tfOptionalAuth),
      "tfRequireAuth and tfOptionalAuth cannot both be set to true."
    );

    Preconditions.checkArgument(
      !(tfDisallowXrp && tfAllowXrp),
      "tfDisallowXrp and tfAllowXrp cannot both be set to true."
    );
    return new AccountSetTransactionFlags(
      Flags.of(
        tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
        tfRequireDestTag ? REQUIRE_DEST_TAG : UNSET,
        tfOptionalDestTag ? OPTIONAL_DEST_TAG : UNSET,
        tfRequireAuth ? REQUIRE_AUTH : UNSET,
        tfOptionalAuth ? OPTIONAL_AUTH : UNSET,
        tfDisallowXrp ? DISALLOW_XRP : UNSET,
        tfAllowXrp ? ALLOW_XRP : UNSET
      ).getValue()
    );
  }

  /**
   * Construct {@link AccountSetTransactionFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link AccountSetTransactionFlags}.
   *
   * @return New {@link AccountSetTransactionFlags}.
   */
  public static AccountSetTransactionFlags of(long value) {
    AccountSetTransactionFlags flags = new AccountSetTransactionFlags(value);

    Preconditions.checkArgument(
      !(flags.tfRequireDestTag() && flags.tfOptionalDestTag()),
      "tfRequireDestTag and tfOptionalDestTag cannot both be set to true."
    );

    Preconditions.checkArgument(
      !(flags.tfRequireAuth() && flags.tfOptionalAuth()),
      "tfRequireAuth and tfOptionalAuth cannot both be set to true."
    );

    Preconditions.checkArgument(
      !(flags.tfDisallowXrp() && flags.tfAllowXrp()),
      "tfDisallowXrp and tfAllowXrp cannot both be set to true."
    );

    return flags;
  }

  /**
   * Construct an empty instance of {@link AccountSetTransactionFlags}. Transactions with empty flags will
   * not be serialized with a {@code Flags} field.
   *
   * @return An empty {@link AccountSetTransactionFlags}.
   */
  public static AccountSetTransactionFlags empty() {
    return new AccountSetTransactionFlags();
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
   * Require a fully canonical signature.
   *
   * @return {@code true} if {@code tfFullyCanonicalSig} is set, otherwise {@code false}.
   */
  public boolean tfFullyCanonicalSig() {
    return this.isSet(TransactionFlags.FULLY_CANONICAL_SIG);
  }

  /**
   * Whether or not the {@code tfRequireDestTag} flag is set.
   *
   * @return {@code true} if {@code tfRequireDestTag} is set, otherwise {@code false}.
   */
  public boolean tfRequireDestTag() {
    return this.isSet(REQUIRE_DEST_TAG);
  }

  /**
   * Whether or not the {@code tfOptionalDestTag} flag is set.
   *
   * @return {@code true} if {@code tfOptionalDestTag} is set, otherwise {@code false}.
   */
  public boolean tfOptionalDestTag() {
    return this.isSet(OPTIONAL_DEST_TAG);
  }

  /**
   * Whether or not the {@code tfRequireAuth} flag is set.
   *
   * @return {@code true} if {@code tfRequireAuth} is set, otherwise {@code false}.
   */
  public boolean tfRequireAuth() {
    return this.isSet(REQUIRE_AUTH);
  }

  /**
   * Whether or not the {@code tfOptionalAuth} flag is set.
   *
   * @return {@code true} if {@code tfOptionalAuth} is set, otherwise {@code false}.
   */
  public boolean tfOptionalAuth() {
    return this.isSet(OPTIONAL_AUTH);
  }

  /**
   * Whether or not the {@code tfDisallowXrp} flag is set.
   *
   * @return {@code true} if {@code tfDisallowXrp} is set, otherwise {@code false}.
   */
  public boolean tfDisallowXrp() {
    return this.isSet(DISALLOW_XRP);
  }

  /**
   * Whether or not the {@code tfAllowXrp} flag is set.
   *
   * @return {@code true} if {@code tfAllowXrp} is set, otherwise {@code false}.
   */
  public boolean tfAllowXrp() {
    return this.isSet(ALLOW_XRP);
  }

  /**
   * A builder class for {@link AccountSetTransactionFlags}.
   */
  public static class Builder {
    private boolean tfRequireDestTag = false;
    private boolean tfOptionalDestTag = false;
    private boolean tfRequireAuth = false;
    private boolean tfOptionalAuth = false;
    private boolean tfDisallowXrp = false;
    private boolean tfAllowXrp = false;

    /**
     * Set {@code tfRequireDestTag} to the given value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfRequireDestTag() {
      this.tfRequireDestTag = true;
      return this;
    }

    /**
     * Set {@code tfOptionalDestTag} to the given value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfOptionalDestTag() {
      this.tfOptionalDestTag = true;
      return this;
    }

    /**
     * Set {@code tfRequireAuth} to the given value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfRequireAuth() {
      this.tfRequireAuth = true;
      return this;
    }

    /**
     * Set {@code tfOptionalAuth} to the given value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfOptionalAuth() {
      this.tfOptionalAuth = true;
      return this;
    }

    /**
     * Set {@code tfDisallowXrp} to the given value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfDisallowXrp() {
      this.tfDisallowXrp = true;
      return this;
    }

    /**
     * Set {@code tfAllowXrp} to the given value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfAllowXrp() {
      this.tfAllowXrp = true;
      return this;
    }

    /**
     * Build a new {@link AccountSetTransactionFlags} from the current boolean values.
     *
     * @return A new {@link AccountSetTransactionFlags}.
     */
    public AccountSetTransactionFlags build() {
      return AccountSetTransactionFlags.of(
        true,
        tfRequireDestTag,
        tfOptionalDestTag,
        tfRequireAuth,
        tfOptionalAuth,
        tfDisallowXrp,
        tfAllowXrp
      );
    }
  }
}
