package org.xrpl.xrpl4j.model.client.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableJobType.class)
@JsonDeserialize(as = ImmutableJobType.class)
public interface JobType {

  static ImmutableJobType.Builder builder() {
    return ImmutableJobType.builder();
  }

  @JsonProperty("job_type")
  String jobType();

  @JsonProperty("in_progress")
  Optional<UnsignedInteger> inProgress();

  @JsonProperty("peak_time")
  Optional<UnsignedInteger> peakTime();

  @JsonProperty("per_second")
  Optional<UnsignedInteger> perSecond();

  @JsonProperty("avg_time")
  Optional<UnsignedInteger> averageTime();

}
