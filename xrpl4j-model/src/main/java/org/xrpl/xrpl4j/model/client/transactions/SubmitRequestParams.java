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

/**
 * Request parameters for the "submit" rippled API method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSubmitRequestParams.class)
@JsonDeserialize(as = ImmutableSubmitRequestParams.class)
public interface SubmitRequestParams extends XrplRequestParams {

  /**
   * Construct a {@link SubmitRequestParams} containing the given transaction blob as its only parameter.
   *
   * @param blobHex The binary serialized transaction to submit, as a hexadecimal encoded {@link String}.
   *
   * @return A new {@link SubmitRequestParams}.
   */
  static SubmitRequestParams of(String blobHex) {
    return ImmutableSubmitRequestParams.builder().txBlob(blobHex).build();
  }

  /**
   * The hex encoded {@link String} containing a signed, binary encoded transaction.
   *
   * @return A {@link String} containing the transaction blob.
   */
  @JsonProperty("tx_blob")
  String txBlob();

}
