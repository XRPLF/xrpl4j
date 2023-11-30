package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * JSON mapping object for the Issue serializable type.
 */
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

  /**
   * The currency code of the Issue.
   *
   * @return A {@link JsonNode} containing the currency code.
   */
  JsonNode currency();

  /**
   * The address of the issuer of this currency. Will be empty if {@link #currency()} is XRP.
   *
   * @return An optionally present {@link JsonNode}.
   */
  Optional<JsonNode> issuer();

  /**
   * Validate that {@link #issuer()} is empty if {@link #currency()} is "XRP".
   */
  @Value.Check
  default void checkIssuerEmptyForXrp() {
    if (currency().asText().equals("XRP")) {
      Preconditions.checkState(!issuer().isPresent(), "If Issue is XRP, issuer must be empty.");
    }
  }

}
