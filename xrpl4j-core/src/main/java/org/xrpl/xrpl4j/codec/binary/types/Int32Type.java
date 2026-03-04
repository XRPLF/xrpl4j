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

import java.nio.ByteBuffer;

/**
 * Codec for XRPL Int32 type (signed 32-bit integer using two's complement encoding).
 */
public class Int32Type extends SerializedType<Int32Type> {

  public Int32Type() {
    this(0);
  }

  /**
   * Constructs an {@link Int32Type} from a signed integer value.
   *
   * @param value The signed integer value to encode.
   */
  public Int32Type(int value) {
    super(UnsignedByteArray.fromHex(encodeInt32(value)));
  }

  /**
   * Constructs an {@link Int32Type} from an {@link UnsignedByteArray}.
   *
   * @param value The {@link UnsignedByteArray} containing the encoded Int32 value (must be 4 bytes).
   * @throws IllegalArgumentException if the value is not exactly 4 bytes.
   */
  public Int32Type(UnsignedByteArray value) {
    super(value);
    if (value.length() != 4) {
      throw new IllegalArgumentException("Invalid Int32 length: " + value.length());
    }
  }

  /**
   * Encode a signed 32-bit integer to hex string using two's complement.
   *
   * @param value The signed integer value to encode.
   * @return A hex string representation of the value.
   */
  private static String encodeInt32(int value) {
    ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.putInt(value);
    return UnsignedByteArray.of(buffer.array()).hexValue();
  }

  /**
   * Decode a two's complement encoded Int32 from UnsignedByteArray.
   *
   * @param unsignedByteArray The bytes to decode.
   * @return The decoded signed integer value.
   */
  private static int decodeInt32(UnsignedByteArray unsignedByteArray) {
    byte[] bytes = unsignedByteArray.toByteArray();
    if (bytes.length != 4) {
      throw new IllegalArgumentException("Invalid Int32 byte length: " + bytes.length);
    }
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    return buffer.getInt();
  }

  /**
   * Get the integer value of this Int32Type.
   *
   * @return The signed integer value.
   */
  public int intValue() {
    return decodeInt32(value());
  }

  @Override
  public Int32Type fromParser(BinaryParser parser) {
    return new Int32Type(parser.read(4));
  }

  @Override
  public Int32Type fromJson(JsonNode node) {
    if (node.isNumber()) {
      return new Int32Type(node.asInt());
    } else if (node.isTextual()) {
      String text = node.asText();
      // Support hex (0x prefix) and binary (0b prefix) in addition to decimal
      if (text.startsWith("0x") || text.startsWith("0X")) {
        return new Int32Type((int) Long.parseLong(text.substring(2), 16));
      } else if (text.startsWith("0b") || text.startsWith("0B")) {
        return new Int32Type((int) Long.parseLong(text.substring(2), 2));
      } else {
        return new Int32Type(Integer.parseInt(text));
      }
    } else {
      throw new IllegalArgumentException("Invalid JSON node type for Int32: " + node.getNodeType());
    }
  }

  @Override
  public JsonNode toJson() {
    return new IntNode(intValue());
  }
}
