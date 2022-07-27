package org.xrpl.xrpl4j.model.client.serverinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;

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
   * The version number of the running rippled version.
   *
   * @return A {@link String} containing the version number.
   */
  @JsonProperty("rippled_version")
  String rippledVersion();
}
