package org.xrpl.xrpl4j.model.client;

import java.util.Optional;

/**
 * Marker interface for JSON RPC API results.
 */
public interface XrplResult {

  Optional<String> status();

}
