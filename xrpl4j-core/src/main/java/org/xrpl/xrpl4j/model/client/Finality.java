package org.xrpl.xrpl4j.model.client;

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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Defines how an XRP Ledger finality decision is represented; includes both a finality status and an engine code.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableFinality.class)
@JsonDeserialize(as = ImmutableFinality.class)
public interface Finality {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableFinality.Builder}.
   */
  static ImmutableFinality.Builder builder() {
    return ImmutableFinality.builder();
  }

  /**
   * Get {@link FinalityStatus} status value for a transaction.
   *
   * @return {@link FinalityStatus} value for a
   *   {@link org.xrpl.xrpl4j.model.transactions.Transaction}.
   */
  FinalityStatus finalityStatus();

  /**
   * The rippled server summarizes transaction results with result codes, which appear in fields such as engine_result
   * and meta.TransactionResult. These codes are grouped into several categories of with different prefixes.
   *
   * @return A {@link String} containing the result of the submission.
   * @see "https://xrpl.org/transaction-results.html#transaction-results"
   */
  Optional<String> resultCode();

  /**
   * Text result code indicating the preliminary result of the transaction.
   *
   * @return The #result() value for the particular transaction.
   */
  @Value.Auxiliary
  default String resultCodeSafe() {
    return resultCode().orElseThrow(() -> new IllegalStateException("Finality does not contain resultCode."));
  }
}
