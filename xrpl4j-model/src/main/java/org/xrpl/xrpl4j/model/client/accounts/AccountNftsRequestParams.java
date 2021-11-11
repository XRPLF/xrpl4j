package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Request parameters for the "account_ntfs" rippled API method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountNftsRequestParams.class)
@JsonDeserialize(as = ImmutableAccountNftsRequestParams.class)
public interface AccountNftsRequestParams {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAccountNftsRequestParams.Builder}.
   */
  static ImmutableAccountNftsRequestParams.Builder builder() {
    return ImmutableAccountNftsRequestParams.builder();
  }

  /**
   * The unique {@link Address} for the account.
   *
   * @return The unique {@link Address} for the account.
   */
  Address account();

  /**
   * Limit the number of NFTs to retrieve from an account. The server is not required to honor this value.
   * Must be within the inclusive range 10 to 400.
   *
   * @return An optionally-present {@link UnsignedInteger} representing the response limit.
   */
  Optional<UnsignedInteger> limit();

  /**
   * Value from a previous paginated response. Resume retrieving data where that response left off.
   *
   * @return An optionally-present {@link String} containing the marker.
   */
  Optional<Marker> marker();

}
