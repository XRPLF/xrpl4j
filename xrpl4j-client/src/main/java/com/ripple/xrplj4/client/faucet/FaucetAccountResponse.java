package com.ripple.xrplj4.client.faucet;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

/**
 * Response to a POST request to the /accounts API.
 */
@Immutable
@JsonSerialize(as = ImmutableFaucetAccountResponse.class)
@JsonDeserialize(as = ImmutableFaucetAccountResponse.class)
public interface FaucetAccountResponse {

  /**
   * XRPL account that was created on testnet.
   *
   * @return
   */
  FaucetAccount account();

  /**
   * Amount the faucet sent to the account.
   *
   * @return
   */
  long amount();

  /**
   * Current balance of the account.
   *
   * @return
   */
  long balance();

}
