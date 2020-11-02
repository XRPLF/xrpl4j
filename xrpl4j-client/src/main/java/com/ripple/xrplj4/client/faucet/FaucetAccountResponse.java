package com.ripple.xrplj4.client.faucet;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

import java.util.OptionalLong;

/**
 * Response to a POST request to the /accounts API.
 */
@Immutable
@JsonSerialize(as = ImmutableFaucetAccountResponse.class)
@JsonDeserialize(as = ImmutableFaucetAccountResponse.class)
public interface FaucetAccountResponse {

  /**
   * XRPL account that was created on testnet.
   */
  FaucetAccount account();

  /**
   * Amount the faucet sent to the account.
   */
  long amount();

  /**
   * Current balance of the account. Only sent back for newly generated accounts.
   */
  OptionalLong balance();

}
