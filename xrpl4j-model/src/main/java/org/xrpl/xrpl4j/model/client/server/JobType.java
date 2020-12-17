package org.xrpl.xrpl4j.model.client.server;

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
