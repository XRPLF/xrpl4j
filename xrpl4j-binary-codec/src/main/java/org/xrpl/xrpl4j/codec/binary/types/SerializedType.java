package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableMap;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Supplier;

abstract public class SerializedType<T extends SerializedType<T>> {

  private static Map<String, Supplier<SerializedType>> typeMap =
      new ImmutableMap.Builder<String, Supplier<SerializedType>>()
          .put("AccountID", () -> new AccountIdType())
          .put("Amount", () -> new AmountType())
          .put("Blob", () -> new BlobType())
          .put("Currency", () -> new CurrencyType())
          .put("Hash128", () -> new Hash128Type())
          .put("Hash160", () -> new Hash160Type())
          .put("Hash256", () -> new Hash256Type())
          .put("PathSet", () -> new PathSetType())
          .put("STArray", () -> new STArrayType())
          .put("STObject", () -> new STObjectType())
          .put("UInt8", () -> new UInt8Type())
          .put("UInt16", () -> new UInt16Type())
          .put("UInt32", () -> new UInt32Type())
          .put("Vector256", () -> new Vector256Type())
          .build();
  private final UnsignedByteArray bytes;

  public SerializedType(UnsignedByteArray bytes) {
    this.bytes = bytes;
  }

  public static SerializedType getTypeByName(String name) {
    return typeMap.get(name).get();
  }

  public static String getNameByType(SerializedType type) {
    return typeMap.entrySet()
        .stream()
        .filter(entry -> entry.getValue().get().getClass().equals(type.getClass()))
        .map(Map.Entry::getKey)
        .findAny()
        .orElse(null);
  }

  public T fromParser(BinaryParser parser) {
    return fromParser(parser, OptionalInt.empty());
  }

  public abstract T fromParser(BinaryParser parser, OptionalInt lengthHint);

  public abstract T fromJSON(JsonNode node) throws JsonProcessingException;

  public T fromHex(String hex) {
    return fromParser(new BinaryParser(hex));
  }

  public T fromHex(String hex, int hint) {
    return fromParser(new BinaryParser(hex), OptionalInt.of(hint));
  }

  public T fromJSON(String json) {
    try {
      JsonNode node = BinaryCodecObjectMapperFactory.getObjectMapper().readTree(json);
      UnsignedByteArray byteList = UnsignedByteArray.empty();
      T newValue = fromJSON(node);
      newValue.toBytesSink(byteList);
      return newValue;
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public void toBytesSink(UnsignedByteArray list) {
    list.append(this.bytes);
  }

  public byte[] toBytes() {
    return bytes.toByteArray();
  }

  public JsonNode toJSON() {
    return new TextNode(toHex());
  }

  public String toString() {
    return this.toHex();
  }

  public final String toHex() {
    return bytes.hexValue();
  }

  protected UnsignedByteArray value() {
    return bytes;
  }

}
