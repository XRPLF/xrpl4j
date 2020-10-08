package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.OptionalInt;

/**
 * Codec for XRPL Blob type.
 */
public class BlobType extends SerializedType<BlobType> {

  public BlobType() {
    this(UnsignedByteArray.empty());
  }

  public BlobType(UnsignedByteArray list) {
    super(list);
  }

  @Override
  public BlobType fromParser(BinaryParser parser, OptionalInt lengthHint) {
    return new BlobType(parser.read(lengthHint.getAsInt()));
  }

  @Override
  public BlobType fromJSON(JsonNode node) {
    return new BlobType(UnsignedByteArray.fromHex(node.asText()));
  }

}
