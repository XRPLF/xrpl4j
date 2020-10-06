package com.ripple.xrpl4j.codec.binary.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ripple.xrpl4j.codec.addresses.UnsignedByte;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;
import com.ripple.xrpl4j.codec.binary.FieldHeaderCodec;
import com.ripple.xrpl4j.codec.binary.enums.FieldInstance;
import com.ripple.xrpl4j.codec.binary.types.SerializedType;

public class BinarySerializer {

  private final UnsignedByteArray sink;

  public BinarySerializer(UnsignedByteArray sink) {
    this.sink = sink;
  }

  public void put(String hexBytes) {
    sink.add(UnsignedByteArray.fromHex(hexBytes));
  }

  public void write(UnsignedByteArray list) {
    list.toByteSink(this.sink);
  }

  /**
   * Calculate the header of Variable Length encoded bytes
   *
   * @param length the length of the bytes
   */
  private UnsignedByteArray encodeVariableLength(int length) {
    if (length <= 192) {
      return UnsignedByteArray.of(UnsignedByte.of(length));
    } else if (length <= 12480) {
      length -= 193;
      int byte1 = 193 + (length >>> 8);
      int byte2 = length & 0xff;
      return UnsignedByteArray.of(UnsignedByte.of(byte1), UnsignedByte.of(byte2));
    } else if (length <= 918744) {
      length -= 12481;
      int byte1 = 241 + (length >>> 16);
      int byte2 = (length >> 8) & 0xff;
      int byte3 = length & 0xff;
      return UnsignedByteArray.of(UnsignedByte.of(byte1), UnsignedByte.of(byte2), UnsignedByte.of(byte3));
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
    this.sink.add(UnsignedByteArray.fromHex(fieldHeaderHex));

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
  public void writeFieldAndValue(FieldInstance field, JsonNode value) throws JsonProcessingException {
    SerializedType typedValue = SerializedType.getTypeByName(field.type()).fromJSON(value);
    writeFieldAndValue(field, typedValue);
  }

  /**
   * Write a variable length encoded value to the BinarySerializer
   *
   * @param value length encoded value to write to BytesList
   */
  public void writeLengthEncoded(SerializedType value) {
    UnsignedByteArray bytes = UnsignedByteArray.empty();
    value.toBytesSink(bytes);
    this.put(this.encodeVariableLength(bytes.length()).hexValue());
    this.write(bytes);
  }

}
