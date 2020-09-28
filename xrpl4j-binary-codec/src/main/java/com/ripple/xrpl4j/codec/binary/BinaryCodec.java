package com.ripple.xrpl4j.codec.binary;

public interface BinaryCodec {

  String decode(String encoded);

  String encode(String json);

}
