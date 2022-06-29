package org.xrpl.xrpl4j.model.client.server;

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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.XrplResult;

/**
 * Result of a "server_info" rippled API method request.
 *
 * @deprecated {@link ServerInfo} was deprecated hence this interface is being deprecated as well.
 *   Use {@link org.xrpl.xrpl4j.model.client.serverinfo.ServerInfoResult} instead.
 */
@Deprecated
@Immutable
@JsonSerialize(as = ImmutableServerInfoResult.class)
@JsonDeserialize(as = ImmutableServerInfoResult.class)
public interface ServerInfoResult extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableServerInfoResult.Builder}.
   */
  static ImmutableServerInfoResult.Builder builder() {
    return ImmutableServerInfoResult.builder();
  }

  /**
   * Information about the requested server.
   *
   * @return A {@link ServerInfo}.
   */
  ServerInfo info();

}
