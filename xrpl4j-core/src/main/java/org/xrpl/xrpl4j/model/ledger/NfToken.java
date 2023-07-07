package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;

import java.util.Optional;

/**
 * Object mapping for NFToken inner objects. This is distinct from {@link
 * org.xrpl.xrpl4j.model.client.accounts.NfTokenObject} because that class holds other fields that are only
 * available in responses to {@code account_nfts} RPC calls.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNfToken.class)
@JsonDeserialize(as = ImmutableNfToken.class)
public interface NfToken {

  /**
   * Construct a {@code NfToken} builder.
   *
   * @return An {@link ImmutableNfToken.Builder}.
   */
  static ImmutableNfToken.Builder builder() {
    return ImmutableNfToken.builder();
  }

  /**
   * The unique NFTokenID of the token.
   *
   * @return The unique NFTokenID of the token.
   */
  @JsonProperty("NFTokenID")
  NfTokenId nfTokenId();

  /**
   * The URI for the data of the token.
   *
   * @return The URI for the data of the token.
   */
  @JsonProperty("URI")
  Optional<NfTokenUri> uri();

}
