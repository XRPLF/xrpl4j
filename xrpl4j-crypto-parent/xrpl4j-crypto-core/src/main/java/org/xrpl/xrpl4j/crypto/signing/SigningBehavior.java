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

/**
 * Defines the type of signature to perform.
 *
 * @deprecated Prefer the variant found in {@link org.xrpl.xrpl4j.crypto.core} instead.
 */
@Deprecated
public enum SigningBehavior {
  /**
   * Indicates the signature was generated for a multi-signed transaction.
   *
   * @see "https://xrpl.org/sign.html"
   */
  SINGLE,
  /**
   * Indicates the signature was generated for a multi-signed transaction.
   *
   * @see "https://xrpl.org/multi-signing.html"
   */
  MULTI
}
