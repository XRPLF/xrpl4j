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

/**
 * An X-Address, decoded into an AccountID, destination tag, and a boolean for testnet or mainnet.
 * Note that the AccountID in this decoded X-Address is not Base58 encoded.
 */
@Value.Immutable
public interface DecodedXAddress {

  /**
   * Get a new {@link ImmutableDecodedXAddress.Builder} instance.
   *
   * @return A {@link ImmutableDecodedXAddress.Builder}.
   */
  static ImmutableDecodedXAddress.Builder builder() {
    return ImmutableDecodedXAddress.builder();
  }

  /**
   * The Account ID of the X-Address.
   *
   * @return An {@link UnsignedByteArray} containing the Account ID.
   */
  UnsignedByteArray accountId();

  /**
   * The tag of the X-Address.
   *
   * @return An {@link UnsignedInteger} representing the tag.
   */
  UnsignedInteger tag();

  /**
   * Whether or not this address exists on mainnet or testnet.
   *
   * @return {@code true} if it is a tesnet address, {@code false} if it is mainnet.
   */
  boolean test();

}
