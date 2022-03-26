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

import java.util.Optional;

/**
 * (Admin only) Information about the rate of a job the server is doing and how much time it spends on it.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableJobType.class)
@JsonDeserialize(as = ImmutableJobType.class)
public interface JobType {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableJobType.Builder}.
   */
  static ImmutableJobType.Builder builder() {
    return ImmutableJobType.builder();
  }

  /**
   * The type of job.
   *
   * @return A {@link String} representing the job type.
   */
  @JsonProperty("job_type")
  String jobType();

  /**
   * The number of jobs that are currently in progress.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the number of jobs in progress.
   */
  @JsonProperty("in_progress")
  Optional<UnsignedInteger> inProgress();

  /**
   * The peak time of the job.
   *
   * @return An optionally-present {@link UnsignedInteger} denoting the peak time.
   */
  @JsonProperty("peak_time")
  Optional<UnsignedInteger> peakTime();

  /**
   * The number of jobs of this type performed per second.
   *
   * @return An optionally-present {@link UnsignedInteger} denoting the number of jobs.
   */
  @JsonProperty("per_second")
  Optional<UnsignedInteger> perSecond();

  /**
   * The average time, in seconds, jobs of this type take to perform.
   *
   * @return An optionally-present {@link UnsignedInteger} denoting the average time.
   */
  @JsonProperty("avg_time")
  Optional<UnsignedInteger> averageTime();

}
