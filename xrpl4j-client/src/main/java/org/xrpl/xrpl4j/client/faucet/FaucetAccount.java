package org.xrpl.xrpl4j.client.faucet;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Optional;

/**
 * Faucet account details returned as part of a request to the /accounts API.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableFaucetAccount.class)
@JsonDeserialize(as = ImmutableFaucetAccount.class)
public interface FaucetAccount {

  /**
   * X-Address of the created account.
   *
   * @return A {@link String} containing the X-Address.
   */
  @SuppressWarnings("MethodName")
  String xAddress();

  /**
   * Classic address of the created account.
   *
   * @return An {@link Address} containing the classic address of the account.
   */
  Address classicAddress();

  /**
   * Same value as classicAddress.
   *
   * @return An {@link Address} containing the classic address of the account.
   */
  Address address();

  /**
   * Private secret/seed for the address.
   *
   * @return An {@link Optional} of type {@link String} containing the private key for the account.
   */
  Optional<String> secret();

}
