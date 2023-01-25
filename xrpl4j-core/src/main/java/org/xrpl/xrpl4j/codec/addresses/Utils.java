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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

  private static final MessageDigest digest;

  static {
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);  // Can't happen.
    }
  }

  /**
   * See {@link Utils#doubleDigest(byte[], int, int)}.
   *
   * @param input A byte array to double digest.
   *
   * @return The SHA-256 hash of the SHA-256 hash of the given input.
   */
  public static byte[] doubleDigest(byte[] input) {
    return doubleDigest(input, 0, input.length);
  }

  /**
   * Calculates the SHA-256 hash of the given byte range, and then hashes the resulting hash again. This is
   * standard procedure in XRPL. The resulting hash is in big endian form.
   *
   * @param input  A byte array to double digest.
   * @param offset The beginning index of the input to digest.
   * @param length The length of the input to digest.
   *
   * @return The SHA-256 hash of the SHA-256 hash of the given input.
   */
  public static byte[] doubleDigest(byte[] input, int offset, int length) {
    synchronized (digest) {
      digest.reset();
      digest.update(input, offset, length);
      byte[] first = digest.digest();
      return digest.digest(first);
    }
  }

}
