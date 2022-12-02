package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableIssue.class)
@JsonDeserialize(as = ImmutableIssue.class)
public interface Issue {

  /**
   * Construct a {@code Issue} builder.
   *
   * @return An {@link ImmutableIssue.Builder}.
   */
  static ImmutableIssue.Builder builder() {
    return ImmutableIssue.builder();
  }

  JsonNode currency();

  Optional<JsonNode> issuer();

  @Value.Check
  default void checkIssuerEmptyForXrp() {
    if (currency().asText().equals("XRP")) {
      Preconditions.checkState(!issuer().isPresent(), "If Issue is XRP, issuer must be empty.");
    }
  }

}
