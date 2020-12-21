package org.xrpl.xrpl4j.client.faucet;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Request object for POST requests to the /accounts API on the XRPL faucet.
 */
@Immutable
@JsonSerialize(as = ImmutableFundAccountRequest.class)
@JsonDeserialize(as = ImmutableFundAccountRequest.class)
public interface FundAccountRequest {

  static ImmutableFundAccountRequest.Builder builder() {
    return ImmutableFundAccountRequest.builder();
  }

  /**
   * Construct a {@link FundAccountRequest} for the given address.
   *
   * @param classicAddress The {@link Address} of the account to fund.
   *
   * @return A {@link FundAccountRequest}.
   */
  static FundAccountRequest of(Address classicAddress) {
    return builder().destination(classicAddress).build();
  }

  /**
   * The account to be funded.
   *
   * @return The {@link Address} containing the classic address of the account.
   */
  Address destination();

}
