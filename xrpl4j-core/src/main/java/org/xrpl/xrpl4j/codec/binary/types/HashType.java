package org.xrpl.xrpl4j.codec.binary.types;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: binary-codec
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

import com.google.common.base.Preconditions;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Base class for XRPL Hash types.
 */
public abstract class HashType<T extends HashType<T>> extends SerializedType<T> {

  private final int width;

  /**
   * Required-args Constructor.
   *
   * @param bytes An {@link UnsignedByteArray}.
   * @param width An integer.
   */
  public HashType(final UnsignedByteArray bytes, final int width) {
    super(bytes);
    Preconditions.checkArgument(bytes.length() == width, "Invalid hash length " + bytes.length());
    this.width = width;
  }

  @Override
  public String toString() {
    return this.toHex();
  }

  @Override
  public T fromHex(String hex) {
    return super.fromHex(hex, width);
  }

  public int getWidth() {
    return width;
  }

  /**
   * Returns four bits at the specified depth within a hash.
   *
   * @param depth The depth of the four bits.
   *
   * @return The number represented by the four bits.
   */
  // TODO: Delete if unused?
  public int nibblet(int depth) {
    int byteIndex = depth > 0 ? (depth / 2) | 0 : 0;
    int theByte = this.value().get(byteIndex).asInt();
    if (depth % 2 == 0) {
      theByte = (theByte & 0xf0) >>> 4;
    } else {
      theByte = theByte & 0x0f;
    }
    return theByte;
  }

}
