package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * JSON mapping object for the Issue serializable type.
 * Supports XRP, IOU (currency + issuer), and MPT (mpt_issuance_id) issues.
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
   * The currency code of the Issue. Will be empty if this is an MPT issue.
   *
   * @return An optionally present {@link JsonNode} containing the currency code.
   */
  Optional<JsonNode> currency();

  /**
   * The address of the issuer of this currency. Will be empty if {@link #currency()} is XRP or if this is an MPT issue.
   *
   * @return An optionally present {@link JsonNode}.
   */
  Optional<JsonNode> issuer();

  /**
   * The MPT issuance ID. Will be present only for MPT issues.
   *
   * @return An optionally present {@link JsonNode} containing the MPT issuance ID.
   */
  @JsonProperty("mpt_issuance_id")
  Optional<JsonNode> mptIssuanceId();

  /**
   * Validate that this Issue has valid field combinations.
   */
  @Value.Check
  default void checkFields() {
    if (currency().isPresent()) {
      Preconditions.checkState(
        !mptIssuanceId().isPresent(),
        "Issue cannot have both currency and mpt_issuance_id."
      );
      if (currency().get().asText().equals("XRP")) {
        Preconditions.checkState(!issuer().isPresent(), "If Issue is XRP, issuer must be empty.");
      }
    } else {
      Preconditions.checkState(
        mptIssuanceId().isPresent(),
        "Issue must have either currency or mpt_issuance_id."
      );
      Preconditions.checkState(!issuer().isPresent(), "MPT Issue must not have an issuer.");
    }
  }

}
