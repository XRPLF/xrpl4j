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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Codec for XRPL Vector256 type.
 */
public class Vector256Type extends SerializedType<Vector256Type> {

  public static final int WIDTH = 32;

  public Vector256Type() {
    this(UnsignedByteArray.empty());
  }

  public Vector256Type(UnsignedByteArray list) {
    super(list);
  }

  @Override
  public Vector256Type fromParser(BinaryParser parser, int lengthHint) {
    UnsignedByteArray byteArray = UnsignedByteArray.empty();
    int bytes = lengthHint;
    int hashes = bytes / WIDTH;
    for (int i = 0; i < hashes; i++) {
      new Hash256Type().fromParser(parser).toBytesSink(byteArray);
    }
    return new Vector256Type(byteArray);
  }

  @Override
  public Vector256Type fromJson(JsonNode node) {
    if (!node.isArray()) {
      throw new IllegalArgumentException("node is not an array");
    }
    UnsignedByteArray byteList = UnsignedByteArray.empty();
    Iterator<JsonNode> nodeIterator = node.elements();
    while (nodeIterator.hasNext()) {
      JsonNode child = nodeIterator.next();
      if (!child.isTextual()) {
        throw new IllegalArgumentException("non-string value found in vector");
      }
      new Hash256Type().fromJson(child).toBytesSink(byteList);
    }
    return new Vector256Type(byteList);
  }

  @Override
  public JsonNode toJson() {
    BinaryParser parser = new BinaryParser(this.toString());
    List<JsonNode> values = new ArrayList<>();
    while (parser.hasMore()) {
      UnsignedByteArray bytes = parser.read(32);
      values.add(new TextNode(bytes.hexValue()));
    }
    return new ArrayNode(BinaryCodecObjectMapperFactory.getObjectMapper().getNodeFactory(), values);
  }
}
