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
import java.util.function.Supplier;

abstract public class SerializedType<T extends SerializedType<T>> implements SerializedComparable<T> {

  private final UnsignedByteList bytes;

  private static Map<String, Supplier<SerializedType>> typeMap =
      new ImmutableMap.Builder<String, Supplier<SerializedType>>()
          .put("UInt8", () -> new UInt8Type())
          .put("UInt16", () -> new UInt16Type())
          .put("UInt32", () -> new UInt32Type())
          .put("STObject", () -> new STObjectType())
          .put("Amount", () -> new AmountType())
          .put("Hash128", () -> new Hash128Type())
          .put("Hash160", () -> new Hash160Type())
          .put("Hash256", () -> new Hash256Type())
          .build();

  public static SerializedType getTypeByName(String name) {
    return typeMap.get(name).get();
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
