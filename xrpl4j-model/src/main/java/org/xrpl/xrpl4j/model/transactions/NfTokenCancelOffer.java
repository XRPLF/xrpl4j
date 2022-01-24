package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

/**
 * The {@link NfTokenCancelOffer} transaction creates an NfToken object and adds it to the
 * relevant NfTokenPage object of the minter. If the transaction is
 * successful, the newly minted token will be owned by the minter(issuer) account
 * specified by the transaction.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNfTokenCancelOffer.class)
@JsonDeserialize(as = ImmutableNfTokenCancelOffer.class)
public interface NfTokenCancelOffer extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableNfTokenCancelOffer.Builder}.
   */
  static ImmutableNfTokenCancelOffer.Builder builder() {
    return ImmutableNfTokenCancelOffer.builder();
  }

  /**
   * An array of TokenID objects, each identifying an
   * NfTokenOffer object, which should be cancelled by this
   * transaction.
   *
   * <p>It is an error if an entry in this list points to an
   * object that is not an NfTokenOffer object. It is not an
   * error if an entry in this list points to an object that
   * does not exist.
   *
   * @return Array of TokenIDs, each identifying a unique NfToken object, to cancel the offers for.
   */
  @JsonProperty("TokenIDs")
  List<NfTokenId> tokenIds();

  /**
   * Token Ids array should have atleast one value.
   */
  @Value.Check
  default void nonEmptyTokenIds() {

    Preconditions.checkArgument(
      tokenIds().size() > 0,
      String.format("List of tokenIds must be non-empty.")
    );
  }
}