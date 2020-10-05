package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableMap;
import com.ripple.xrpl4j.codec.binary.ObjectMapperFactory;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;
import com.ripple.xrpl4j.codec.binary.serdes.UnsignedByteList;

import java.util.Map;
import java.util.OptionalInt;

abstract public class SerializedType<T extends SerializedType<T>> implements SerializedComparable<T> {

  // FIXME could be more effecient and simpler to keep everything as hex strings
  private final UnsignedByteList bytes;

  private static Map<String, Class<SerializedType>> typeMap =
      new ImmutableMap.Builder()
          .put("UInt8", UInt8.class)
          .put("UInt16", UInt16.class)
          .put("UInt32", UInt32.class)
          .put("STObject", STObject.class)
          .put("Amount", AmountType.class)
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

  public abstract T fromJSON(JsonNode node) throws JsonProcessingException;

  public T fromHex(String hex) {
    return fromParser(new BinaryParser(hex), OptionalInt.empty());
  }

  public T fromJSON(String json) {
    try {
      JsonNode node = ObjectMapperFactory.getObjectMapper().readTree(json);
      UnsignedByteList byteList = new UnsignedByteList();
      T newValue = fromJSON(node);
      newValue.toBytesSink(byteList);
      return newValue;
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }

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

  public final String toHex() {
    return bytes.toHex();
  }

  @Override
  public int compareTo(T o) {
    return this.toHex().compareTo(o.toHex());
  }

  protected UnsignedByteList value() {
    return bytes;
  }

}
