package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

import java.util.Optional;

/**
 * Model object a Hop object inside a XRPL {@link PathType} object.
 */
@Immutable
@JsonSerialize(as = ImmutableHop.class)
@JsonDeserialize(as = ImmutableHop.class)
public interface Hop {

  static ImmutableHop.Builder builder() {
    return ImmutableHop.builder();
  }

  Optional<JsonNode> issuer();

  Optional<JsonNode> account();

  Optional<JsonNode> currency();

}
