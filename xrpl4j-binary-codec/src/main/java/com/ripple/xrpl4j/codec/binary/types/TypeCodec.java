package com.ripple.xrpl4j.codec.binary.types;

public interface TypeCodec {

  String getName();

  String encode(String text);

  String decode(String hex);

}
