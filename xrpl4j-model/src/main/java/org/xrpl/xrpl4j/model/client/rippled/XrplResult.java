package org.xrpl.xrpl4j.model.client.rippled;

import java.util.Optional;

/**
 * Marker interface for JSON RPC API results.
 */
public interface XrplResult {

  Optional<String> status();

}
