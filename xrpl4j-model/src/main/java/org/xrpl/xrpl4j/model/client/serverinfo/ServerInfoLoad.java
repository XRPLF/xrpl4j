package org.xrpl.xrpl4j.model.client.serverinfo;

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
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.serverinfo.JobType;

import java.util.List;

/**
 * Information about the current load state of a rippled server.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableServerInfoLoad.class)
@JsonDeserialize(as = ImmutableServerInfoLoad.class)
public interface ServerInfoLoad {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableServerInfoLoad.Builder}.
   */
  static ImmutableServerInfoLoad.Builder builder() {
    return ImmutableServerInfoLoad.builder();
  }

  /**
   * (Admin only) Information about the rate of different types of jobs the server is doing and how much time
   * it spends on each.
   *
   * @return A {@link List} of {@link JobType}s.
   */
  @JsonProperty("job_types")
  List<JobType> jobTypes();

  /**
   * (Admin only) The number of threads in the server's main job pool.
   *
   * @return An {@link UnsignedLong} representing the number of threads.
   */
  UnsignedInteger threads();
}
