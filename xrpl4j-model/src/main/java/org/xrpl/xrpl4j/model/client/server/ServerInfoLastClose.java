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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo.LastClose;

/**
 * Information about the last time the server closed a ledger, including the amount of time it took to reach a consensus
 * and the number of trusted validators participating.
 *
 * @deprecated Package org.xrpl.xrpl4j.model.client.server was deprecated hence this interface is also deprecated. Use
 *   {@link LastClose} instead.
 */
@Deprecated
@Value.Immutable
@JsonSerialize(as = ImmutableServerInfoLastClose.class)
@JsonDeserialize(as = ImmutableServerInfoLastClose.class)
public interface ServerInfoLastClose {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableServerInfoLastClose.Builder}.
   */
  static ImmutableServerInfoLastClose.Builder builder() {
    return ImmutableServerInfoLastClose.builder();
  }

  /**
   * The amount of time, in seconds, it took to converge.
   *
   * @return A {@link Double} representing the convergence time.
   */
  @JsonProperty("converge_time_s")
  Double convergeTimeSeconds();

  /**
   * The number of proposers in the last closed ledger.
   *
   * @return An {@link UnsignedInteger} representing the number of proposers.
   */
  UnsignedInteger proposers();
}
