package org.xrpl.xrpl4j.model.ledger;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
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

/**
 * Represents an IOU (issued currency) asset on the ledger, identified by a currency code and issuer address.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableIouIssue.class)
@JsonDeserialize(as = ImmutableIouIssue.class)
public interface IouIssue extends Issue {

  /**
   * Construct an {@code IouIssue} builder.
   *
   * @return An {@link ImmutableIouIssue.Builder}.
   */
  static ImmutableIouIssue.Builder builder() {
    return ImmutableIouIssue.builder();
  }

  /**
   * Either a 3 character currency code, or a 40 character hexadecimal encoded currency code value.
   *
   * @return A {@link String} containing the currency code.
   */
  String currency();

  /**
   * The {@link Address} of the issuer of the currency.
   *
   * @return The {@link Address} of the issuer account.
   */
  Address issuer();

}
