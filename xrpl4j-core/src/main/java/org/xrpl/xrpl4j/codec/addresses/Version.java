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

public enum Version {

  ED25519_SEED(new int[] {0x01, 0xE1, 0x4B}),
  FAMILY_SEED(new int[] {0x21}),
  ACCOUNT_ID(new int[] {0}),
  NODE_PUBLIC(new int[] {0x1C}),
  ACCOUNT_PUBLIC_KEY(new int[] {0x23});

  private final int[] values;

  Version(int[] values) {
    this.values = values;
  }

  public int[] getValues() {
    return values;
  }

  /**
   * Get values as a byte array.
   *
   * @return A byte array of values.
   */
  public byte[] getValuesAsBytes() {
    byte[] bytes = new byte[values.length];
    for (int i = 0; i < values.length; i++) {
      bytes[i] = (byte) values[i];
    }
    return bytes;
  }
}
