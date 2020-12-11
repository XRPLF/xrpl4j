package org.xrpl.xrpl4j.model.client.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableServerInfoLastClose.class)
@JsonDeserialize(as = ImmutableServerInfoLastClose.class)
public interface ServerInfoLastClose {

  static ImmutableServerInfoLastClose.Builder builder() {
    return ImmutableServerInfoLastClose.builder();
  }

  @JsonProperty("converge_time_s")
  UnsignedInteger convergeTimeSeconds();

  UnsignedInteger proposers();
}
