package org.xrpl.xrpl4j.codec.addresses;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;

import java.util.function.Function;

/**
 * Abstract helper class for testing codecs.
 */
public abstract class AbstractCodecTest {

  protected static UnsignedByteArray unsignedByteArrayFromHex(String hexValue) {
    byte[] decodedHex = BaseEncoding.base16().decode(hexValue);
    return UnsignedByteArray.of(decodedHex);
  }

  /**
   * Tests the encode & decode functionality.
   *
   * @param encoder A {@link Function} for encoding.
   * @param decoder A {@link Function} for decoding.
   * @param bytes   Some bytes.
   * @param base58  A based58-encoded {@link String}.
   */
  protected void testEncodeDecode(
    final Function<UnsignedByteArray, String> encoder,
    final Function<String, UnsignedByteArray> decoder,
    final UnsignedByteArray bytes,
    final String base58
  ) {
    String encoded = encoder.apply(bytes);
    assertThat(encoded).isEqualTo(base58);

    UnsignedByteArray decoded = decoder.apply(base58);
    assertThat(decoded).isEqualTo(bytes);
  }

}
