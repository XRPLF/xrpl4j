package org.xrpl.xrpl4j.crypto.keys;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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

import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.Base58;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.util.Objects;

/**
 * A typed instance of an XRPL Base58 Encoded Secret, which can be used to generate {@link Seed}.
 */
@Value.Immutable
public interface Base58EncodedSecret {

  /**
   * Instantiates a new builder.
   *
   * @return A {@link ImmutableBase58EncodedSecret.Builder}.
   */
  static ImmutableBase58EncodedSecret.Builder builder() {
    return ImmutableBase58EncodedSecret.builder();
  }

  /**
   * Construct a {@link Base58EncodedSecret} from a base58-encoded {@link String}.
   *
   * @param base58EncodedSecret A base58-encoded {@link String}.
   *
   * @return A {@link Base58EncodedSecret}.
   */
  static Base58EncodedSecret of(final String base58EncodedSecret) {
    Objects.requireNonNull(base58EncodedSecret);
    return Base58EncodedSecret.builder()
      .value(base58EncodedSecret)
      .build();
  }

  /**
   * The value of Base58 Encoded Secret.
   *
   * @return An instance of {@link String}.
   */
  String value();

  /**
   * The decoded value of Base58 Encoded Secret.
   *
   * @return An instance of {@link UnsignedByteArray}.
   */
  @Value.Derived
  default UnsignedByteArray decodedValueBytes() {
    return UnsignedByteArray.of(Base58.decode(value()));
  }

}
