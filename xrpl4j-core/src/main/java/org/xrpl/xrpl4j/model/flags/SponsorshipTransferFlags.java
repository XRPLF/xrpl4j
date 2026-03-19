package org.xrpl.xrpl4j.model.flags;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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
import org.xrpl.xrpl4j.model.transactions.SponsorshipTransfer;

/**
 * A set of static {@link Flags} which can be set on {@link SponsorshipTransfer} transactions.
 *
 * <p>This class will be marked {@link Beta} until the featureSponsorship amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 */
@Beta
public class SponsorshipTransferFlags extends TransactionFlags {

  /**
   * Constant {@link SponsorshipTransferFlags} for the {@code tfSponsorshipEnd} flag.
   * The sponsor or sponsee is ending the sponsorship, transferring the responsibility of the reserve back to the sponsee.
   */
  protected static final SponsorshipTransferFlags SPONSORSHIP_END = new SponsorshipTransferFlags(0x00010000);

  /**
   * Constant {@link SponsorshipTransferFlags} for the {@code tfSponsorshipCreate} flag.
   * The sponsee is creating a new sponsored object, transferring the responsibility of the reserve to a sponsor.
   */
  protected static final SponsorshipTransferFlags SPONSORSHIP_CREATE = new SponsorshipTransferFlags(0x00020000);

  /**
   * Constant {@link SponsorshipTransferFlags} for the {@code tfSponsorshipReassign} flag.
   * The sponsee is reassigning a sponsorship, transferring the responsibility of the reserve from one sponsor to another.
   */
  protected static final SponsorshipTransferFlags SPONSORSHIP_REASSIGN = new SponsorshipTransferFlags(0x00040000);

  private SponsorshipTransferFlags(long value) {
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

  /**
   * Construct {@link SponsorshipTransferFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link SponsorshipTransferFlags}.
   *
   * @return New {@link SponsorshipTransferFlags}.
   */
  public static SponsorshipTransferFlags of(long value) {
    return new SponsorshipTransferFlags(value);
  }

  /**
   * Construct an empty instance of {@link SponsorshipTransferFlags}. Transactions will have their {@code Flags} field
   * set to this value by default.
   *
   * @return An empty {@link SponsorshipTransferFlags}.
   */
  public static SponsorshipTransferFlags empty() {
    return new SponsorshipTransferFlags(0);
  }

  /**
   * Check if the {@code tfSponsorshipEnd} flag is set.
   *
   * @return {@code true} if the {@code tfSponsorshipEnd} flag is set, otherwise {@code false}.
   */
  public boolean tfSponsorshipEnd() {
    return this.isSet(SponsorshipTransferFlags.SPONSORSHIP_END);
  }

  /**
   * Check if the {@code tfSponsorshipCreate} flag is set.
   *
   * @return {@code true} if the {@code tfSponsorshipCreate} flag is set, otherwise {@code false}.
   */
  public boolean tfSponsorshipCreate() {
    return this.isSet(SponsorshipTransferFlags.SPONSORSHIP_CREATE);
  }

  /**
   * Check if the {@code tfSponsorshipReassign} flag is set.
   *
   * @return {@code true} if the {@code tfSponsorshipReassign} flag is set, otherwise {@code false}.
   */
  public boolean tfSponsorshipReassign() {
    return this.isSet(SponsorshipTransferFlags.SPONSORSHIP_REASSIGN);
  }

  /**
   * A builder class for {@link SponsorshipTransferFlags} flags.
   */
  public static class Builder {
    private boolean tfSponsorshipEnd = false;
    private boolean tfSponsorshipCreate = false;
    private boolean tfSponsorshipReassign = false;
    private boolean tfFullyCanonicalSig = true;

    /**
     * Set {@code tfSponsorshipEnd} to the given value.
     *
     * @param tfSponsorshipEnd A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfSponsorshipEnd(boolean tfSponsorshipEnd) {
      this.tfSponsorshipEnd = tfSponsorshipEnd;
      return this;
    }

    /**
     * Set {@code tfSponsorshipCreate} to the given value.
     *
     * @param tfSponsorshipCreate A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfSponsorshipCreate(boolean tfSponsorshipCreate) {
      this.tfSponsorshipCreate = tfSponsorshipCreate;
      return this;
    }

    /**
     * Set {@code tfSponsorshipReassign} to the given value.
     *
     * @param tfSponsorshipReassign A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfSponsorshipReassign(boolean tfSponsorshipReassign) {
      this.tfSponsorshipReassign = tfSponsorshipReassign;
      return this;
    }

    /**
     * Set {@code tfFullyCanonicalSig} to the given value.
     *
     * @param tfFullyCanonicalSig A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfFullyCanonicalSig(boolean tfFullyCanonicalSig) {
      this.tfFullyCanonicalSig = tfFullyCanonicalSig;
      return this;
    }

    /**
     * Build a new {@link SponsorshipTransferFlags} from the current boolean values.
     *
     * @return A new {@link SponsorshipTransferFlags}.
     */
    public SponsorshipTransferFlags build() {
      long value = 0;
      if (tfSponsorshipEnd) {
        value |= SPONSORSHIP_END.getValue();
      }
      if (tfSponsorshipCreate) {
        value |= SPONSORSHIP_CREATE.getValue();
      }
      if (tfSponsorshipReassign) {
        value |= SPONSORSHIP_REASSIGN.getValue();
      }
      if (tfFullyCanonicalSig) {
        value |= TransactionFlags.FULLY_CANONICAL_SIG.getValue();
      }
      return new SponsorshipTransferFlags(value);
    }
  }
}

