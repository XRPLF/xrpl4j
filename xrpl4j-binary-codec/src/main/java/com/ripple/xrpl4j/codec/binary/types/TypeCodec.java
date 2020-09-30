package com.ripple.xrpl4j.codec.binary.types;

/**
 * Codec for an XRPL type (e.g. UInt32, Amount).
 */
public interface TypeCodec {

  /**
   * Type name.
   *
   * @return
   */
  String typeName();

  /**
   * Encode from raw value to hex.
   *
   * @param text the value to encoded.
   * @return
   */
  String encode(String text);

  /**
   * Decodes hex to raw value.
   *
   * @param hex the value to be decoded.
   * @return
   */
  String decode(String hex);

}
