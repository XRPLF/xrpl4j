package org.xrpl.xrpl4j.model.client.server;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.rippled.XrplResult;

/**
 * Result wrapper for the "server_info" API result.
 */
@Immutable
@JsonSerialize(as = ImmutableServerInfoResult.class)
@JsonDeserialize(as = ImmutableServerInfoResult.class)
public interface ServerInfoResult extends XrplResult {

  ServerInfo info();

}
