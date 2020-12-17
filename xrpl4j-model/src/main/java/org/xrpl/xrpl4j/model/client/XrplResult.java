package org.xrpl.xrpl4j.model.client;

import java.util.Optional;

/**
 * Marker interface for rippled API results.
 */
public interface XrplResult {

  /**
   * The value {@code "success"} indicates the request was successfully received and understood by the server.
   *
   * @return The {@link String} {@code "success"} if the request was successful, otherwise {@link Optional#empty()}.
   */
  Optional<String> status();

}
