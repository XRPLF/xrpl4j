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
 * Flags for the {@code SponsorFlags} field on transactions that use sponsorship. These flags indicate
 * what type of sponsorship is being used for a transaction.
 *
 * <p>At least one of {@link #SPONSOR_FEE} or {@link #SPONSOR_RESERVE} must be set when sponsorship is used.</p>
 *
 * <p>This class will be marked {@link Beta} until the featureSponsorship amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/blob/master/XLS-0068-sponsored-fees-and-reserves/README.md"
 */
@Beta
public class SponsorFlags extends Flags {

  /**
   * Constant for an unset flag.
   */
  public static final SponsorFlags UNSET = new SponsorFlags(0);

  /**
   * Constant {@link SponsorFlags} for the {@code spfSponsorFee} flag.
   *
   * <p>When set, the sponsor is paying the transaction fee.</p>
   */
  public static final SponsorFlags SPONSOR_FEE = new SponsorFlags(0x00000001L);

  /**
   * Constant {@link SponsorFlags} for the {@code spfSponsorReserve} flag.
   *
   * <p>When set, the sponsor is covering reserve requirements for any ledger objects created by the transaction.</p>
   */
  public static final SponsorFlags SPONSOR_RESERVE = new SponsorFlags(0x00000002L);

  /**
   * Required-args Constructor.
   *
   * @param value The long-number encoded flags value of this {@link SponsorFlags}.
   */
  private SponsorFlags(final long value) {
    super(value);
  }

  /**
   * Construct {@link SponsorFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link SponsorFlags}.
   *
   * @return New {@link SponsorFlags}.
   */
  public static SponsorFlags of(long value) {
    return new SponsorFlags(value);
  }

  /**
   * Indicates whether the sponsor is paying the transaction fee.
   *
   * @return {@code true} if {@code spfSponsorFee} is set, otherwise {@code false}.
   */
  public boolean spfSponsorFee() {
    return this.isSet(SponsorFlags.SPONSOR_FEE);
  }

  /**
   * Indicates whether the sponsor is covering reserve requirements for any ledger objects created by the transaction.
   *
   * @return {@code true} if {@code spfSponsorReserve} is set, otherwise {@code false}.
   */
  public boolean spfSponsorReserve() {
    return this.isSet(SponsorFlags.SPONSOR_RESERVE);
  }

  /**
   * Validates that at least one of the sponsor flags is set.
   *
   * <p>Per XLS-0068, at least one of {@code spfSponsorFee} or {@code spfSponsorReserve} must be set
   * when sponsorship is used.</p>
   *
   * @return {@code true} if at least one flag is set, otherwise {@code false}.
   */
  public boolean isValid() {
    return spfSponsorFee() || spfSponsorReserve();
  }

}
