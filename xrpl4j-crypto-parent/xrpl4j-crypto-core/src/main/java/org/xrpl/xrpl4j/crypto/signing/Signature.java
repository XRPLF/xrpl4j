package org.xrpl.xrpl4j.crypto.signing;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: crypto :: core
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

import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Represents a digital signature for a transaction that can be submitted to the XRP Ledger.
 */
@Value.Immutable
public interface Signature {

  /**
   * Instantiates a new builder.
   *
   * @return A {@link ImmutableSignature.Builder}.
   */
  static ImmutableSignature.Builder builder() {
    return ImmutableSignature.builder();
  }

  /**
   * The bytes of this signature.
   *
   * @return A {@link UnsignedByteArray}.
   */
  UnsignedByteArray value();

  /**
   * Accessor for this signature as a base16-encoded (HEX) string.
   *
   * @return A {@link String}.
   */
  @Derived
  default String base16Value() {
    return BaseEncoding.base16().encode(value().toByteArray());
  }
}
