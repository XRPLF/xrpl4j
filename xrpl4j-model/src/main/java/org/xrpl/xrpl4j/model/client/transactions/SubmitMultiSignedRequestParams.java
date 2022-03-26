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
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Request parameters for the "submit_multisigned" rippled API method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSubmitMultiSignedRequestParams.class)
@JsonDeserialize(as = ImmutableSubmitMultiSignedRequestParams.class)
public interface SubmitMultiSignedRequestParams extends XrplRequestParams {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSubmitMultiSignedRequestParams.Builder}.
   */
  static ImmutableSubmitMultiSignedRequestParams.Builder builder() {
    return ImmutableSubmitMultiSignedRequestParams.builder();
  }

  /**
   * Construct a {@link SubmitMultiSignedRequestParams} with the given {@link Transaction}.
   *
   * @param multiSigTransaction A {@link Transaction} that has been signed by multiple accounts.
   *
   * @return A {@link SubmitMultiSignedRequestParams} populated with the given {@link Transaction}.
   */
  static SubmitMultiSignedRequestParams of(Transaction multiSigTransaction) {
    return SubmitMultiSignedRequestParams.builder().transaction(multiSigTransaction).build();
  }

  /**
   * The {@link Transaction} to submit.
   *
   * @return The {@link Transaction} to submit.
   */
  @JsonProperty("tx_json")
  Transaction transaction();

}
