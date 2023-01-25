package org.xrpl.xrpl4j.model.client.serverinfo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;

/**
 * Result of a "server_info" rippled API method request.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableServerInfoResult.class)
@JsonDeserialize(as = ImmutableServerInfoResult.class)
public interface ServerInfoResult extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableServerInfoResult.Builder}.
   */
  static ImmutableServerInfoResult.Builder builder() {
    return ImmutableServerInfoResult.builder();
  }

  /**
   * Information about the requested server.
   *
   * @return A {@link ServerInfo}.
   */
  ServerInfo info();
}
