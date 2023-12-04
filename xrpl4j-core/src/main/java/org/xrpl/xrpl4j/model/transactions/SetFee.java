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
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.jackson.modules.BaseFeeDropsDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.BaseFeeDropsSerializer;

import java.util.Optional;

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
  // TODO say that we Assume no one will ever serialize a SetFee transaction to JSON...
  static ImmutableSetFee.Builder builder() {
    return ImmutableSetFee.builder();
  }

  /**
   * The charge, in drops of XRP, for the reference transaction, as hex. (This is the transaction cost before scaling
   * for load.)
   *
   * @return A hex {@link String} basefee value.
   */
//  @JsonProperty("BaseFee")
  @Value.Default
  @Deprecated
  @JsonIgnore
  default String baseFee() {
    return baseFeeDrops().value().toString(16);
  }


  /*@JsonProperty("BaseFeeDrops")
  @JsonSerialize(using = BaseFeeDropsSerializer.class)
  @JsonDeserialize(using = BaseFeeDropsDeserializer.class)
  @Value.Default
  default XrpCurrencyAmount baseFeeDrops() {
    return XrpCurrencyAmount.of(UnsignedLong.valueOf(baseFee(), 16));
  }*/

  @JsonProperty("BaseFeeDrops")
  @JsonAlias({"BaseFee"})
  XrpCurrencyAmount baseFeeDrops();

  /*@JsonProperty("BaseFee")
  @Value.Default
  default String baseFee() {
    if isPostXrpFeesAmendment() {
      return baseFeeDrops().toString();
    } else {
      throw new RuntimeException();
    }
  }

  @JsonProperty("BaseFeeDrops")
  @Value.Default
  default XrpCurrencyAmount baseFeeDrops() {
    if isPostXrpFeesAmendment() {
      throw new RuntimeException();
    } else {
      return baseFee()
    }
  }

  @JsonIgnore
  default boolean isPostXrpFeesAmendment() {
    return false;
  }*/

  /**
   * The cost, in fee units, of the reference transaction.
   *
   * @return An {@link UnsignedInteger} cost of ref transaction.
   */
  // FIXME: Deprecate this and make it @Nullable. Add a new field Optional<UnsignedInteger> maybeReferenceFeeUnits()
  @JsonProperty("ReferenceFeeUnits")
  @Value.Default
  default UnsignedInteger referenceFeeUnits() {
    return UnsignedInteger.ZERO;
  }

  /**
   * The base reserve, in drops.
   *
   * @return An {@link UnsignedInteger} base reverse value in {@link org.xrpl.xrpl4j.model.client.fees.FeeDrops}.
   */
  @JsonProperty("ReserveBase")
  @Value.Default
  default UnsignedInteger reserveBase() {
    return UnsignedInteger.valueOf(reserveBaseDrops().value().longValue());
  }

  @JsonProperty("ReserveBaseDrops")
  @Value.Default
  default XrpCurrencyAmount reserveBaseDrops() {
    return XrpCurrencyAmount.ofDrops(reserveBase().longValue());
  }

  /**
   * The incremental reserve, in drops.
   *
   * @return An {@link UnsignedInteger} incremental reserve in {@link org.xrpl.xrpl4j.model.client.fees.FeeDrops}.
   */
  @JsonProperty("ReserveIncrement")
  @Value.Default
  default UnsignedInteger reserveIncrement() {
    return UnsignedInteger.valueOf(reserveIncrementDrops().value().longValue());
  }

  @JsonProperty("ReserveIncrementDrops")
  @Value.Default
  default XrpCurrencyAmount reserveIncrementDrops() {
    return XrpCurrencyAmount.ofDrops(reserveIncrement().longValue());
  }

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
