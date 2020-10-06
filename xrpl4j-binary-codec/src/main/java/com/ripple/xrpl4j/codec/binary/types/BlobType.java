package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;
import com.ripple.xrpl4j.codec.binary.serdes.UnsignedByteList;

import java.util.OptionalInt;

public class BlobType extends SerializedType<BlobType> {
  public BlobType() {
    this(new UnsignedByteList());
  }

  public BlobType(UnsignedByteList list) {
    super(list);
  }

  @Override
  public BlobType fromParser(BinaryParser parser, OptionalInt lengthHint) {
    return new BlobType(parser.read(lengthHint.getAsInt()));
  }

  @Override
  public BlobType fromJSON(JsonNode node) {
    return new BlobType(new UnsignedByteList(node.asText()));
  }

}
