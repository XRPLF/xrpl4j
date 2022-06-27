package org.xrpl.xrpl4j.model.client.serverinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.xrpl.xrpl4j.model.client.server.ServerInfoLastClose;

import java.util.Optional;

public interface RippledServerInfo extends ServerInfo {

  /**
   * Information about the last time the server closed a ledger, including the amount of time it took to reach a
   * consensus and the number of trusted validators participating.
   *
   * @return A {@link org.xrpl.xrpl4j.model.client.server.ServerInfoLastClose}.
   */
  @JsonProperty("last_close")
  Optional<ServerInfoLastClose> lastClose();

}
