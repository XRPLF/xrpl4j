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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Codec for XRPL Path type inside a PathSet.
 */
public class PathType extends SerializedType<PathType> {

  /**
   * Constants for separating Paths in a PathSet.
   */
  public static final String PATHSET_END_HEX = "00";
  public static final String PATH_SEPARATOR_HEX = "FF";

  public PathType() {
    this(UnsignedByteArray.empty());
  }

  public PathType(UnsignedByteArray list) {
    super(list);
  }

  @Override
  public PathType fromParser(BinaryParser parser) {
    UnsignedByteArray byteArray = UnsignedByteArray.empty();

    while (parser.hasMore()) {
      byteArray.append(new HopType().fromParser(parser).value());
      String nextByte = parser.peek().hexValue();
      if (nextByte.equals(PATH_SEPARATOR_HEX) || nextByte.equals(PATHSET_END_HEX)) {
        break;
      }
    }
    return new PathType(byteArray);
  }

  @Override
  public PathType fromJson(JsonNode node) throws JsonProcessingException {
    if (!node.isArray()) {
      throw new IllegalArgumentException("node is not an object");
    }
    UnsignedByteArray byteArray = UnsignedByteArray.empty();
    Iterator<JsonNode> nodeIterator = node.elements();
    while (nodeIterator.hasNext()) {
      JsonNode child = nodeIterator.next();
      byteArray.append(new HopType().fromJson(child).value());
    }
    return new PathType(byteArray);
  }

  @Override
  public JsonNode toJson() {
    List<JsonNode> values = new ArrayList<>();
    BinaryParser parser = new BinaryParser(this.toHex());
    while (parser.hasMore()) {
      values.add(new HopType().fromParser(parser).toJson());
    }
    return new ArrayNode(BinaryCodecObjectMapperFactory.getObjectMapper().getNodeFactory(), values);
  }
}
