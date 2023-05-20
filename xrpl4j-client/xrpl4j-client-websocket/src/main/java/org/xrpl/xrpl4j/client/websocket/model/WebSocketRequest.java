package org.xrpl.xrpl4j.client.websocket.model;

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
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;

import java.util.List;

/**
 * Generic rippled JSON RPC request object.
 */
//@Immutable
//@JsonSerialize(as = ImmutableWebSocketRequest.class)
//@JsonDeserialize(as = ImmutableWebSocketRequest.class)
public interface WebSocketRequest {
  
//  static ImmutableWebSocketRequest.Builder builder() {
//    return ImmutableWebSocketRequest.builder();
//  }
  
  UnsignedInteger id();
  
  String command();
  
//  T WebSocket
  
}
