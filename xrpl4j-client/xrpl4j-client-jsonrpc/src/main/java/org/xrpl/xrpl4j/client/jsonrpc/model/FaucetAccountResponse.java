package org.xrpl.xrpl4j.client.jsonrpc.model;

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
   *
   * @return A {@link FaucetAccount}.
   */
  FaucetAccount account();

  /**
   * Amount the faucet sent to the account.
   *
   * @return A long representing the amount of XRP the faucent sent to the account.
   */
  long amount();

  /**
   * Current balance of the account. Only sent back for newly generated accounts.
   *
   * @return An optionally_present {@link Long} representing the balance of the account.
   */
  OptionalLong balance();

}
