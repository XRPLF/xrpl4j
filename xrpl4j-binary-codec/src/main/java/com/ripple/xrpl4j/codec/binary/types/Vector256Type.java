package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;
import com.ripple.xrpl4j.codec.binary.ObjectMapperFactory;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;

public class Vector256Type extends SerializedType<Vector256Type> {

  public static final int WIDTH = 32;

  public Vector256Type() {
    this(UnsignedByteArray.empty());
  }

  public Vector256Type(UnsignedByteArray list) {
    super(list);
  }

  @Override
  public Vector256Type fromParser(BinaryParser parser, OptionalInt lengthHint) {
    UnsignedByteArray byteArray = UnsignedByteArray.empty();
    int bytes = lengthHint.orElse(parser.size());
    int hashes = bytes / WIDTH;
    for (int i = 0; i < hashes; i++) {
      new Hash256Type().fromParser(parser).toBytesSink(byteArray);
    }
    return new Vector256Type(byteArray);
  }

  @Override
  public Vector256Type fromJSON(JsonNode node) {
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
      new Hash256Type().fromJSON(child).toBytesSink(byteList);
    }
    return new Vector256Type(byteList);
  }

  @Override
  public JsonNode toJSON() {
    BinaryParser parser = new BinaryParser(this.toString());
    List<JsonNode> values = new ArrayList<>();
    while(!parser.end()) {
      UnsignedByteArray bytes = parser.read(32);
      values.add(new TextNode(bytes.hexValue()));
    }
    return new ArrayNode(ObjectMapperFactory.getObjectMapper().getNodeFactory(), values);
  }
}
