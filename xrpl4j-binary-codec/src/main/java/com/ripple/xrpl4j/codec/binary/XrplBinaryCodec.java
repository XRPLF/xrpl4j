package com.ripple.xrpl4j.codec.binary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;
import com.ripple.xrpl4j.codec.binary.types.STObjectType;

public class XrplBinaryCodec {

  /**
   * Encodes JSON to canonical XRPL binary as a hex string.
   *
   * @param json String containing JSON to be encoded.
   * @return hex encoded representations
   * @throws JsonProcessingException if JSON is not valid.
   */
  public String encode(String json) throws JsonProcessingException {
    JsonNode node = ObjectMapperFactory.getObjectMapper().readTree(json);
    UnsignedByteArray byteList = UnsignedByteArray.empty();
    new STObjectType().fromJSON(node).toBytesSink(byteList);
    return byteList.hexValue();
  }

  /**
   * Decodes canonical XRPL binary hex string to JSON.
   *
   * @param hex value to decode.
   * @return
   * @throws JsonProcessingException
   */
  public String decode(String hex) {
    return new BinaryParser(hex).readType(STObjectType.class)
        .toJSON()
        .toString();
  }


}
