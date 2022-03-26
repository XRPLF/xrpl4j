package org.xrpl.xrpl4j.client.faucet;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: client
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */


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
