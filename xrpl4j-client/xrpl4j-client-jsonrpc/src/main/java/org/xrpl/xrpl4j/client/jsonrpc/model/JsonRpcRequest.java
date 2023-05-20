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
import org.xrpl.xrpl4j.model.client.XrplRequestParams;

import java.util.List;

/**
 * Generic rippled JSON RPC request object.
 */
@Immutable
@JsonSerialize(as = ImmutableJsonRpcRequest.class)
@JsonDeserialize(as = ImmutableJsonRpcRequest.class)
public interface JsonRpcRequest {
  
  static ImmutableJsonRpcRequest.Builder builder() {
    return ImmutableJsonRpcRequest.builder();
  }
  
  /**
   * The name of the <a href="https://xrpl.org/public-rippled-methods.html">API method</a>.
   *
   * @return A {@link String} containing the method name.
   */
  String method();
  
  /**
   * A one-item {@link List} containing a {@link XrplRequestParams} with the parameters to this method. You may omit
   * this field if the method does not require any parameters.
   *
   * @return A {@link List} of {@link XrplRequestParams}.
   */
  List<XrplRequestParams> params();
  
}
