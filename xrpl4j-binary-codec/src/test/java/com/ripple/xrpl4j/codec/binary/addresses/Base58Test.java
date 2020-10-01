package com.ripple.xrpl4j.codec.binary.addresses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

public class Base58Test {

  @Test
  public void testEncodeDecodeString() throws Exception {
    byte[] decoded = "Hello World".getBytes();
    String encoded = "JxErpTiA7PhnBMd";
    assertThat(Base58.encode(decoded)).isEqualTo(encoded);
    assertThat(Base58.decode(encoded)).isEqualTo(decoded);
  }

  @Test
  void testEncodeUnsignedInteger() throws EncodingFormatException {
    UnsignedLong decoded = UnsignedLong.valueOf(3471844090L);
    String encoded = "raHofH1";
    assertThat(Base58.encode(decoded.bigIntegerValue().toByteArray())).isEqualTo(encoded);
    assertThat(Base58.decode(encoded)).isEqualTo(decoded.bigIntegerValue().toByteArray());
  }

  @Test
  void testEncodeDecodeZeroByte() throws EncodingFormatException {
    byte[] decoded = new byte[1];
    String encoded = "r";
    assertThat(Base58.encode(decoded)).isEqualTo(encoded);
    assertThat(Base58.decode(encoded)).isEqualTo(decoded);
  }

  @Test
  void testEncodeDecodeZeroBytes() throws EncodingFormatException {
    byte[] decoded = new byte[7];
    String encoded = "rrrrrrr";
    assertThat(Base58.encode(decoded)).isEqualTo(encoded);
    assertThat(Base58.decode(encoded)).isEqualTo(decoded);
  }

  @Test
  void testDecodeInvalidBase58() {
    try {
      Base58.decode("This isn't valid base58");
      fail();
    } catch (EncodingFormatException e) {

    }
  }
}
