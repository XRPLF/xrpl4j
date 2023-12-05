package org.xrpl.xrpl4j.model.transactions;

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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * A {@link SetFee} pseudo-transaction marks a change in transaction cost or reserve requirements as a result of Fee
 * Voting.
 *
 * @see "https://xrpl.org/setfee.html"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSetFee.class)
@JsonDeserialize(as = ImmutableSetFee.class)
public interface SetFee extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSetFee.Builder}.
   */
  static ImmutableSetFee.Builder builder() {
    return ImmutableSetFee.builder();
  }

  /**
   * The charge, in drops of XRP, for the reference transaction, as hex. (This is the transaction cost before scaling
   * for load.)
   *
   * @return A hex {@link String} baseFee value.
   *
   * @deprecated Prefer {@link #baseFeeDrops()} over this field.
   */
  @Value.Default
  @Deprecated
  @JsonIgnore
  default String baseFee() {
    return baseFeeDrops().value().toString(16);
  }

  /**
   * The charge, in drops of XRP, for the reference transaction (This is the transaction cost before scaling for load).
   *
   * <p>This field will also be populated with the {@code BaseFee} value from any {@link SetFee} transactions
   * that occurred before the XRPFees amendment took effect.</p>
   *
   * @return An {@link XrpCurrencyAmount}.
   */
  @JsonProperty("BaseFeeDrops")
  @JsonAlias("BaseFee")
  XrpCurrencyAmount baseFeeDrops();

  /**
   * The cost, in fee units, of the reference transaction. This field can be {@code null} if this {@link SetFee}
   * transaction was included in a ledger after the XRPFees amendment was enabled because this amendment removes the
   * {@code ReferenceFeeUnits} field from the {@link SetFee} transaction.
   *
   * @return An {@link UnsignedInteger} cost of ref transaction.
   *
   * @deprecated Prefer {@link #maybeReferenceFeeUnits()} over this field.
   */
  @Deprecated
  @Nullable
  @Value.Default
  @JsonIgnore
  default UnsignedInteger referenceFeeUnits() {
    return maybeReferenceFeeUnits().orElse(null);
  }

  /**
   * The cost, in fee units, of the reference transaction, or empty if this {@link SetFee} transaction occurred after
   * the XRPFees amendment was enabled.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("ReferenceFeeUnits")
  Optional<UnsignedInteger> maybeReferenceFeeUnits();

  /**
   * The base reserve, in drops.
   *
   * @return An {@link UnsignedInteger} base reserve value in {@link org.xrpl.xrpl4j.model.client.fees.FeeDrops}.
   *
   * @deprecated Prefer {@link #reserveBaseDrops()} over this field.
   */
  @Value.Default
  @Deprecated
  @JsonIgnore
  default UnsignedInteger reserveBase() {
    return UnsignedInteger.valueOf(reserveBaseDrops().value().longValue());
  }

  /**
   * The base reserve, as an {@link XrpCurrencyAmount}.
   *
   * <p>This field will also be populated with the {@code ReserveBase} value from any {@link SetFee} transactions
   * that occurred before the XRPFees amendment took effect.</p>
   *
   * @return An {@link XrpCurrencyAmount}.
   */
  @JsonProperty("ReserveBaseDrops")
  @JsonAlias("ReserveBase")
  XrpCurrencyAmount reserveBaseDrops();

  /**
   * The incremental reserve, in drops.
   *
   * @return An {@link UnsignedInteger} incremental reserve in {@link org.xrpl.xrpl4j.model.client.fees.FeeDrops}.
   *
   * @deprecated Prefer {@link #reserveIncrementDrops()} over this field.
   */
  @Deprecated
  @JsonIgnore
  @Value.Default
  default UnsignedInteger reserveIncrement() {
    return UnsignedInteger.valueOf(reserveIncrementDrops().value().longValue());
  }

  /**
   * The incremental reserve, as an {@link XrpCurrencyAmount}.
   *
   * <p>This field will also be populated with the {@code ReserveIncrement} value from any {@link SetFee} transactions
   * that occurred before the XRPFees amendment took effect.</p>
   *
   * @return An {@link XrpCurrencyAmount}.
   */
  @JsonProperty("ReserveIncrementDrops")
  @JsonAlias("ReserveIncrement")
  XrpCurrencyAmount reserveIncrementDrops();

  /**
   * The index of the ledger version where this pseudo-transaction appears. This distinguishes the pseudo-transaction
   * from other occurrences of the same change. Omitted for some historical SetFee pseudo-transactions hence making it
   * optional.
   *
   * @return A {@link LedgerIndex} to indicates where the tx appears.
   */
  @JsonProperty("LedgerSequence")
  Optional<LedgerIndex> ledgerSequence();
}
