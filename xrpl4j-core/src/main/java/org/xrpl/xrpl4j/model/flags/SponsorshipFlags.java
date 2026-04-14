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
 * A set of static {@link Flags} which can be set on {@link org.xrpl.xrpl4j.model.ledger.SponsorshipObject}s.
 *
 * <p>This class will be marked {@link Beta} until the featureSponsorship amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/blob/master/XLS-0068-sponsored-fees-and-reserves/README.md"
 */
@Beta
public class SponsorshipFlags extends Flags {

  /**
   * Constant for an unset flag.
   */
  public static final SponsorshipFlags UNSET = new SponsorshipFlags(0);

  /**
   * Constant {@link SponsorshipFlags} for the {@code lsfSponsorshipRequireSignForFee} flag.
   *
   * <p>If enabled, transactions that use this Sponsorship object for fee sponsorship must include
   * a sponsor signature on the transaction.</p>
   */
  public static final SponsorshipFlags REQUIRE_SIGN_FOR_FEE = new SponsorshipFlags(0x00000001L);

  /**
   * Constant {@link SponsorshipFlags} for the {@code lsfSponsorshipRequireSignForReserve} flag.
   *
   * <p>If enabled, transactions that use this Sponsorship object for reserve sponsorship must include
   * a sponsor signature on the transaction.</p>
   */
  public static final SponsorshipFlags REQUIRE_SIGN_FOR_RESERVE = new SponsorshipFlags(0x00000002L);

  /**
   * Required-args Constructor.
   *
   * @param value The long-number encoded flags value of this {@link SponsorshipFlags}.
   */
  private SponsorshipFlags(final long value) {
    super(value);
  }

  /**
   * Construct {@link SponsorshipFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link SponsorshipFlags}.
   *
   * @return New {@link SponsorshipFlags}.
   */
  public static SponsorshipFlags of(long value) {
    return new SponsorshipFlags(value);
  }

  /**
   * If enabled, transactions that use this Sponsorship object for fee sponsorship must include
   * a sponsor signature on the transaction.
   *
   * @return {@code true} if {@code lsfSponsorshipRequireSignForFee} is set, otherwise {@code false}.
   */
  public boolean lsfSponsorshipRequireSignForFee() {
    return this.isSet(SponsorshipFlags.REQUIRE_SIGN_FOR_FEE);
  }

  /**
   * If enabled, transactions that use this Sponsorship object for reserve sponsorship must include
   * a sponsor signature on the transaction.
   *
   * @return {@code true} if {@code lsfSponsorshipRequireSignForReserve} is set, otherwise {@code false}.
   */
  public boolean lsfSponsorshipRequireSignForReserve() {
    return this.isSet(SponsorshipFlags.REQUIRE_SIGN_FOR_RESERVE);
  }

}
