package org.xrpl.xrpl4j.client.jsonrpc.model;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: client
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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.XrplResult;

/**
 * Generic JSON RPC response object.
 *
 * @param <ResultType> The type of {@link JsonRpcResponse#result()}, which varies based on API method.
 */
@Immutable
@JsonSerialize(as = ImmutableJsonRpcResponse.class)
@JsonDeserialize(as = ImmutableJsonRpcResponse.class)
public interface JsonRpcResponse<ResultType extends XrplResult> {

  /**
   * The result of a request to the rippled JSON RPC API. Contents vary depending on the API method.
   *
   * @return The result of a request, represented by a {@link ResultType}.
   */
  ResultType result();

}
