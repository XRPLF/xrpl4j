package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;

import java.util.Optional;

/**
 * Structure of an NFToken stored on the ledger.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNfTokenObject.class)
@JsonDeserialize(as = ImmutableNfTokenObject.class)
public interface NfTokenObject {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableNfTokenObject.Builder}.
   */
  static ImmutableNfTokenObject.Builder builder() {
    return ImmutableNfTokenObject.builder();
  }

  /**
   * The unique TokenID of the token.
   *
   * @return The unique TokenID of the token.
   */
  @JsonProperty("TokenID")
  NfTokenId tokenId();

  /**
   * The URI for the data of the token.
   *
   * @return The URI for the data of the token.
   */
  @JsonProperty("URI")
  Optional<String> uri();
}
