package com.ripple.xrpl4j.codec.binary.serdes;

import com.fasterxml.jackson.databind.JsonNode;
import com.ripple.xrpl4j.codec.binary.FieldHeaderCodec;
import com.ripple.xrpl4j.codec.binary.UnsignedByte;
import com.ripple.xrpl4j.codec.binary.enums.FieldInstance;
import com.ripple.xrpl4j.codec.binary.types.SerializedType;

public class BinarySerializer {

  private final UnsignedByteList sink;

  public BinarySerializer() {
    this(new UnsignedByteList());
  }

  public BinarySerializer(UnsignedByteList sink) {
    this.sink = sink;
  }


  public void write(SerializedType value) {
    value.toBytesSink(this.sink);
  }

  public void put(String hexBytes) {
    sink.put(new UnsignedByteList(hexBytes));
  }

  public void writeBytesList(UnsignedByteList list) {
    list.toByteSink(this.sink);
  }

  /**
   * Calculate the header of Variable Length encoded bytes
   *
   * @param length the length of the bytes
   */
  private UnsignedByteList encodeVariableLength(int length) {
    if (length <= 192) {
      return new UnsignedByteList(UnsignedByte.of(length));
    } else if (length <= 12480) {
      length -= 193;
      int byte1 = 193 + (length >>> 8);
      int byte2 = length & 0xff;
      return new UnsignedByteList(UnsignedByte.of(byte1), UnsignedByte.of(byte2));
    } else if (length <= 918744) {
      length -= 12481;
      int byte1 = 241 + (length >>> 16);
      int byte2 = (length >> 8) & 0xff;
      int byte3 = length & 0xff;
      return new UnsignedByteList(UnsignedByte.of(byte1), UnsignedByte.of(byte2), UnsignedByte.of(byte3));
    }
    throw new Error("Overflow error");
  }

  /**
   * Write field and value to BinarySerializer
   *
   * @param field field to write to BinarySerializer
   * @param value value to write to BinarySerializer
   */
  public void writeFieldAndValue(FieldInstance field, SerializedType value) {
    String fieldHeaderHex = FieldHeaderCodec.getInstance().encode(field.name());
    this.sink.put(new UnsignedByteList(fieldHeaderHex));

    if (field.isVariableLengthEncoded()) {
      this.writeLengthEncoded(value);
    } else {
      value.toBytesSink(this.sink);
    }
  }

  /**
   * Write field and value to BinarySerializer
   *
   * @param field field to write to BinarySerializer
   * @param value value to write to BinarySerializer
   */
  public void writeFieldAndValue(FieldInstance field, JsonNode value) {
    SerializedType typedValue = SerializedType.getTypeByName(field.type()).fromJSON(value);
    writeFieldAndValue(field, typedValue);
  }

  /**
   * Write a variable length encoded value to the BinarySerializer
   *
   * @param value length encoded value to write to BytesList
   */
  public void writeLengthEncoded(SerializedType value) {
    UnsignedByteList bytes = new UnsignedByteList();
    value.toBytesSink(bytes);
    this.put(this.encodeVariableLength(bytes.getLength()).toHex());
    this.writeBytesList(bytes);
  }

}
