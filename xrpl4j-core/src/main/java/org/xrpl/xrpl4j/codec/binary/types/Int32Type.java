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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import org.xrpl.xrpl4j.codec.addresses.ByteUtils;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

import java.math.BigInteger;

/**
 * Codec for the XRPL Int32 type, a signed 32-bit integer encoded on the wire using two's complement, the same
 * 4-byte-wide encoding as {@link UInt32Type}.
 */
public class Int32Type extends SerializedType<Int32Type> {

  private static final BigInteger UINT32_MASK = BigInteger.valueOf(0xFFFFFFFFL);
  private static final int HEX_LENGTH = 8;

  /**
   * No-args constructor, defaulting to a value of {@code 0}.
   */
  public Int32Type() {
    this(0);
  }

  /**
   * Required-args constructor.
   *
   * @param value The signed {@code int} value that this {@link Int32Type} should encode.
   */
  public Int32Type(int value) {
    super(UnsignedByteArray.fromHex(
      ByteUtils.padded(BigInteger.valueOf(value).and(UINT32_MASK).toString(16), HEX_LENGTH)
    ));
  }

  private Int32Type(UnsignedByteArray bytes) {
    super(bytes);
  }

  @Override
  public Int32Type fromParser(BinaryParser parser) {
    return new Int32Type(parser.read(4));
  }

  @Override
  public Int32Type fromJson(JsonNode value) {
    return new Int32Type(value.asInt());
  }

  @Override
  public JsonNode toJson() {
    return new IntNode((int) Long.parseLong(toHex(), 16));
  }
}
