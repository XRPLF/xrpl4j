package org.xrpl.xrpl4j.model.client.server;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

import java.time.ZonedDateTime;

/**
 * Maps the fields inside the "info" section of the "server_info" API call. At the moment, this class only
 * maps a subset of the response data.
 */
@Immutable
@JsonSerialize(as = ImmutableServerInfo.class)
@JsonDeserialize(as = ImmutableServerInfo.class)
public interface ServerInfo {

  // TODO map the rest of the fields

  @JsonProperty("build_version")
  String buildVersion();

  @JsonFormat(pattern = "yyyy-MMM-dd HH:mm:ss.SSSSSS z")
  ZonedDateTime time();

}
