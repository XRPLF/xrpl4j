package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableMap;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;
import com.ripple.xrpl4j.codec.binary.serdes.UnsignedByteList;

import java.util.Map;
import java.util.OptionalInt;

abstract public class SerializedType<T extends SerializedType<T>> implements SerializedComparable<T> {

  private final UnsignedByteList bytes;

  private static Map<String, Class<SerializedType>> typeMap =
      new ImmutableMap.Builder()
          .put("UInt8", UInt8.class)
          .put("UInt16", UInt16.class)
          .put("STObject", STObject.class)
          .build();

  public static SerializedType getTypeByName(String name) {
    try {
      return typeMap.get(name).getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Could not create serialized type for " + name, e);
    }
  }

  public SerializedType(UnsignedByteList bytes) {
    this.bytes = bytes;
  }

  public abstract T fromParser(BinaryParser parser, OptionalInt lengthHint);

  public abstract T fromJSON(JsonNode node);

  public void toBytesSink(UnsignedByteList list) {
    list.put(this.bytes);
  }

  public byte[] toBytes() {
    return bytes.toBytes();
  }

  public JsonNode toJSON() {
    return new TextNode(toHex());
  }

  public String toString() {
    return this.toHex();
  }

  private String toHex() {
    return bytes.toHex();
  }


}
