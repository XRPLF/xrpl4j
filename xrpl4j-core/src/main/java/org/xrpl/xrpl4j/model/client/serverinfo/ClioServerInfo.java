package org.xrpl.xrpl4j.model.client.serverinfo;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * An implementation of {@link ServerInfo} that conforms to Clio server payloads.
 */
@Beta
@Value.Immutable
@JsonSerialize(as = ImmutableClioServerInfo.class)
@JsonDeserialize(as = ImmutableClioServerInfo.class)
public interface ClioServerInfo extends ServerInfo {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableClioServerInfo.Builder}.
   */
  static ImmutableClioServerInfo.Builder builder() {
    return ImmutableClioServerInfo.builder();
  }

  /**
   * The version number of the running clio version.
   *
   * @return A {@link String} containing the version number.
   */
  @JsonProperty("clio_version")
  String clioVersion();

  /**
   * The version number of the running rippled version. Optional because clio tries to get the info from rippled, but if
   * there is an error for some reason, the info is not included.
   *
   * @return A {@link String} containing the version number.
   */
  @JsonProperty("rippled_version")
  Optional<String> rippledVersion();
}
