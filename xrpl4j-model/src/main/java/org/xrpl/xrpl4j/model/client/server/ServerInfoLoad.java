package org.xrpl.xrpl4j.model.client.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;

import java.util.List;

/**
 * Information about the current load state of a rippled server.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableServerInfoLoad.class)
@JsonDeserialize(as = ImmutableServerInfoLoad.class)
public interface ServerInfoLoad {

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
  UnsignedLong threads();
}
