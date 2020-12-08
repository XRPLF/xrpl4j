package org.xrpl.xrpl4j.model.client.server;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.rippled.XrplResult;

/**
 * Wrapper of the "server_info" API result.
 */
@Immutable
@JsonSerialize(as = ImmutableServerInfoWrapper.class)
@JsonDeserialize(as = ImmutableServerInfoWrapper.class)
public interface ServerInfoWrapper extends XrplResult {

  ServerInfo info();

}
