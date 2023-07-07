package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Optional;

/**
 * Request parameters for a "book_offers" rippled API method call.
 *
 * @see "https://xrpl.org/book_offers.html"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableBookOffersRequestParams.class)
@JsonDeserialize(as = ImmutableBookOffersRequestParams.class)
public interface BookOffersRequestParams extends XrplRequestParams {

  /**
   * Construct a {@code BookOffersRequestParams} builder.
   *
   * @return An {@link ImmutableBookOffersRequestParams.Builder}.
   */
  static ImmutableBookOffersRequestParams.Builder builder() {
    return ImmutableBookOffersRequestParams.builder();
  }

  /**
   * The asset the account taking the Offer would receive.
   *
   * @return An {@link Issue}.
   */
  @JsonProperty("taker_gets")
  Issue takerGets();

  /**
   * The asset the account taking the Offer would pay.
   *
   * @return An {@link Issue}.
   */
  @JsonProperty("taker_pays")
  Issue takerPays();

  /**
   * The Address of an account to use as a perspective. The response includes this account's Offers even if they are
   * unfunded. (You can use this to see what Offers are above or below yours in the order book.)
   *
   * @return An optionally-present {@link Address}.
   */
  @JsonProperty("taker")
  Optional<Address> taker();

  /**
   * Specifies the ledger version to request. A ledger version can be specified by ledger hash, numerical ledger index,
   * or a shortcut value.
   *
   * @return A {@link LedgerSpecifier} specifying the ledger version to request.
   */
  @JsonUnwrapped
  LedgerSpecifier ledgerSpecifier();

  /**
   * Limit the number of offers to retrieve. Note that until
   * <a href="https://github.com/XRPLF/rippled/issues/3534">#3534</a> is fixed, book_offers results will not be
   * paginated.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  Optional<UnsignedInteger> limit();
}
