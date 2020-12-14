package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

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
  public BlobType fromParser(BinaryParser parser, int lengthHint) {
    return new BlobType(parser.read(lengthHint));
  }

  @Override
  public BlobType fromJson(JsonNode node) {
    return new BlobType(UnsignedByteArray.fromHex(node.asText()));
  }

}
