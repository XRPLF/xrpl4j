package org.xrpl.xrpl4j.model.client.fees;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.jackson.modules.UnsignedIntegerStringDeserializer;

import java.util.Optional;

/**
 * The result of a "fee" rippled API call, which reports the current state of the open-ledger requirements
 * for the transaction cost.
 */
@Immutable
@JsonSerialize(as = ImmutableFeeResult.class)
@JsonDeserialize(as = ImmutableFeeResult.class)
public interface FeeResult extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableFeeResult.Builder}.
   */
  static ImmutableFeeResult.Builder builder() {
    return ImmutableFeeResult.builder();
  }

  /**
   * Number of transactions provisionally included in the in-progress ledger.
   *
   * @return An {@link UnsignedInteger} denoting the current ledger size.
   */
  @JsonProperty("current_ledger_size")
  @JsonDeserialize(using = UnsignedIntegerStringDeserializer.class)
  @JsonSerialize(using = ToStringSerializer.class)
  UnsignedInteger currentLedgerSize();

  /**
   * Number of transactions currently queued for the next ledger.
   *
   * @return An {@link UnsignedInteger} denoting the current queue size.
   */
  @JsonProperty("current_queue_size")
  @JsonDeserialize(using = UnsignedIntegerStringDeserializer.class)
  @JsonSerialize(using = ToStringSerializer.class)
  UnsignedInteger currentQueueSize();

  /**
   * Various information about the transaction cost in drops of XRP.
   *
   * @return A {@link FeeDrops}.
   */
  FeeDrops drops();

  /**
   * The approximate number of transactions expected to be included in the current ledger. This is based on the number
   * of transactions in the previous ledger.
   *
   * @return An {@link UnsignedInteger} denoting the expected ledger size.
   */
  @JsonProperty("expected_ledger_size")
  @JsonDeserialize(using = UnsignedIntegerStringDeserializer.class)
  @JsonSerialize(using = ToStringSerializer.class)
  UnsignedInteger expectedLedgerSize();

  /**
   * The Ledger Index of the current open ledger these stats describe.
   *
   * @return A {@link LedgerIndex} denoting the current ledger index.
   */
  @JsonProperty("ledger_current_index")
  LedgerIndex ledgerCurrentIndex();

  /**
   * Various information about the transaction cost, in
   * <a href="https://xrpl.org/transaction-cost.html#fee-levels">fee levels</a>. The ratio in fee
   * levels applies to any transaction relative to the minimum cost of that particular transaction.
   *
   * @return A {@link FeeLevels}.
   */
  FeeLevels levels();

  /**
   * The maximum number of transactions that the transaction queue can currently hold. Optional because this may not be
   * present on older versions of rippled.
   *
   * @return An optionally-present {@link UnsignedInteger} denoting the maximum queue size.
   */
  @JsonProperty("max_queue_size")
  @JsonDeserialize(contentUsing = UnsignedIntegerStringDeserializer.class)
  @JsonSerialize(contentUsing = ToStringSerializer.class)
  Optional<UnsignedInteger> maxQueueSize();

}
