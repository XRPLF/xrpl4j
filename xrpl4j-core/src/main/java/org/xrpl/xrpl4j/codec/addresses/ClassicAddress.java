package org.xrpl.xrpl4j.codec.addresses;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: address-codec
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

import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * An address on the XRP Ledger represented in Classic Address form.  This form includes a Base58Check encoded
 * address, as well as a destination tag and an indicator of if the address is on XRPL-testnet or XRPL-mainnet.
 *
 * @see "https://xrpl.org/accounts.html#addresses"
 */
@Value.Immutable
public interface ClassicAddress {

  /**
   * Get a new {@link ImmutableClassicAddress.Builder} instance.
   *
   * @return A {@link ImmutableClassicAddress.Builder}.
   */
  static ImmutableClassicAddress.Builder builder() {
    return ImmutableClassicAddress.builder();
  }

  /**
   * A classic address, as an {@link Address}.
   *
   * @return An {@link Address} containing the classic address.
   */
  Address classicAddress();

  /**
   * The tag of the classic address.
   *
   * @return An {@link UnsignedInteger}.
   */
  UnsignedInteger tag();

  /**
   * Whether or not this address exists on mainnet or testnet.
   *
   * @return {@code true} if it is a tesnet address, {@code false} if it is mainnet.
   */
  boolean test();
}
