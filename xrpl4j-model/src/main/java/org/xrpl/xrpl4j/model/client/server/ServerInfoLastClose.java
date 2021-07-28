package org.xrpl.xrpl4j.model.client.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

/**
 * Information about the last time the server closed a ledger, including the amount of time it took to reach a
 * consensus and the number of trusted validators participating.
 */
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
