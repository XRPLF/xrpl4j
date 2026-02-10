package org.xrpl.xrpl4j.model.client.transactions;

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
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * The result of a "submit_multisigned" rippled API call.
 *
 * @param <TxnType> The type of {@link Transaction} that was submitted.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSubmitMultiSignedResult.class)
@JsonDeserialize(as = ImmutableSubmitMultiSignedResult.class)
public interface SubmitMultiSignedResult<TxnType extends Transaction> extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @param <T> The actual type of {@link Transaction} that was submitted.
   *
   * @return An {@link ImmutableSubmitMultiSignedResult.Builder}
   */
  static <T extends Transaction> ImmutableSubmitMultiSignedResult.Builder<T> builder() {
    return ImmutableSubmitMultiSignedResult.builder();
  }

  /**
   * Text result code indicating the preliminary result of the transaction, for example "tesSUCCESS".
   *
   * @return A {@link String} containing the result of the submission.
   */
  @JsonProperty("engine_result")
  String engineResult();

  /**
   * Numeric code indicating the preliminary result of the transaction, directly correlated to {@link #engineResult()}.
   *
   * @return A {@link String} containing the result code of the submission.
   */
  @JsonProperty("engine_result_code")
  Integer engineResultCode();

  /**
   * Human-readable explanation of the transaction's preliminary result.
   *
   * @return A {@link String} containing the result message of the submission.
   */
  @JsonProperty("engine_result_message")
  String engineResultMessage();

  /**
   * The complete transaction in hex {@link String} format.
   *
   * @return A hexadecimal encoded {@link String} containing the binary encoded transaction that was submitted.
   */
  @JsonProperty("tx_blob")
  String transactionBlob();

  /**
   * The complete {@link Transaction} that was submitted, as a {@link TransactionResult}.
   *
   * @return A {@link TransactionResult}.
   */
  @JsonProperty("tx_json")
  TransactionResult<TxnType> transaction();

}
